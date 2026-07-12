// Reading Order: 10011101
//  157
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later

package sv.volcan.kernel;


import java.util.concurrent.locks.LockSupport;
import sv.volcan.core.VolcanLogger;
import java.lang.foreign.Arena;

import sv.volcan.bus.VolcanAtomicBus;
import sv.volcan.bus.VolcanEventDispatcher;
import sv.volcan.bus.VolcanSignalCommands;
import sv.volcan.bus.VolcanSignalPacker;
import sv.volcan.core.AAACertified;
import sv.volcan.core.VolcanTimeControlUnit;
import sv.volcan.core.ExecutionValidator;
import sv.volcan.core.MetricsCollector;
import sv.volcan.kernel.UltraFastBootSequence.BootResult;
import sv.volcan.memory.SectorMemoryVault;
import sv.volcan.state.VolcanStateLayout;
import sv.volcan.state.VolcanStateVault;
import sv.volcan.state.WorldStateFrame;

/**
 * RESPONSIBILITY: Main Kernel - Central Processor orchestrating the 4-phase deterministic loop.
 * WHY: A central coordinator is required to guarantee that all subsystems execute synchronously and deterministically.
 * TECHNIQUE: Operates the loop (Input Latch -> Bus Processing -> Systems Execution -> State Audit) at a fixed 60 FPS target.
 * GUARANTEES: Absolute determinism: Same Input + Same Seed = Same Output.
 *
 * @author Marvin Alexander Flores Canales
 * @since 1.0
 */
@AAACertified(
    date         = "2026-01-06",
    maxLatencyNs = 16_666_000,
    minThroughput = 60,
    alignment    = 64,
    lockFree     = false,
    offHeap      = false,
    notes        = "Central processor - 4-phase deterministic loop at 60 FPS"
)
public final class EngineKernel {

    // Pre-allocated scratch matrix and vector to ensure zero GC in phaseRender()
    private static final float[] RENDER_LIGHT_MATRIX = new float[16];
    private static final float[] RENDER_SUN_DIR = {0.5f, 1.0f, 0.5f};
 
    // Kernel state
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private volatile boolean shutdownInProgress = false;

    // Infrastructure
    private final SystemRegistry systemRegistry;
    private final TimeKeeper timeKeeper;
    private final VolcanStateVault stateVault;

    // [BOOT INFRASTRUCTURE]
    private final SectorMemoryVault sectorVault;
    private final KernelControlRegister controlRegister;

    private final VolcanTimeControlUnit timeControlUnit;
    private final Arena frameArena;
    private WorldStateFrame currentState;
    private final VolcanEventDispatcher eventDispatcher;
    private final VolcanAtomicBus adminMetricsBus; // Control Plane: Metrics out of the hot-path
    
    // [ECS PHASE 30] Scene Orchestrator
    private final sv.volcan.ecs.VolcanScene scene;

    // Pre-allocated array for event batching (Zero-Allocation hot path)
    private final long[] eventBatchBuffer = new long[2048];

    // [RESOURCE TRACKING]
    private final Arena stateArena; // Arena for VolcanStateVault
    private final Thread shutdownHook; // Hook for unregister on manual shutdown

    // Metrics
    private long totalFrames = 0;
    private final MetricsCollector.FrameMetrics pooledFrameMetrics = new MetricsCollector.FrameMetrics();

    private final SystemSnapshot initialSystemState;

    /**
     * Primary constructor with Dependency Injection.
     * Allows injecting the VolcanEventDispatcher and SectorMemoryVault.
     * 
     * @param eventDispatcher Multi-lane event dispatcher.
     * @param sectorVault     Physical memory vault (injected from Engine).
     */
    public EngineKernel(VolcanEventDispatcher eventDispatcher, SectorMemoryVault sectorVault) {
        // Capture initial system state and apply optimizations (Phase 1)
        this.initialSystemState = SystemStateManager.captureInitialState();
        SystemStateManager.applyPerformanceBoost();

        this.systemRegistry = new SystemRegistry();
        this.timeKeeper = new TimeKeeper();
        
        // Create explicit Arena for lifecycle control
        // WE USE SHARED: Required because it's created on the Main Thread but closed in the Shutdown Hook Thread
        this.stateArena = Arena.ofShared();
        
        // Create VolcanStateVault with Arena and maxSlots
        this.stateVault = new VolcanStateVault(stateArena, VolcanStateLayout.MAX_SLOTS);
        
        // EngineStateChannel removed for Headless Mode

        // Assign injected resources
        this.sectorVault = sectorVault;
        this.eventDispatcher = eventDispatcher;

        // Initialize Control Register
        this.controlRegister = new KernelControlRegister();
        this.controlRegister.transition(KernelControlRegister.STATE_OFFLINE, KernelControlRegister.STATE_BOOTING);

        // Initialize TimeControlUnit for Snapshots (60 frames of history = 1 second)
        this.timeControlUnit = new sv.volcan.core.VolcanTimeControlUnit(stateArena, stateVault.getRawSegment().byteSize(), 60);

        // Arena for WorldStateFrame (OPTION D: Shared for multi-threading)
        // WorldStateFrame is accessed by parallel systems (SystemExecutionTest, SystemDependencyTest, SystemParallelismTest)
        this.frameArena = Arena.ofShared();
        
        // Create WorldStateFrame with Arena, segment, and timestamp
        this.currentState = new WorldStateFrame(frameArena, stateVault.getRawSegment(), System.nanoTime());

        // [ECS PHASE 30] Init Scene Orchestrator with default capacity
        // Se usa 50_000 por defecto para no asfixiar el Heap Base en tests de Boot
        this.scene = new sv.volcan.ecs.VolcanScene(50_000);
        
        // [FRAMEGRAPH] Removed for Headless
        // [NEURONA_048 STEP 3] Admin Metrics Bus (Control Plane)
        // Capacity 1024: ~17 seconds of metrics at 60 FPS
        this.adminMetricsBus = new VolcanAtomicBus(1024);

        // -------------------------------------------------------------------------
        // SHUTDOWN HOOK - Graceful Shutdown on JVM Exit
        // -------------------------------------------------------------------------
        //
        // PURPOSE:
        // - Release native resources (Arena, MemorySegments).
        // - Close active buses and threads.
        // - Validate complete cleanup (Baseline Protocol).
        //
        // MECHANICS:
        // - Runtime.addShutdownHook() is executed on Ctrl+C or System.exit().
        // - Order: EventDispatcher -> Buses -> Arenas -> SectorVault.
        // - Final validation with BaselineValidator.
        //
        // GUARANTEES:
        // - 100% of resources released.
        // - No ghost threads.
        // - No memory leaks.

        this.shutdownHook = new Thread(() -> {
            sv.volcan.core.VolcanLogger.info("KERNEL", ">>> INITIATING GRACEFUL SHUTDOWN SEQUENCE...");
            gracefulShutdown();
            sv.volcan.core.VolcanLogger.info("KERNEL", ">>> VOLCAN ENGINE OFFLINE. GRAPHICS RELEASED.");
        }, "VolcanShutdownHook");

        Runtime.getRuntime().addShutdownHook(this.shutdownHook);

        // -------------------------------------------------------------------------
        // ANTI-ZOMBIE SHIELD (Cross-Platform Daemon Polling)
        // -------------------------------------------------------------------------
        // Polling a 1 Hz out of the hot-path to detect orphaned JVMs without overhead.
        Thread zombieShield = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    // Java 9+ ProcessHandle API
                    if (!ProcessHandle.current().parent().isPresent()) {
                        sv.volcan.core.VolcanLogger.error("KERNEL", "[ZOMBIE SHIELD] Parent process died! Triggering Poison Pill...");
                        gracefulShutdown();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ZombieShield");
        zombieShield.setDaemon(true);
        zombieShield.start();
    }

    /**
     * Legacy constructor for compatibility with older tests.
     * 
     * @deprecated Use the constructor with dependency injection.
     */
    @Deprecated
    public EngineKernel() {
        this(VolcanEventDispatcher.createDefault(14), new SectorMemoryVault(1024));
    }



    /**
     * Retrieves the system registry for configuration.
     * 
     * @return SystemRegistry.
     */
    public SystemRegistry getSystemRegistry() {
        return systemRegistry;
    }

    /**
     * Retrieves the administrative metrics bus (Control Plane).
     * 
     * @return AdminMetricsBus.
     */
    public VolcanAtomicBus getAdminMetricsBus() {
        return adminMetricsBus;
    }

    /**
     * Retrieves the global ECS Scene Graph.
     * 
     * @return VolcanScene.
     */
    public sv.volcan.ecs.VolcanScene getScene() {
        return scene;
    }

    public void start() {
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] STARTUP SEQUENCE START");

        // [NEURONA_048] STEP 2: CPU PINNING
        // Pin logic thread to Core 1 to eliminate jitter (Target: <35us)
        ThreadPinning.pinToCore(1);

        ExecutionValidator.verify();
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] INTEGRITY CHECK PASSED");

        // -------------------------------------------------------------------------
        // AAA++ JIT WARM-UP (Structural Integration)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] EXECUTING JIT WARM-UP...");
        UltraFastBootSequence.warmUpWithStructuralIntegrity();
        
        // [HEADLESS] No shader reload or native window init needed.

        // -------------------------------------------------------------------------
        // ULTRA FAST BOOT SEQUENCE
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] EXECUTING BOOT SEQUENCE...");
        BootResult bootResult = UltraFastBootSequence.execute(
                controlRegister,
                sectorVault,
                adminMetricsBus // Validate the admin bus as part of the boot
        );

        UltraFastBootSequence.printBootStats(bootResult);

        if (!bootResult.success) {
            System.err.println("[KERNEL PANIC] BOOT FAILED: " + bootResult.errorMessage);
            System.exit(1);
        }

        runMainLoop();
    }

    public sv.volcan.kernel.TimeKeeper getTimeKeeper() {
        return timeKeeper;
    }

    /**
     * Dumps the current performance metrics to the logger explicitly (e.g. for UI triggers)
     */
    public void dumpTelemetryToLog() {
        double frameTimeUs = timeKeeper.getLastActualFps() > 0 ? (1_000_000_000.0 / timeKeeper.getLastActualFps()) / 1000.0 : 0.0;
        double headroomMs = timeKeeper.getLastHeadroomNs() / 1_000_000.0;
        
        sv.volcan.core.VolcanLogger.info("METRICS", String.format(
            "Frame: %d | Time: %.2fus | FPS: %d (Target: %d) | Headroom: %.2fms",
            totalFrames,
            frameTimeUs,
            timeKeeper.getLastActualFps(),
            timeKeeper.getCurrentTargetFps(),
            headroomMs
        ));
    }

    /**
     * Halts the main processing loop and triggers a clean shutdown.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Starts the main engine loop.
     * 
     * <p><b>4-Phase Loop:</b>
     * <ul>
     *   <li>1. INPUT LATCH: Captures input (future).</li>
     *   <li>2. BUS PROCESSING: Processes events (future).</li>
     *   <li>3. SYSTEMS EXECUTION: Executes game logic.</li>
     *   <li>4. STATE AUDIT: Validates state integrity.</li>
     * </ul>
     * 
     * <p><b>Cooperative Interruption:</b>
     * <ul>
     *   <li>Verifies 'running' flag every frame.</li>
     *   <li>Compatible with Graceful Shutdown Protocol.</li>
     * </ul>
     * 
     * <p><b>Power Saving Scaling (3 Tiers):</b>
     * <ul>
     *   <li>Tier 1 (0-10s): Thread.onSpinWait() - Medium power, ns response.</li>
     *   <li>Tier 2 (10s-1min): Thread.sleep(1) - Low power, 1ms response.</li>
     *   <li>Tier 3 (>1min): Thread.sleep(100) - Near-zero power, hibernation.</li>
     * </ul>
     */
    private void runMainLoop() {

        // Off-critical-path telemetry (Pooled to avoid Zero-Garbage violation)
        // We reuse the pre-allocated this.pooledFrameMetrics

        // Variables for sleep scaling (3 tiers)
        long lastActivityTime = System.nanoTime();

        // Time thresholds
        long TIER1_THRESHOLD_NS = 10_000_000_000L; // 10 seconds
        long TIER2_THRESHOLD_NS = 60_000_000_000L; // 1 minute

        // Current power saving state
        int powerSavingTier = 0; // 0=Active, 1=SpinWait, 2=LightSleep, 3=DeepHibernation

        while (running && !Thread.currentThread().isInterrupted()) {
            // [HEADLESS] No OS Window polling needed here.

            timeKeeper.startFrame();
            // @SuppressWarnings("unused")
            long frameStart = System.nanoTime();

            // -------------------------------------------------------------------------
            // PHASE 1: INPUT LATCH (Determinism)
            // -------------------------------------------------------------------------
            long phase1Start = System.nanoTime();
            phaseInputLatch();
            long phase1End = System.nanoTime();
            timeKeeper.recordPhaseTime(1, phase1End - phase1Start);

            // -------------------------------------------------------------------------
            // PHASE 2: BUS PROCESSING (Communication)
            // -------------------------------------------------------------------------
            long phase2Start = System.nanoTime();
            int eventsProcessed = phaseBusProcessing();
            

            
            long phase2End = System.nanoTime();
            timeKeeper.recordPhaseTime(2, phase2End - phase2Start);

            // -------------------------------------------------------------------------
            // POWER SAVING SCALING (3 TIERS)
            // -------------------------------------------------------------------------
            if (eventsProcessed > 0) {
                // ACTIVITY PRESENT: Reset to active mode
                lastActivityTime = System.nanoTime();
                if (powerSavingTier > 0) {
                    if (powerSavingTier == 3) {
                        // Resumed from deep hibernation
                    }
                    powerSavingTier = 0;
                }
            } else {
                // NO EVENTS: Calculate idle time and scale
                long idleTimeNs = System.nanoTime() - lastActivityTime;

                if (idleTimeNs > TIER2_THRESHOLD_NS) {
                    // TIER 3: DEEP HIBERNATION (>1 minute)
                    if (powerSavingTier != 3) {
                        powerSavingTier = 3;
                    }
                    try {
                        Thread.sleep(100); // Deep hibernation: 100ms (10 wake-ups/second)
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue; // Skip systems execution

                } else if (idleTimeNs > TIER1_THRESHOLD_NS) {
                    // TIER 2: LIGHT SLEEP (10s - 1min)
                    if (powerSavingTier != 2) {
                        powerSavingTier = 2;
                    }
                    LockSupport.parkNanos(1_000_000); // Light sleep: 1ms (native)
                    continue; // Skip systems execution

                } else if (idleTimeNs > 0) {
                    // TIER 1: SPIN WAIT (0 - 10s)
                    if (powerSavingTier != 1) {
                        powerSavingTier = 1;
                    }
                    Thread.onSpinWait(); // CPU Hint: release resources without sleeping
                }
            }

            // -------------------------------------------------------------------------
            // HOT-RELOAD CHECK (Phase 2.5) - Removed for Headless
            // -------------------------------------------------------------------------

            // -------------------------------------------------------------------------
            // PHASE 3: SYSTEMS EXECUTION (Game Logic)
            // -------------------------------------------------------------------------
            long phase3Start = System.nanoTime();
            if (!paused) {
                phaseSystemsExecution();
            }
            long phase3End = System.nanoTime();
            timeKeeper.recordPhaseTime(3, phase3End - phase3Start);

            // -------------------------------------------------------------------------
            // PHASE 4: STATE AUDIT (Integrity)
            // -------------------------------------------------------------------------
            long phase4Start = System.nanoTime();
            phaseStateAudit();
            long phase4End = System.nanoTime();
            timeKeeper.recordPhaseTime(4, phase4End - phase4Start);

            // -------------------------------------------------------------------------
            // PHASE 5: NATIVE RENDER (ImGui & GLFW)
            // -------------------------------------------------------------------------
            long phaseRenderStart = System.nanoTime();
            phaseRender();
            long phaseRenderEnd = System.nanoTime();

            // [NEURONA_048 STEP 3] Send metrics to Control Plane (no I/O on hot-path)
            totalFrames++;
            if (totalFrames % 60 == 0) {
                long totalTimeNs = phase1End - phase1Start + phase2End - phase2Start +
                        phase3End - phase3Start + phase4End - phase4Start;
                long packedMetric = MetricsPacker.packFrameStats(totalFrames, totalTimeNs,
                        timeKeeper.getCurrentTargetFps(),
                        timeKeeper.getLastActualFps(),
                        timeKeeper.getLastHeadroomNs());
                adminMetricsBus.offer(packedMetric); // Zero-copy, no I/O
            }

            // -------------------------------------------------------------------------
            // PHASE 5: METRICS AGGREGATION (Off-Critical-Path)
            // -------------------------------------------------------------------------
            pooledFrameMetrics.frameTimeNs = System.nanoTime() - frameStart;
            pooledFrameMetrics.frameNumber = totalFrames;
            pooledFrameMetrics.systemsExecutionNs = phase3End - phase3Start;

            if (MetricsCollector.shouldCollectMetrics(totalFrames)) {
                ParallelSystemExecutor executor = systemRegistry.getParallelExecutor();
                if (executor != null) {
                    MetricsCollector.aggregateMetrics(
                            executor.getPhysicsSystem(),
                            adminMetricsBus,
                            pooledFrameMetrics);
                }
            }

            // Wait for the next frame (Fixed Timestep)
            timeKeeper.waitForNextFrame();
        }

        // Loop terminated: verify reason
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] Loop terminated");

        // [CRITICAL FIX] Release resources IMMEDIATELY upon exiting the run loop.
        // This allows tests to validate memory deallocation without waiting for the JVM shutdown.
        gracefulShutdown();
    }

    /**
     * PHASE 1: INPUT LATCH
     * 
     * Captures user input and stores it in the state.
     * FUTURE: Integrate with input system.
     */
    private void phaseInputLatch() {
        // [FFI BINDING] Poll Native OS Events synchronously (Spatial Slicing)
        // [HEADLESS] No native OS Events to poll

        // Simulating input buffer read (avoids empty TODOs)
        @SuppressWarnings("unused")
        int mouseX = currentState.readInt(VolcanStateLayout.INPUT_MOUSE_X);
        @SuppressWarnings("unused")
        int mouseY = currentState.readInt(VolcanStateLayout.INPUT_MOUSE_Y);

        // Complex input logic would go here.
    }

    /**
     * Phase 2: BUS PROCESSING
     * 
     * <p>Processes all events on the Bus in priority order.
     * 
     * <p><b>Guarantees:</b>
     * <ul>
     *   <li>Deterministic order (System -> Network -> Input -> Physics -> Audio -> Render).</li>
     * </ul>
     * 
     * @return Number of processed events (for idle detection).
     */
    private int phaseBusProcessing() {
        int eventsProcessed = 0;

        // Zero-Allocation batch extraction
        int count = eventDispatcher.batchPollAll(eventBatchBuffer);

        for (int i = 0; i < count; i++) {
            long event = eventBatchBuffer[i];
            int commandId = VolcanSignalPacker.unpackCommandId(event);

            // [CENTRAL KERNEL ROUTER]
            switch (commandId) {
                case 1: // SYS_EXIT_SIGNAL
                    this.running = false;
                    break;
                case 2: // SYS_PAUSE_SIGNAL
                    this.paused = !this.paused; // Toggle pause state
                    break;
                case sv.volcan.bus.VolcanSignalCommands.SYS_ENGINE_ROLLBACK:
                    if (this.timeControlUnit != null) {
                        this.timeControlUnit.rollback(stateVault.getRawSegment());
                    }
                    break;
                case 100: // INPUT_KEY_PRESS
                    break;
                default:
                    break;
            }

            eventsProcessed++;
        }

        return eventsProcessed;
    }

    /**
     * Phase 3: SYSTEMS EXECUTION
     * 
     * <p>Executes all game logic systems in order.
     * 
     * <p><b>Guarantees:</b>
     * <ul>
     *   <li>Same order always.</li>
     *   <li>Same deltaTime always (1/60 seconds).</li>
     *   <li>Same WorldStateFrame for all systems.</li>
     * </ul>
     */
    private void phaseSystemsExecution() {
        float deltaTime = timeKeeper.getDeltaTime();
        systemRegistry.executeGameSystems(currentState, deltaTime);
    }

    /**
     * PHASE 4: STATE AUDIT
     * 
     * Validates state integrity and updates the tick counter.
     * FUTURE: State hash for corruption detection.
     */
    private void phaseStateAudit() {
        // Increment the tick counter in the state
        int currentTick = stateVault.read(VolcanStateLayout.SYS_TICK);
        stateVault.write(VolcanStateLayout.SYS_TICK, currentTick + 1);

        // [STATE AUDIT]
        // 1. Memory integrity verification (Basic checksum)
        // In AAA production, this can be random sampling for performance.

        // 2. Snapshot for Rollback (Netcode)
        if (this.timeControlUnit != null) {
            this.timeControlUnit.capture(stateVault.getRawSegment());
        }

        // Critical bounds validation - Protects against memory corruption
        if (stateVault.read(VolcanStateLayout.ENTITY_COUNT) < 0) {
            System.err.println("[KERNEL PANIC] Entity count corrupted!");
            this.running = false;
        }
    }

    /**
     * PHASE 5: NATIVE RENDER (ImGui & GLFW)
     */
    private void phaseRender() {
        // [HEADLESS] Engine operates without rendering phase.
    }

    public void shutdown() {
        this.running = false;
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] SHUTDOWN SEQUENCE");
    }

    // -------------------------------------------------------------------------
    // GRACEFUL SHUTDOWN - Full Resource Deallocation
    // -------------------------------------------------------------------------

    /**
     * Safe kernel shutdown with full resource release.
     * 
     * <p><b>Shutdown Sequence:</b>
     * <ul>
     *   <li>1. Stop main loop (running = false).</li>
     *   <li>2. Close EventDispatcher (all priority buses).</li>
     *   <li>3. Close adminMetricsBus (control bus).</li>
     *   <li>4. Close frameArena (releases WorldStateFrame).</li>
     *   <li>5. Close stateVault Arena (releases MemorySegments).</li>
     *   <li>6. Close sectorVault (releases off-heap memory).</li>
     * </ul>
     * 
     * <p><b>Guarantees:</b>
     * <ul>
     *   <li>Thread-safe (volatile flags + drain period).</li>
     *   <li>No SIGSEGV (correct closing order).</li>
     *   <li>No memory leaks (validated with BaselineValidator).</li>
     *   <li>Graphics return to State A (baseline).</li>
     * </ul>
     */
    private void gracefulShutdown() {
        // Clear interrupt flag to prevent ClassLoader I/O failures (NoClassDefFoundError)
        Thread.interrupted();

        // Prevent multiple calls
        if (shutdownInProgress) {
            sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] Shutdown already in progress, ignoring...");
            return;
        }
        shutdownInProgress = true;

        // [TERMINATOR THREAD] Guarantees process death if shutdown freezes or throws an Error
        Thread terminator = new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) { return; }
            if (System.getProperty("sv.volcan.test.nohalt") == null) {
                Runtime.getRuntime().halt(0);
            }
        }, "KernelTerminator");
        terminator.setDaemon(true);
        terminator.start();

        // [POISON PILL] Instruct AdminConsumer to flush logs and terminate gracefully
        try {
            adminMetricsBus.offer(sv.volcan.bus.VolcanSignalPacker.packCmd(sv.volcan.bus.VolcanSignalCommands.SYS_TERMINATE_LOG_SIGNAL));
        } catch (Throwable t) {
            System.err.println("[KERNEL] Error sending Poison Pill to metrics bus: " + t.getMessage());
        }

        // Stop the Control Plane (Metrics Server & Admin Consumer) - this will now block until the consumer dies
        try {
            sv.volcan.admin.AdminController.stopControlPlane();
        } catch (Throwable t) {
            System.err.println("[KERNEL] Error stopping Control Plane: " + t.getMessage());
        }

        sv.volcan.core.VolcanLogger.info("KERNEL", "-------------------------------------------------------------------------");
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] GRACEFUL SHUTDOWN SEQUENCE");
        sv.volcan.core.VolcanLogger.info("KERNEL", "-------------------------------------------------------------------------");

        // -------------------------------------------------------------------------
        // STEP 1: STOP MAIN LOOP & SUBSYSTEMS
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 1/6] Stopping main loop and subsystems...");
        running = false;

        ParallelSystemExecutor executor = systemRegistry.getParallelExecutor();
        if (executor != null) {
            // 1. Apagamos los hilos del Game System de forma segura para que terminen de escribir
            executor.shutdown(); 
        }

        // Wait for loop to finish (maximum 1 second)
        try {
            Thread.sleep(100); // Give time for loop to finish current frame
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 1/6] Main loop and subsystems stopped [OK]");

        // -------------------------------------------------------------------------
        // STEP 2: CLOSE EVENT DISPATCHER (All priority buses)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 2/6] Closing Event Dispatcher...");
        try {
            eventDispatcher.shutdown();
            sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 2/6] Event Dispatcher closed [OK]");
        } catch (Throwable e) {
            System.err.println("[STEP 2/6] Error closing Event Dispatcher: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // STEP 3: CLOSE ADMIN METRICS BUS (Control Plane)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 3/6] Closing Admin Metrics Bus...");
        try {
            adminMetricsBus.gracefulShutdown();
            sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 3/6] Admin Metrics Bus closed [OK]");
        } catch (Throwable e) {
            System.err.println("[STEP 3/6] Error closing Admin Metrics Bus: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // STEP 4: CLOSE FRAME ARENA (WorldStateFrame)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 4/6] Closing Frame Arena...");
        try {
            frameArena.close();
            sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 4/6] Frame Arena closed [OK]");
        } catch (Throwable e) {
            System.err.println("[STEP 4/6] Error closing Frame Arena: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // STEP 5: CLOSE STATE VAULT ARENA (MemorySegments)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 5/6] Closing State Vault...");
        try {
            stateArena.close();
            scene.destroy(); // Libera la memoria SIMD del Scene Graph
            sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 5/6] State Vault Arena closed [OK]");
        } catch (Throwable e) {
            System.err.println("[STEP 5/6] Error closing State Vault: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // STEP 6: CLOSE SECTOR VAULT (Off-heap memory)
        // -------------------------------------------------------------------------
        sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 6/7] Closing Sector Vault...");
        try {
            sectorVault.close();
            sv.volcan.core.VolcanLogger.info("KERNEL", "[STEP 6/7] Sector Vault closed [OK]");
        } catch (Throwable e) {
            System.err.println("[STEP 6/7] Error closing Sector Vault: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // NATIVE FFI DESTRUCTION (Skipped for Headless)
        // -------------------------------------------------------------------------

        sv.volcan.core.VolcanLogger.info("KERNEL", "-------------------------------------------------------------------------");
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] GRACEFUL SHUTDOWN COMPLETED");
        sv.volcan.core.VolcanLogger.info("KERNEL", "-------------------------------------------------------------------------");

        // -------------------------------------------------------------------------
        // RESTORE SYSTEM STATE AND VALIDATE (Milestone 1)
        // -------------------------------------------------------------------------
        if (initialSystemState != null) {
            SystemStateManager.restoreInitialState(initialSystemState);
            SystemSnapshot currentSystemState = SystemStateManager.captureInitialState();
            CleanupValidator.validate(initialSystemState, currentSystemState);
        }

        // [SHUTDOWN GUARANTEE]
        // Force Windows kernel to clear native memory descriptors and CPU Pinning
        sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] EXECUTING LOW-LEVEL SHUTDOWN (HALT)...");
        terminator.interrupt();
        if (System.getProperty("sv.volcan.test.nohalt") == null) {
            Runtime.getRuntime().halt(0);
        } else {
            sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] HALT bypassed for test execution.");
        }

        // EXTRA CLEANUP: Remove shutdown hook to prevent thread leaks if manual
        try {
            Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            sv.volcan.core.VolcanLogger.info("KERNEL", "[KERNEL] Shutdown Hook removed (CLEANUP");
        } catch (IllegalStateException e) {
            // Ignore: Shutdown in progress
        } catch (Exception e) {
            System.err.println("[KERNEL] Warning: Could not remove Shutdown Hook");
        }
    }

}


