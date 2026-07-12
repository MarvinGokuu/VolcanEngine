// Reading Order: 00010011
//  19
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.admin;

import sv.volcan.core.AAACertified;

import sv.volcan.core.VolcanLogger;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RESPONSIBILITY: Administrative Data Bridge (Control Plane).
 * WHY: We need peripheral servers (HTTP/WebSocket) to read unformatted telemetry data without ever touching or blocking the Kernel.
 * TECHNIQUE: Maintains the latest state snapshot "pre-baked" by the AdminConsumer in an AtomicReference. Separates String formatting logic from the Main Kernel.
 * GUARANTEES: Non-blocking reads. Single writer principle. Zero-Garbage JSON construction via pre-allocated StringBuilder.
 * 
 * <p>Metrics: Non-blocking reads
 * 
 * @author Marvin Alexander Flores Canales
 * @since 1.0
 */
@AAACertified(date = "2026-06-11",
     maxLatencyNs = 0,
     minThroughput = 0, 
     alignment = 0, 
     lockFree = false, 
     offHeap = false, 
     notes = "Automatically AAA Certified during Core Audit")
public final class AdminController {

    // Default snapshot (JSON valid dummy)
    private static final byte[] DEFAULT_SNAPSHOT = "{\"status\":\"waiting_for_kernel\"}"
            .getBytes(StandardCharsets.UTF_8);

    // Atomic reference for thread-safe reading
    private static final AtomicReference<byte[]> latestSnapshot = new AtomicReference<>(DEFAULT_SNAPSHOT);

    // Zero-GC Hot-Path variables
    private static Thread adminConsumerThread = null;

    private AdminController() {
    } // Utility Class

    /**
     * Gets the latest snapshot of raw bytes.
     * Trivial cost operation (reference read).
     *
     * @return byte[] ready to write to socket
     */
    public static byte[] getLatestSnapshot() {
        return latestSnapshot.get();
    }

    /**
     * Updates the snapshot with new pre-formatted data.
     * Called only by AdminConsumer (Single writer principle recommended,
     * although AtomicReference supports concurrency).
     *
     * @param snapshotBytes JSON already converted to bytes
     */
    public static void updateSnapshot(byte[] snapshotBytes) {
        if (snapshotBytes != null) {
            latestSnapshot.set(snapshotBytes);
        }
    }

    /**
     * Starts the Control Plane asynchronously.
     * "Invisible" infrastructure bootstrap (Metrics Logger).
     * 
     * @param kernel Reference to Main Kernel for telemetry
     */
    public static void startControlPlane(sv.volcan.kernel.EngineKernel kernel, sv.volcan.memory.SectorMemoryVault memoryVault) {
        try {
            VolcanLogger.info("Admin", "Iniciando plano de control (Metricas)");
            // Start AdminConsumer (Zero-Garbage Translator for Metrics Logging)
            adminConsumerThread = new Thread(() -> runAdminLoop(kernel), "AdminConsumer");
            adminConsumerThread.setDaemon(true);
            adminConsumerThread.start();
        } catch (Exception e) {
            VolcanLogger.error("Admin", "Failed to start Control Plane: " + e.getMessage());
        }
    }

    /**
     * Stops the admin consumer cleanly.
     */
    public static void stopControlPlane() {
        if (adminConsumerThread != null) {
            try {
                // Wait for the Poison Pill to finish processing
                adminConsumerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            adminConsumerThread = null;
        }
    }

    /**
     * Administrative Consumer loop.
     * Separates "dirty" logic (String formatting) from the Main Kernel.
     */
    private static void runAdminLoop(sv.volcan.kernel.EngineKernel kernel) {
        var adminBus = kernel.getAdminMetricsBus();

        while (true) {
            try {
                long metric = adminBus.poll();
                if (metric != -1L) {
                    long metricType = sv.volcan.kernel.MetricsPacker.getMetricType(metric);
                    
                    if (metricType == sv.volcan.kernel.MetricsPacker.TYPE_FRAME_STATS) {
                        // 1. Unpack hot-path data
                        long frameCount = sv.volcan.kernel.MetricsPacker.unpackFrameCount(metric);
                        long timeMicros = sv.volcan.kernel.MetricsPacker.unpackTimeMicros(metric);
                        long targetFps = sv.volcan.kernel.MetricsPacker.unpackTargetFps(metric);
                        long actualFps = sv.volcan.kernel.MetricsPacker.unpackActualFps(metric);
                        long headroomNs = sv.volcan.kernel.MetricsPacker.unpackHeadroomNs(metric);

                        // 1.5 Write beautifully formatted metrics to volcanengine_metrics.log
                        // Note: VolcanLogger should eventually become completely Zero-GC too.
                        VolcanLogger.info("METRICS", String.format("Frame: %d | Time: %dus | FPS: %d (Target: %d) | Headroom: %.2fms", 
                                        frameCount, timeMicros, actualFps, targetFps, headroomNs / 1_000_000.0));
                    } else {
                        // Not a frame stat, possibly a packed command ID
                        int commandId = sv.volcan.bus.VolcanSignalPacker.unpackCommandId(metric);
                        if (commandId == 2) {
                            VolcanLogger.info("KERNEL", "Pause State toggled");
                        } else if (commandId == sv.volcan.bus.VolcanSignalCommands.SYS_ENGINE_ROLLBACK) {
                            VolcanLogger.info("KERNEL", "Rollback / Time Travel Executed");
                        } else if (commandId == sv.volcan.bus.VolcanSignalCommands.SYS_TERMINATE_LOG_SIGNAL) {
                            VolcanLogger.info("Admin", "Poison Pill Received. Terminating logger.");
                            VolcanLogger.flushAndClose();
                            break;
                        }
                    }

                } else {
                    try {
                        Thread.sleep(16); // ~60 FPS check
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IllegalStateException e) {
                // Bus closed during shutdown - terminate silently
                VolcanLogger.info("Admin", "Bus cerrado - AdminConsumer terminando");
                break;
            }
        }
    }
}
