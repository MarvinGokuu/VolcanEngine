# 🗺️ Mapa del Flujo de Memoria Zero-GC (Capa 1: Cimientos)

Este mapa arquitectónico documenta la filosofía de memoria estricta de VolcanEngine (Headless Backend). Para evitar micro-pausas (stutters) producidas por el Recolector de Basura (Garbage Collector) de la JVM, el motor secuestra bloques directos de RAM (Off-Heap) utilizando Project Panama (`MemorySegment`, `Arena`).



```mermaid
graph TD
    subgraph jvm [Mundo Lento: Java Heap GC]
        A(Variables Temporales Locales)
        B(Strings Cortos y Logs)
        C(VolcanEngineMaster Boot)
        Z["VolcanScene / VolcanEntity Pool<br/>(Pre-Alocados - Zero GC)"]
    end

    subgraph offheap [Mundo Rápido: Memoria Off-Heap Panama]
        D[("SectorMemoryVault<br/>Memoria Compartida Global")]
        E[("VolcanTransformSoA<br/>Arreglos Primitivos Alineados")]
        F[("Network Ring Buffers<br/>UDP Zero-Copy Packets")]
    end

    subgraph hardware [Capa de Hardware]
        G(CPU L1/L2 Cache)
        H(NIC Network Interface)
    end

    %% Flujos de Asignación y Copia
    C -->|Asigna| D
    C -->|Asigna| E
    C -->|Asigna| F
    C -->|Pre-Aloca al inicio| Z

    %% Flujo OOP Facade
    Z == Punteros VarHandles ==> E

    %% Flujo Cero Copias Red
    F == Zero-Copy NIO ==> H
    
    %% ECS SoA a Caché
    E -->|Lectura Secuencial Perfecta| G

    %% Estilos AAA
    classDef slow fill:#e74c3c,stroke:#c0392b,stroke-width:2px,color:#fff;
    classDef fast fill:#2980b9,stroke:#3498db,stroke-width:2px,color:#fff;
    classDef hardware fill:#f39c12,stroke:#d35400,stroke-width:3px,color:#fff;
    classDef prealloc fill:#27ae60,stroke:#2ecc71,stroke-width:2px,color:#fff;

    class A,B,C slow;
    class Z prealloc;
    class D,E,F fast;
    class G,H hardware;
```



## Leyenda Técnica:
*   **SectorMemoryVault:** El cofre central del estado de la simulación. No guarda objetos Java (`new Object()`), guarda variables atómicas primitivas en memoria nativa C-like.
*   **Network Ring Buffers:** Lee paquetes UDP directos de la tarjeta de red (NIC) a la memoria Off-Heap y los dispara al bus de eventos sin crear objetos temporales (Zero-Copy NIO).
*   **VolcanScene / VolcanEntity Pool:** El orquestador y los envoltorios (Wrappers) Orientados a Objetos. Nacen en el Java Heap, pero se *pre-alocan* 100% en el Boot, burlando al GC. Leen/Escriben al SoA crudo usando punteros hiper-rápidos (`VarHandles`).
*   **Zero-GC:** Durante el tick loop de servidor, la cantidad de memoria asignada en el Heap de Java debe ser exactamente cero (`0 bytes/tick`). Todo ocurre en el bloque `Off-Heap` o reciclando la *Pool* pre-asignada.
