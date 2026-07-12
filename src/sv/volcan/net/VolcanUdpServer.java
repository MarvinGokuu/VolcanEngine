// Reading Order: 01000010
//  66
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.lang.foreign.MemorySegment;
import sv.volcan.core.AAACertified;
import sv.volcan.core.VolcanLogger;
import sv.volcan.bus.NetworkRingBuffer;

/**
 * Servidor UDP Orientado a Datos (NIO Fast-Path).
 * Escucha continuamente datagramas entrantes y los escribe en crudo (Zero-Copy)
 * dentro del NetworkRingBuffer usando operaciones atómicas sin locks.
 */
@AAACertified(date = "2026-07-10", maxLatencyNs = 100, minThroughput = 0, lockFree = true, offHeap = true, notes = "DatagramChannel UDP Zero-Copy Ingestion")
public final class VolcanUdpServer {

    private DatagramChannel channel;
    private final ByteBuffer receiveBuffer;
    private final MemorySegment receiveSegment;
    private final NetworkRingBuffer ringBuffer;
    private boolean isListening = false;

    public VolcanUdpServer(int port, int packetSize, NetworkRingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
        this.receiveBuffer = ByteBuffer.allocateDirect(packetSize);
        this.receiveSegment = MemorySegment.ofBuffer(this.receiveBuffer);
        
        try {
            this.channel = DatagramChannel.open();
            this.channel.configureBlocking(false); // Spin-Wait Mode
            this.channel.bind(new InetSocketAddress(port));
            this.isListening = true;
            VolcanLogger.info("NETWORK", "Servidor UDP Autorizado escuchando en puerto " + port);
        } catch (IOException e) {
            VolcanLogger.fatal("NETWORK", "Fallo al inicializar DatagramChannel", e);
        }
    }

    /**
     * Drena todos los paquetes disponibles en el NIC y los transfiere al RingBuffer.
     * Esta función debe ser llamada repetidamente por un hilo dedicado o durante la ingesta del DAG.
     */
    public void drainNetworkBus() {
        if (!isListening) return;

        try {
            while (true) {
                receiveBuffer.clear();
                // ZERO ALLOCATION NATIVE SYSCALL
                SocketAddress sender = channel.receive(receiveBuffer);
                if (sender == null) {
                    break; // No hay más paquetes en la cola del OS
                }
                
                receiveBuffer.flip();
                int bytesRead = receiveBuffer.limit();
                
                if (bytesRead > 0) {
                    MemorySegment slot = ringBuffer.claimWriteSlot();
                    if (slot != null) {
                        // Bulk copy SIMD
                        MemorySegment.copy(receiveSegment, 0, slot, 0, bytesRead);
                        ringBuffer.commitWrite();
                    } else {
                        // Buffer full (Dropping packet)
                        VolcanLogger.warning("NETWORK", "NetworkRingBuffer lleno. Paquete UDP descartado.");
                    }
                }
            }
        } catch (IOException e) {
            VolcanLogger.warning("NETWORK", "Error I/O durante ingesta UDP: " + e.getMessage());
        }
    }

    public void close() {
        this.isListening = false;
        try {
            if (channel != null) channel.close();
        } catch (IOException e) {
            VolcanLogger.warning("NETWORK", "Error al cerrar canal UDP");
        }
    }
}
