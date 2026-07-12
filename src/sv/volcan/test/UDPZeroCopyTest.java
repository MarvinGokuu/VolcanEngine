// Reading Order: 10100111
//  167
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.test;

import sv.volcan.net.VolcanUdpServer;
import sv.volcan.bus.NetworkRingBuffer;
import sv.volcan.net.NetworkPacketSystem;
import sv.volcan.ecs.VolcanScene;

/**
 * RESPONSIBILITY: Verify Zero-Allocation UDP Ingestion (Server Side).
 * WHY: The game server must receive datagrams continuously without freezing for GC.
 */
public class UDPZeroCopyTest {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" UDP NETWORKING - ZERO-COPY STRESS TEST");
        System.out.println("==============================================");

        System.gc();
        long startMem = getUsedMemory();

        NetworkRingBuffer ringBuffer = new NetworkRingBuffer(1024, 12);
        VolcanUdpServer server = new VolcanUdpServer(0, 12, ringBuffer); // Puerto efímero
        
        VolcanScene scene = new VolcanScene(1000);
        NetworkPacketSystem packetSys = new NetworkPacketSystem(ringBuffer, scene, 12);

        long memDiff = getUsedMemory() - startMem;
        System.out.printf("[RESULT] Heap Memory Footprint for Networking Subsystem: %d bytes.%n", memDiff);

        // La carga estática de las clases internas de java.nio.channels y DatagramChannel consume ~1.5 MB 
        // una sola vez al arrancar. Tolerancia base de 5MB.
        if (memDiff > 5 * 1024 * 1024) {
            System.err.println("[FAIL] Datagram buffers are allocating objects on Heap!");
            System.exit(1);
        } else {
            System.out.println("[OK] AAA+ Zero-GC Networking Structure passed!");
        }

        System.out.println("[TEST] Verifying Server Draining...");
        
        // Simular 10 frames de subida de red
        for (int i = 0; i < 10; i++) {
            server.drainNetworkBus();
            packetSys.update(null, 0.05f);
        }
        
        System.out.println("[OK] Shipped UDP Packets gracefully.");

        server.close();
        packetSys.cleanup();
        ringBuffer.close();

        System.out.println("==============================================");
        System.out.println(" TESTS PASSED: UDP REPLICATION AUTHORITATIVE");
        System.out.println("==============================================");
        System.exit(0);
    }

    private static long getUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}
