// Reading Order: 01000101
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.net;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import sv.volcan.core.AAACertified;
import sv.volcan.core.systems.GameSystem;
import sv.volcan.state.WorldStateFrame;
import sv.volcan.bus.NetworkRingBuffer;
import sv.volcan.ecs.VolcanScene;

/**
 * Worker del DAG (Fase 3) encargado de procesar la ingesta asíncrona UDP.
 * Drena el NetworkRingBuffer y actualiza el estado de las entidades en la escena
 * mediante operaciones Vectoriales en Off-Heap (Struct-of-Arrays).
 */
@AAACertified(date = "2026-07-10", maxLatencyNs = 50, minThroughput = 0, lockFree = true, offHeap = true, notes = "DAG Worker: UDP Packet Decoder")
public final class NetworkPacketSystem implements GameSystem {

    private final NetworkRingBuffer ringBuffer;
    private final VolcanScene scene;
    private final MemorySegment packetBuffer;
    private final Arena arena;
    private final int packetSize;

    public NetworkPacketSystem(NetworkRingBuffer ringBuffer, VolcanScene scene, int packetSize) {
        this.ringBuffer = ringBuffer;
        this.scene = scene;
        this.packetSize = packetSize;
        this.arena = Arena.ofShared();
        this.packetBuffer = arena.allocate(packetSize);
    }

    @Override
    public void update(WorldStateFrame state, float deltaTime) {
        // Drenar todos los paquetes pendientes del Ring Buffer (Lock-Free Loop)
        while (ringBuffer.poll(packetBuffer)) {
            processPacket(packetBuffer);
        }
    }

    /**
     * Decodifica un payload crudo.
     * Estructura esperada (12 Bytes): [ID: Int32] [InputX: Float32] [InputY: Float32]
     */
    private void processPacket(MemorySegment packet) {
        int entityId = packet.get(ValueLayout.JAVA_INT, 0);
        float inX = packet.get(ValueLayout.JAVA_FLOAT, 4);
        float inY = packet.get(ValueLayout.JAVA_FLOAT, 8);
        
        // Validación básica (No confiar en inputs de cliente)
        if (entityId >= 0 && entityId < scene.getSoA().getCapacity()) {
            var tSoa = scene.getSoA();
            
            // Integración de input crudo usando VarHandles Off-Heap
            // En un caso real esto alteraría la velocidad, no la posición directa.
            double currentX = (double) tSoa.globalPosX.get(ValueLayout.JAVA_DOUBLE, entityId * 8L);
            double currentY = (double) tSoa.globalPosY.get(ValueLayout.JAVA_DOUBLE, entityId * 8L);
            
            tSoa.globalPosX.set(ValueLayout.JAVA_DOUBLE, entityId * 8L, currentX + inX);
            tSoa.globalPosY.set(ValueLayout.JAVA_DOUBLE, entityId * 8L, currentY + inY);
        }
    }

    @Override
    public String getName() {
        return "NetworkPacketSystem";
    }

    public void cleanup() {
        arena.close();
    }
}
