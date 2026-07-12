# 🗺️ Mapa del Flujo de Físicas y Orquestación DAG (Fase 4)

Este diagrama documenta la arquitectura de ejecución asíncrona del VolcanEngine (Headless Backend). El motor no usa barreras globales ni pausas. Utiliza un Grafo Acíclico Dirigido (DAG) para disparar hilos trabajadores (Worker Threads) elásticamente en cuanto sus dependencias de datos (Data-Oriented) son resueltas.



```mermaid
graph TD
    %% Inicialización
    subgraph init [1. Arranque del Tick]
        A(("TimeKeeper<br/>Tick Delta")) --> B{"VolcanTaskDispatcher<br/>Dual MPMC Ring Buffer"}
        B -->|Canal A| B1[workerQueue]
        B -->|Canal B| B2[networkThreadQueue]
    end

    %% Tareas Paralelas sin Dependencias (Se ejecutan al mismo tiempo en N Cores)
    subgraph parallel [2. Ingesta Inicial (Workers)]
        B1 --> C[NetworkPacketSystem]
        B1 --> D[SystemStateMonitor]
    end

    %% Tareas Secuenciales Dependientes (Físicas)
    subgraph physics [3. Pipeline de Físicas CPU SIMD]
        C --> E[BroadphaseSystem<br/>Spatial Hash CPU]
        E --> F[NarrowphaseSystem]
        F -->|Calcula colisiones| G[SceneKinematicsSystem]
        G -->|Integra Posición| H[(VolcanTransformSoA)]
    end

    %% Tareas con Afinidad de Hilo (Network I/O)
    subgraph netio [4. Telemetría y Egresos]
        B2 -.->|Afinidad NIO| I["MetricTelemetrySystem<br/>Async Export"]
        B2 -.->|Afinidad NIO| J["StateBroadcastSystem"]
    end

    %% Sincronización Final
    subgraph sync [5. Cierre de Estado]
        H --> K{Sincronización Lock-Free}
        I --> K
        J --> K
        K --> L((Fin del Tick Físico))
    end

    %% Estilos AAA
    classDef dispatcher fill:#c0392b,stroke:#e74c3c,stroke-width:3px,color:#fff;
    classDef worker fill:#2980b9,stroke:#3498db,stroke-width:2px,color:#fff;
    classDef physics fill:#27ae60,stroke:#2ecc71,stroke-width:2px,color:#fff;
    classDef memory fill:#8e44ad,stroke:#9b59b6,stroke-width:4px,color:#fff;
    classDef netio fill:#e67e22,stroke:#d35400,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;
    classDef queue fill:#d35400,stroke:#e67e22,stroke-width:2px,color:#fff;

    class B,K dispatcher;
    class B1,B2 queue;
    class C,D,E worker;
    class F,G physics;
    class H memory;
    class I,J netio;
```



## Leyenda Técnica:
*   **VolcanTaskDispatcher (Dual Ring Buffer):** El "cerebro" multicore. Implementa dos arreglos MPMC lock-free (Vyukov Bounded Queues) con un Stride de 64-bytes. 
*   **workerQueue:** Cola Lock-Free de tareas agnósticas (Físicas, Transformaciones) consumidas por los *Workers* paralelos de la CPU.
*   **networkThreadQueue (Replaces mainThreadQueue):** Cola de tareas que obligatoriamente requieren afinidad para I/O (Exportación de telemetría y broadcast UDP). Anteriormente usada para gráficos OpenGL, ahora enfocada en sockets no bloqueantes.
*   **Broadphase / Narrowphase:** Físicas ejecutadas 100% en CPU a través de SIMD. Desvinculadas de cualquier dependencia de Compute Shaders.
*   **VolcanTransformSoA:** Memoria final en arreglos primitivos alineados a 64 bytes que sirve de base para armar el paquete binario de red.
