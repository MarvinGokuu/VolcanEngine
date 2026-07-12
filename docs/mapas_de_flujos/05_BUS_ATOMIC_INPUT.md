# 🗺️ Mapa del Bus de Entrada Atómico (Capa 2: Orquestación Network)

Para garantizar que el motor (Server Backend) nunca pierda la llegada de un paquete UDP de un cliente conectado, el sistema de ingesta debe ser Lock-Free (sin cerrojos). Si la tarjeta de red (NIC) notifica un paquete mientras el motor está procesando físicas, bloquear el hilo principal causaría *stuttering* masivo a todos los clientes.

Por eso, VolcanEngine utiliza un Bus de Señales de Red implementado con operaciones atómicas (`AtomicInteger`, `VarHandle`).



```mermaid
graph TD
    subgraph os_thread [Hilo de I/O Asíncrono - NIO]
        A("AsynchronousSocketChannel<br/>UDP Datagrams") -->|Genera Evento| B{Ring Buffer Atómico}
    end

    subgraph game_thread [Hilo Lógico del Servidor]
        B -->|Shadow Buffer Latch<br/>MemorySegment.copy| C[VolcanInputSystem]
        C -->|Procesa y Despacha| D[VolcanSignalCommands]
        D -->|Señal Atómica| E("VolcanEngineMaster<br/>Acción Ejecutada")
    end

    %% Estilos AAA
    classDef os fill:#1e1e1e,stroke:#3498db,stroke-width:2px,color:#fff;
    classDef buffer fill:#27ae60,stroke:#2ecc71,stroke-width:4px,color:#fff;
    classDef game fill:#8e44ad,stroke:#9b59b6,stroke-width:2px,color:#fff;

    class A os;
    class B buffer;
    class C,D,E game;
```



## Leyenda Técnica:
*   **Ring Buffer Atómico (Memory Visibility):** Un arreglo circular en memoria que permite a un hilo escribir (Network NIO) y a otro leer (Simulación) al mismo tiempo sin colisionar ni trabarse. Funciona bajo el principio de semántica de memoria *Volatile*. Todo el acceso al arreglo se gestiona explícitamente mediante *Memory Fences* usando `VarHandle.setRelease` y `getAcquire`, aniquilando las condiciones de carrera (Data Races) o lecturas fantasma.
*   **AsynchronousSocketChannel:** El canal NIO de Java que Windows/Linux dispara instantáneamente (a través de epoll/IOCP) cada vez que el servidor recibe una trama de red.
*   **Shadow Buffer Latch:** El hilo central copia masivamente (vía SIMD Vectorizado `MemorySegment.copy`) el estado del buffer directamente a Off-Heap. Evita todo bloqueo (Zero-Contention) contra los hilos del OS que procesan la red.
