# 01 - ZERO-COPY UDP NETWORKING

## 1. Visión General
Como motor de Backend Headless, VolcanEngine está diseñado para comunicarse con miles de clientes simultáneamente. En lugar de utilizar TCP (que sufre de Head-of-Line Blocking), el motor utiliza datagramas UDP procesados mediante NIO (`AsynchronousSocketChannel`) y mapeados directamente a `MemorySegment` sin instanciar objetos intermedios.

## 2. Ingesta Asíncrona (NIO)
El hilo de red escucha en un puerto UDP dedicado (ej. 13001). Cuando el OS (epoll/IOCP) recibe un paquete, lo copia directamente a un Ring Buffer Atómico pre-asignado. 
El `VolcanTaskDispatcher` luego dispara *Workers* paralelos para procesar estas colisiones de red.

## 3. Filosofía Zero-Copy
- Nunca se hace `new byte[]` al recibir un paquete.
- El paquete decodificado modifica el `VolcanTransformSoA` usando `VarHandle` (Memory Fences).
- La latencia objetivo desde la llegada al NIC hasta la integración física es `< 1ms`.
