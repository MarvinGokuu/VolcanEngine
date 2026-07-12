// Reading Order: 00011001
//  25
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later

package sv.volcan.bus;

import sv.volcan.core.AAACertified;
import sv.volcan.core.VolcanLogger;

/**
 * Central Event Orchestrator and Multi-Lane Dispatcher.
 *
 * <p>Main facade for the event system. Manages multiple specialized lanes 
 * for different traffic types (Network, Physics, System, etc.) with independent
 * backpressure strategies.
 * 
 * <p>MECHANICAL SYMPATHY: Uses direct array indexing (O(1)) instead of HashMaps 
 * to achieve zero-allocation routing and eliminate L1 cache misses.
 * 
 * @author Marvin Alexander Flores Canales
 * @since 1.0
 */
@AAACertified(
    date         = "2026-01-05",
    maxLatencyNs = 50,
    minThroughput = 5_000_000,
    alignment    = 64,
    lockFree     = true,
    offHeap      = false,
    notes        = "Zero-Allocation Dispatch. O(1) Array Routing instead of HashMap"
)
public final class VolcanEventDispatcher {

    // Mechanical Sympathy: Direct array mapping based on Enum Ordinal
    private final VolcanEventLane[] laneArray;
    
    // Priority order for sequential processing
    private static final VolcanEventType[] PRIORITY_ORDER = {
        VolcanEventType.SYSTEM,
        VolcanEventType.NETWORK,
        VolcanEventType.INPUT,
        VolcanEventType.PHYSICS,
        VolcanEventType.AUDIO,
        VolcanEventType.RENDER
    };

    /**
     * Creates an empty dispatcher.
     * Lanes must be registered manually with registerLane().
     */
    public VolcanEventDispatcher() {
        this.laneArray = new VolcanEventLane[VolcanEventType.cachedValues().length];
    }

    /**
     * Creates a dispatcher with predefined lanes.
     * 
     * @param busSize Size of each bus (power of 2).
     * @return Fully configured dispatcher.
     */
    public static VolcanEventDispatcher createDefault(int busSize) {
        VolcanEventDispatcher dispatcher = new VolcanEventDispatcher();

        // Input Lane: DROP (high frequency, non-critical)
        dispatcher.registerLane(
                VolcanEventType.INPUT,
                new VolcanRingBus(busSize),
                BackpressureStrategy.DROP);

        // Network Lane: BLOCK (critical, must not be lost)
        dispatcher.registerLane(
                VolcanEventType.NETWORK,
                new VolcanRingBus(busSize),
                BackpressureStrategy.BLOCK);

        // System Lane: BLOCK (critical engine events)
        dispatcher.registerLane(
                VolcanEventType.SYSTEM,
                new VolcanRingBus(busSize),
                BackpressureStrategy.BLOCK);

        // Audio Lane: DROP (non-critical)
        dispatcher.registerLane(
                VolcanEventType.AUDIO,
                new VolcanRingBus(busSize),
                BackpressureStrategy.DROP);

        // Physics Lane: OVERWRITE (only most recent state matters)
        dispatcher.registerLane(
                VolcanEventType.PHYSICS,
                new VolcanRingBus(busSize),
                BackpressureStrategy.OVERWRITE);

        // Render Lane: DROP (visual events, non-critical)
        dispatcher.registerLane(
                VolcanEventType.RENDER,
                new VolcanRingBus(busSize),
                BackpressureStrategy.DROP);

        return dispatcher;
    }

    // -------------------------------------------------------------------------
    // LANE REGISTRATION
    // -------------------------------------------------------------------------

    /**
     * Registers a specialized lane using O(1) ordinal mapping.
     * 
     * @param type     Event type (determines array index).
     * @param bus      Bus implementation.
     * @param strategy Backpressure strategy.
     */
    public void registerLane(VolcanEventType type, IEventBus bus, BackpressureStrategy strategy) {
        VolcanEventLane lane = new VolcanEventLane(type.name(), type, bus, strategy);
        laneArray[type.ordinal()] = lane;
    }

    /**
     * Retrieves a lane by event type.
     * 
     * @param type Event type.
     * @return Lane or null if it doesn't exist.
     */
    public VolcanEventLane getLane(VolcanEventType type) {
        return laneArray[type.ordinal()];
    }

    // -------------------------------------------------------------------------
    // EVENT DISPATCH
    // -------------------------------------------------------------------------

    /**
     * Dispatches an event to a specific lane via O(1) lookup.
     * 
     * @param type  Event type (target lane).
     * @param event Encoded event.
     * @return true if accepted.
     */
    public boolean dispatch(VolcanEventType type, long event) {
        VolcanEventLane lane = laneArray[type.ordinal()];
        if (lane == null) {
            return false;
        }
        return lane.offer(event);
    }

    /**
     * Automatically routes an event by extracting its type from the command ID.
     * 
     * @param event Encoded event (must include type in command ID).
     * @return true if accepted.
     */
    public boolean dispatchAuto(long event) {
        int commandId = VolcanSignalPacker.unpackCommandId(event);
        VolcanEventType type = VolcanEventType.fromCommandId(commandId);
        
        VolcanEventLane lane = laneArray[type.ordinal()];
        if (lane != null) {
            return lane.offer(event);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // EVENT PROCESSING
    // -------------------------------------------------------------------------



    public int batchPollAll(long[] buffer) {
        int total = 0;

        for (int i= 0; i< PRIORITY_ORDER.length; i++) {
            VolcanEventType type = PRIORITY_ORDER[i];
            VolcanEventLane lane = laneArray[type.ordinal()];
            if (lane != null) {
                int remaining = buffer.length - total;
                if (remaining == 0) break;
                
                long event;
                while (total < buffer.length && (event = lane.poll()) != -1) {
                    buffer[total++] = event;
                }
            }
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // OBSERVABILITY
    // -------------------------------------------------------------------------

    /**
     * Prints the status of all active lanes.
     */
    public void printStatus() {
        VolcanLogger.info("DISPATCHER", "═══ VOLCAN EVENT DISPATCHER - STATUS REPORT ═══");
        for (int i= 0; i< laneArray.length; i++) {
            VolcanEventLane lane = laneArray[i];
            if (lane != null) {
                VolcanLogger.info("DISPATCHER", lane.getStatusReport());
            }
        }
        VolcanLogger.info("DISPATCHER", "═══════════════════════════════════════════════");
    }

    /**
     * Clears all active lanes.
     */
    public void clearAll() {
        for (int i= 0; i< laneArray.length; i++) {
            VolcanEventLane lane = laneArray[i];
            if (lane != null) {
                lane.clear();
            }
        }
    }

    /**
     * Returns the total number of pending events across all lanes.
     * 
     * @return Sum of pending events.
     */
    public int getTotalPendingEvents() {
        int total = 0;
        for (int i= 0; i< laneArray.length; i++) {
            VolcanEventLane lane = laneArray[i];
            if (lane != null) {
                total += lane.size();
            }
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // GRACEFUL SHUTDOWN
    // -------------------------------------------------------------------------

    /**
     * Safe shutdown of the dispatcher and underlying buses.
     * 
     * <p>PURPOSE:
     * - Close all priority buses safely.
     * - Inject Tombstone Events.
     * - Release bus references.
     */
    public void shutdown() {
        VolcanLogger.info("DISPATCHER", "Initiating shutdown for all lanes...");

        int pendingEvents = getTotalPendingEvents();
        if (pendingEvents > 0) {
            VolcanLogger.warning("DISPATCHER", "WARNING: " + pendingEvents + " pending events at shutdown");
        }

        // Close in reverse priority order
        for (int i= PRIORITY_ORDER.length - 1; i>= 0; i--) {
            VolcanEventType type = PRIORITY_ORDER[i];
            VolcanEventLane lane = laneArray[type.ordinal()];
            
            if (lane != null) {
                IEventBus bus = lane.getBus();
                if (bus instanceof VolcanAtomicBus) {
                    ((VolcanAtomicBus) bus).gracefulShutdown();
                } else if (bus instanceof VolcanRingBus) {
                    ((VolcanRingBus) bus).gracefulShutdown();
                } else {
                    bus.clear();
                }
                
                // Release reference
                laneArray[type.ordinal()] = null;
            }
        }

        VolcanLogger.info("DISPATCHER", "Shutdown completed");
    }
}
