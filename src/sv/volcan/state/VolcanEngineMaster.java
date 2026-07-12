// Reading Order: 10101010
//  170
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.state;

import sv.volcan.admin.AdminController;
import sv.volcan.bus.VolcanEventDispatcher;
import sv.volcan.config.VolcanEngineConfig;
import sv.volcan.core.AAACertified;
import sv.volcan.core.VolcanLogger;
import sv.volcan.kernel.EngineKernel;
import sv.volcan.memory.SectorMemoryVault;


/**
 * RESPONSIBILITY: Global Runtime Orchestration and Entry Point.
 * WHY: The engine requires a strict chronological bootstrap sequence to initialize native memory, the event bus, the visual window, and the background kernel thread safely.
 * TECHNIQUE: Single entry point (Main). Redirects I/O to async loggers, initializes off-heap memory vaults, configures multi-lane architecture, and pins the Kernel to a MAX_PRIORITY background thread.
 * GUARANTEES: Clean and predictable bootstrap sequence, isolating the kernel hot-path from AWT/OS thread interruptions.
 *
 * @author Marvin Alexander Flores Canales
 * @since 1.0
 */
@AAACertified(date = "2026-01-08", maxLatencyNs = 200_000_000, notes = "Engine Bootstrapper - Infrastructure Orchestrator")
public final class VolcanEngineMaster {

    public static void main(String[] args) throws java.io.IOException {

        // -------------------------------------------------------------------------
        // (STEP 0 Eliminado: No se intercepta stdout/stderr. Se delega todo a VolcanLogger)
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
        // STEP 1: (REMOVED) Visual Window is now initialized inside EngineKernel.start()
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
        // STEP 2: Original Init (unchanged)
        // -------------------------------------------------------------------------
        // Force VolcanEngineConfig class loading to trigger static block
        // initialization.
        // The static block prints the configuration banner and loads all config
        // settings.
        // Variable appears unused but its purpose is the side effect of class loading.
        @SuppressWarnings("unused")
        String profile = VolcanEngineConfig.getProfile();

        VolcanLogger.info("ENGINE", "VolcanEngine v2.0");
        VolcanLogger.info("ENGINE", "=================");

        // [NEURONA_048 STEP 1] SECTOR MEMORY VAULT (Off-Heap Memory)
        SectorMemoryVault memoryVault = new SectorMemoryVault(1024);

        // [NEURONA_048 STEP 2] EVENT DISPATCHER (Multi-Lane Bus)
        VolcanEventDispatcher dispatcher = VolcanEventDispatcher.createDefault(14);

        // [NEURONA_048 STEP 3] MAIN KERNEL (Central Processor)
        VolcanLogger.info("ENGINE", "Starting kernel...");
        EngineKernel kernel = new EngineKernel(dispatcher, memoryVault);

        // [NEURONA_048 STEP 4] ADMIN CONTROLLER (Control Plane)
        // Start the Control Plane (HTTP Server + Admin Consumer)
        // This DOES NOT block the hot-path, it runs in separate threads
        AdminController.startControlPlane(kernel, memoryVault);

        // [HEADLESS] Native GUI/Audio contexts skipped

        // [NEURONA_048 STEP 5] CONFIGURE SYSTEMS
        configureSystems(kernel, memoryVault);

        // -------------------------------------------------------------------------
        // UI CONTROL WINDOW
        // -------------------------------------------------------------------------
        sv.volcan.ui.ServerControlWindow.open(kernel);

        // -------------------------------------------------------------------------
        // STEP 3: Execute Kernel on Main Thread (MAX_PRIORITY)
        // -------------------------------------------------------------------------
        // By running the Kernel on the Main Thread, we guarantee that GLFW operates
        // directly on the primary OS context (required by macOS and Wayland).
        // The EngineKernel will internally apply Spatial Slicing (Pinning to Core 1)
        // and take absolute control of the OS without yielding.
        Thread.currentThread().setName("volcan-engine-kernel");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        try {
            kernel.start();
        } catch (Throwable t) {
            VolcanLogger.fatal("MASTER", "Fatal Kernel Error", t);
        }
    }

    private static void configureSystems(EngineKernel kernel, SectorMemoryVault memoryVault) {
        VolcanLogger.info("ENGINE", "Configuring User Systems...");
        var registry = kernel.getSystemRegistry();

        // Register Test Systems for Parallel Execution Validation
        registry.registerGameSystem(new sv.volcan.test.SystemExecutionTest());
        registry.registerGameSystem(new sv.volcan.test.SystemDependencyTest());
        registry.registerGameSystem(new sv.volcan.test.SystemParallelismTest());
        
        // [HEADLESS] Input and Audio systems removed

        // [ECS PHASE 30] Register High-Level Scene Kinematics (Runs in parallel via Graph)
        registry.registerGameSystem(new sv.volcan.ecs.SceneKinematicsSystem(kernel.getScene()));
        
        // 5. Physics Broadphase Culling (Data-Oriented Spatial Hash)
        // sv.volcan.physics.BroadphaseSystem broadphase = new sv.volcan.physics.BroadphaseSystem(kernel.getScene());
        // registry.registerGameSystem(broadphase);

        // 6. Physics Narrowphase Solver (Circle/AABB Fast-Paths + Rigidbody Dynamics)
        // sv.volcan.physics.VolcanColliderSoA colliderMemory = new sv.volcan.physics.VolcanColliderSoA(kernel.getScene().getSoA().getCapacity());
        // registry.registerGameSystem(new sv.volcan.physics.NarrowphaseSystem(kernel.getScene(), broadphase.getGrid(), colliderMemory));

        // [HEADLESS] GPU Particles and Skeletal Animation removed

        // 9. Networking & State Replication (Desactivado a petición)
        // sv.volcan.bus.NetworkRingBuffer ringBuffer = new sv.volcan.bus.NetworkRingBuffer(1024, 12);
        // sv.volcan.net.VolcanUdpServer udpServer = new sv.volcan.net.VolcanUdpServer(27020, 12, ringBuffer);
        // sv.volcan.net.NetworkPacketSystem networkIngestion = new sv.volcan.net.NetworkPacketSystem(ringBuffer, kernel.getScene(), 12);
        // sv.volcan.net.NetworkReplicationSystem netReplication = new sv.volcan.net.NetworkReplicationSystem(kernel.getScene());
        // registry.registerGameSystem(networkIngestion);
        // registry.registerGameSystem(netReplication);

        // [FASE 4] Activa el DAG Mode: dispatch elástico sin barreras de layer.
        // Cada sistema se despacha individualmente en cuanto sus dependencias atómicas
        // se satisfacen — 100% CPU core utilization, sin idle time entre layers.
        registry.buildDependencyGraph();
        registry.enableDAGMode();
    }
}
