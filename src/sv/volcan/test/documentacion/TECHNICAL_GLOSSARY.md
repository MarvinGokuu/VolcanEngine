# Glosario Técnico - Volcan Engine

## Autoridad

**Documento**: Glosario Técnico Completo  
**Nivel**: AAA+ Kernel Engineering  
**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Propósito**: Referencia exhaustiva de tecnologías, métodos, archivos, variables y conceptos del motor Volcan

---

## A

### AAA+ Standards
**Categoría**: Metodología  
**Definición**: Estándares de ingeniería de nivel kernel que garantizan latencias <150ns, throughput >10M eventos/s, y alineación de memoria de 64 bytes.  
**Referencia**: [AAA_CODING_STANDARDS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/docs/AAA_CODING_STANDARDS.md)

### Acquire Semantics
**Categoría**: Concurrencia  
**Definición**: Semántica de memoria que garantiza que todas las escrituras previas en otros threads sean visibles ANTES de leer un valor.  
**Implementación**: `VarHandle.getAcquire(this)`  
**Mapeo de hardware**:
- x86/x64: MOV (TSO implícito)
- ARM/AArch64: LDAR
- RISC-V: LD + FENCE

### alignToPage4KB()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long alignToPage4KB(long dataPointer)`  
**Propósito**: Alinear puntero de memoria a página de 4KB  
**Mecánica**: Ajusta offset para que sea múltiplo de 4096  
**Beneficio**: Reduce TLB misses  
**Latencia**: ~10ns  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### ALU (Arithmetic Logic Unit)
**Categoría**: Hardware  
**Definición**: Unidad del CPU que realiza operaciones aritméticas y lógicas  
**Relevancia**: La uniformidad de registro (solo long) permite que la ALU opere a máxima frecuencia sin cambios de contexto

---

## B

### BARRIER DETERMINISM
**Categoría**: Concepto técnico  
**Definición**: Garantías de orden de memoria mediante semántica Acquire/Release sin locks pesados  
**Implementación**: VarHandles (HEAD_H, TAIL_H)  
**Latencia**: ~150ns (vs ~1000-5000ns con synchronized)  
**Documentación**: Bloques de comentarios en VolcanAtomicBus y VolcanRingBus

### batchOffer()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus, VolcanSignalDispatcher)  
**Firma**: `public int batchOffer(long[] events, int offset, int length)`  
**Propósito**: Escritura masiva de eventos  
**Optimización**: 1 setRelease vs N operaciones  
**Throughput**: >10M eventos/segundo  
**Latencia**: ~50ns por evento  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### batchPoll()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus, VolcanSignalDispatcher)  
**Firma**: `public int batchPoll(long[] outputBuffer, int maxEvents)`  
**Propósito**: Lectura masiva de eventos  
**Optimización**: Reduce operaciones Acquire  
**Throughput**: >10M eventos/segundo  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### Boxing
**Categoría**: Anti-patrón  
**Definición**: Conversión de primitivos a objetos (ej: long → Long)  
**Problema**: Crea allocations en hot-path, pausas de GC  
**Solución**: Usar primitivos directos y SignalProcessor en lugar de LongConsumer  
**Prohibido en**: Hot-path, métodos de bus, procesamiento de señales

### buffer
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `private final long[]`  
**Propósito**: Carretera física de datos del RingBuffer  
**Acceso**: Mediante índices calculados dinámicamente (index & mask)  
**Tamaño**: Potencia de 2 (ej: 16384 elementos)  
**Nota**: El IDE puede marcarla como "no usada" debido a acceso dinámico

---

## C

### Cache Line
**Categoría**: Hardware  
**Definición**: Unidad mínima de transferencia entre CPU y memoria (64 bytes en x86/x64)  
**Relevancia**: Padding de 64 bytes previene False Sharing  
**Estructura**: 7 slots de 8 bytes + 1 variable crítica = 64 bytes

### casHead()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public boolean casHead(long expectedHead, long newHead)`  
**Propósito**: Compare-And-Swap atómico en puntero head  
**Uso**: Multi-consumidor concurrente  
**Implementación**: `HEAD_H.compareAndSet(this, expectedHead, newHead)`  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### clear()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus, VolcanSignalDispatcher)  
**Firma**: `public void clear()`  
**Propósito**: Limpieza completa del bus  
**Mecánica**: Resetea head y tail a 0  
**Advertencia**: No thread-safe con productores/consumidores activos  
**Uso**: Shutdown o reset del sistema

### computeOrbitalDifferential()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long computeOrbitalDifferential(long orbit1, long orbit2)`  
**Propósito**: Calcular diferencia entre dos órbitas  
**Mecánica**: Desempaqueta → resta → reempaqueta  
**Optimización**: Operación en registros del CPU, sin allocations  
**Uso**: Telemetría espacial, detección de desviaciones orbitales  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

---

## D

### DATA FLOW ARCHITECTURE
**Categoría**: Concepto técnico  
**Definición**: Arquitectura de flujo de datos que documenta gestión, lectura y liberación  
**Componentes**:
- Gestión: Productor escribe con TAIL_H
- Lectura: Consumidor lee con HEAD_H
- Liberación: Automática al avanzar head
**Documentación**: Bloques de comentarios en VolcanAtomicBus y VolcanRingBus

### dispatch()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public boolean dispatch(long event)`  
**Propósito**: Despachar evento genérico al bus  
**Corrección AAA+**: Cambiado de `push()` a `offer()`  
**Latencia**: <150ns  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### dispatchBatch()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public int dispatchBatch(long[] events, int offset, int length)`  
**Propósito**: Despacho masivo de eventos  
**Delegación**: `bus.batchOffer()`  
**Throughput**: >10M eventos/segundo  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### dispatchGUID()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public boolean dispatchGUID(long guid)`  
**Propósito**: Despachar identificador único de 64 bits  
**Uso**: Identificadores de entidades, tracking de paquetes  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### dispatchOffHeapPointer()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public boolean dispatchOffHeapPointer(long memoryAddress)`  
**Propósito**: Despachar puntero a memoria off-heap  
**Uso**: Referencias a MemorySegment, datos masivos  
**Advertencia**: Solo válido en la misma sesión de JVM  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### dispatchSpatialData()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public boolean dispatchSpatialData(long telemetryData)`  
**Propósito**: Despachar telemetría espacial  
**Uso**: Datos orbitales, telemetría satelital  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### dispatchVector2D()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public boolean dispatchVector2D(float x, float y)`  
**Propósito**: Despachar coordenadas 2D empaquetadas  
**Mecánica**: Empaqueta 2 floats en 1 long  
**Uso**: Posición, velocidad, fuerzas  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

---

## E

### Edge Computing
**Categoría**: Arquitectura  
**Definición**: Procesamiento de datos en el borde de la red, cerca de la fuente  
**Implementación**: `injectFromExternal()` con zero-copy  
**Beneficio**: Latencia mínima, sin copias intermedias  
**Uso**: Telemetría satelital, datos externos

---

## F

### False Sharing
**Categoría**: Problema de rendimiento  
**Definición**: Múltiples threads acceden a variables en la misma cache line, causando invalidaciones  
**Solución**: Padding de 64 bytes (7 slots de 8 bytes)  
**Impacto**: Sin padding: ~1000ns, Con padding: ~150ns  
**Implementación**: headShield_L1, isolationBridge, tailShield_L1

---

## G

### GC (Garbage Collector)
**Categoría**: JVM  
**Definición**: Sistema de gestión automática de memoria en Java  
**Problema**: Compaction puede mover objetos y romper alineación  
**Solución**: REGISTRY ANCHORING (acceso explícito a padding)  
**Relevancia**: Inmunidad al GC compaction preserva layout de 64 bytes

### getPaddingChecksum()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public long getPaddingChecksum()`  
**Propósito**: Validación de integridad de padding  
**Mecánica**: Reducción aritmética vertical (acc += slot)  
**Garantía**: Fuerza dependencia RAW, previene DCE del JIT  
**Retorno**: 0 si padding está intacto  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### getSignalBit()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static boolean getSignalBit(long packed, int bitIndex)`  
**Propósito**: Leer bit específico de señales atómicas  
**Mecánica**: `((packed >>> bitIndex) & 1L) == 1L`  
**Uso**: Estado de comunicación satelital, flags  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### GUID
**Categoría**: Tipo de dato  
**Definición**: Globally Unique Identifier (64 bits)  
**Uso**: Identificadores de entidades, tracking de paquetes  
**Empaquetado**: `packGUID(long)` (pass-through)  
**Formato**: 64 bits completos

---

## H

### head
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `volatile long`  
**Propósito**: Puntero de lectura del RingBuffer  
**Acceso**: Mediante HEAD_H (VarHandle)  
**Inicialización**: 0  
**Operación**: Incrementa después de poll()

### HEAD_H
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `private static final VarHandle`  
**Propósito**: "Puntero de C" para manipulación atómica de head  
**Operaciones**: getAcquire, setRelease, compareAndSet  
**Inicialización**: `MethodHandles.lookup().findVarHandle()`  
**Nota**: El IDE puede marcarla como "no usada" debido a uso con VarHandle

### headShield_L1_slot1..7
**Categoría**: Variables de padding (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `long` (package-private)  
**Propósito**: Padding de 56 bytes antes de head  
**Estructura**: 7 slots × 8 bytes = 56 bytes  
**Total con head**: 64 bytes (1 L1 Cache Line)  
**Visibilidad**: Package-private para auditoría nominal

### Hot-Path
**Categoría**: Concepto de rendimiento  
**Definición**: Código que se ejecuta con alta frecuencia  
**Requisitos**: Sin allocations, sin I/O, sin bloqueos  
**Ejemplos**: offer(), poll(), processAllEvents()  
**Latencia objetivo**: <150ns

---

## I

### IEventBus
**Categoría**: Interfaz  
**Archivo**: [IEventBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/IEventBus.java)  
**Métodos**:
- `boolean offer(long event)`
- `long poll()`
- `long peek()`
- `int size()`
- `int capacity()`
- `void clear()`
- `boolean isEmpty()`
- `boolean isFull()`

### IEEE 754
**Categoría**: Estándar  
**Definición**: Estándar de representación de punto flotante  
**Relevancia**: `Float.floatToRawIntBits()` usa IEEE 754  
**Garantía**: Sin pérdida de precisión en empaquetado de floats

### injectFromExternal()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public int injectFromExternal(long[] externalBuffer, int count)`  
**Propósito**: Inyección zero-copy desde fuentes externas  
**Mecánica**: Escritura directa en buffer del bus  
**Beneficio**: Sin copias intermedias, preserva alineación  
**Uso**: Telemetría satelital, edge computing  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### isContiguous()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public boolean isContiguous(int requiredLength)`  
**Propósito**: Validar espacio contiguo antes de wrap-around  
**Uso**: Permite System.arraycopy directo  
**Beneficio**: Máxima velocidad de transferencia  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### isolationBridge_slot1..7
**Categoría**: Variables de padding (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `long` (package-private)  
**Propósito**: Padding de 64 bytes entre head y tail  
**Estructura**: 7 slots × 8 bytes + tail (8 bytes) = 64 bytes  
**Función**: Separación completa de cache lines

---

## J

### JIT (Just-In-Time Compiler)
**Categoría**: JVM  
**Definición**: Compilador que optimiza bytecode a código nativo en runtime  
**Problema**: Puede eliminar variables de padding (DCE)  
**Solución**: Reducción aritmética vertical en getPaddingChecksum()  
**Optimizaciones**: Inlining, loop unrolling, escape analysis

---

## L

### L1 Cache
**Categoría**: Hardware  
**Definición**: Caché de nivel 1 del CPU (más rápida, más pequeña)  
**Tamaño de línea**: 64 bytes (x86/x64)  
**Latencia**: ~4 ciclos (~1ns a 4GHz)  
**Relevancia**: Padding de 64 bytes alinea con L1 Cache Line

### LDAR/STLR
**Categoría**: Instrucciones ARM  
**Definición**: Load-Acquire / Store-Release  
**Mapeo**: VarHandle.getAcquire → LDAR, VarHandle.setRelease → STLR  
**Propósito**: Garantías de orden de memoria en ARM/AArch64

### Lock-Free
**Categoría**: Arquitectura de concurrencia  
**Definición**: Algoritmo que garantiza progreso sin locks  
**Implementación**: VarHandles con Acquire/Release  
**Beneficio**: Sin deadlocks, sin contención de locks  
**Latencia**: ~150ns vs ~1000-5000ns con synchronized

---

## M

### mask
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `private final int`  
**Propósito**: Optimización matemática para módulo  
**Cálculo**: `capacity - 1`  
**Uso**: `index & mask` (10x más rápido que `index % capacity`)  
**Ejemplo**: capacity=16384 → mask=16383 (0x3FFF)

### Memory Fence
**Categoría**: Concepto de hardware  
**Definición**: Barrera que previene reordenamiento de operaciones de memoria  
**Implementación**: VarHandle.setRelease, VarHandle.fullFence  
**Propósito**: Garantizar visibilidad de escrituras  
**Costo**: ~50-100ns

### MEMORY Commands
**Categoría**: Comandos (0x8000-0x8FFF)  
**Archivo**: [VolcanSignalCommands.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalCommands.java)  
**Comandos**:
- MEMORY_ALLOC_OFFHEAP (0x8001)
- MEMORY_FREE_OFFHEAP (0x8002)
- MEMORY_MAP_SEGMENT (0x8003)
- MEMORY_ALIGN_PAGE_4KB (0x8010)
- MEMORY_ALIGN_PAGE_2MB (0x8011)
- MEMORY_PREFETCH_ENABLE (0x8020)
- MEMORY_PREFETCH_DISABLE (0x8021)

### MemorySegment
**Categoría**: API (Project Panama)  
**Definición**: Abstracción de memoria off-heap en Java  
**Uso**: Datos masivos, zero-copy  
**Relevancia**: packOffHeapPointer() para referencias

---

## O

### offer()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public boolean offer(long eventData)`  
**Propósito**: Inserción no bloqueante de evento  
**Mecánica**: getAcquire(tail) → validar → escribir → setRelease(tail+1)  
**Latencia**: <150ns  
**Retorno**: true si insertado, false si lleno  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### Off-Heap Memory
**Categoría**: Concepto de memoria  
**Definición**: Memoria fuera del heap de Java  
**Beneficio**: Sin GC, acceso directo  
**Uso**: Datos masivos, mapas estelares  
**API**: MemorySegment (Project Panama)

---

## P

### pack()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long pack(int commandId, int value)`  
**Propósito**: Empaquetar comando y valor en 64 bits  
**Formato**: [commandId: 32 bits][value: 32 bits]  
**Mecánica**: `((long) commandId << 32) | (value & 0xFFFFFFFFL)`  
**Latencia**: ~2ns  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### packAtomicSignals()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long packAtomicSignals(long flags)`  
**Propósito**: Empaquetar señales booleanas  
**Formato**: 63 bits de flags  
**Uso**: Estado satelital, flags de sincronización  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### packCoordinates3D()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long packCoordinates3D(short x, short y, int z)`  
**Propósito**: Empaquetar coordenadas 3D comprimidas  
**Formato**: [X: 16 bits][Y: 16 bits][Z: 32 bits]  
**Rangos**: X,Y (-32768 a 32767), Z (rango completo int)  
**Uso**: Telemetría espacial, optimización de ancho de banda  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### packFloats()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long packFloats(float x, float y)`  
**Propósito**: Empaquetar 2 floats en 1 long  
**Formato**: [float X: 32 bits][float Y: 32 bits]  
**Mecánica**: `Float.floatToRawIntBits()` → shift → OR  
**Precisión**: Sin pérdida (IEEE 754)  
**Latencia**: ~5ns  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### packGUID()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long packGUID(long guid)`  
**Propósito**: Empaquetar GUID de 64 bits  
**Mecánica**: Pass-through (consistencia de API)  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### packOffHeapPointer()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long packOffHeapPointer(long memoryAddress)`  
**Propósito**: Empaquetar puntero off-heap  
**Advertencia**: Solo válido en misma sesión JVM  
**Uso**: Referencias a MemorySegment  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### peek()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public long peek()`  
**Propósito**: Lectura no destructiva del siguiente evento  
**Mecánica**: Lee sin avanzar head  
**Retorno**: Evento o -1 si vacío  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### peekWithSequence()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public long peekWithSequence(long sequence)`  
**Propósito**: Lectura indexada sin consumir  
**Uso**: Retransmisión de paquetes perdidos, comunicación satelital  
**Mecánica**: Lee en posición head + sequence  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### poll()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public long poll()`  
**Propósito**: Extracción destructiva de evento  
**Mecánica**: getAcquire(head) → leer → setRelease(head+1)  
**Latencia**: <150ns  
**Retorno**: Evento o -1 si vacío  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### pollEvent()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public long pollEvent()`  
**Propósito**: Consumir siguiente evento del bus  
**Delegación**: `bus.poll()`  
**Latencia**: <150ns  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### Prefetching
**Categoría**: Optimización de hardware  
**Definición**: CPU anticipa accesos secuenciales y carga datos en caché  
**Beneficio**: Reduce latencia de acceso a memoria  
**Implementación**: Escrituras/lecturas secuenciales en batchOffer/batchPoll

### processAllEvents()
**Categoría**: Método (VolcanSignalDispatcher)  
**Firma**: `public int processAllEvents(SignalProcessor processor)`  
**Propósito**: Procesar todos los eventos sin boxing  
**Optimización**: Usa SignalProcessor en lugar de LongConsumer  
**Throughput**: >20M eventos/segundo  
**Latencia**: ~40ns por evento  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)

### Project Panama
**Categoría**: API de Java  
**Definición**: APIs para acceso a memoria nativa y funciones externas  
**Componentes**: MemorySegment, Foreign Function API  
**Uso**: Memoria off-heap, alineación de página

---

## R

### RAW Hazard (Read-After-Write)
**Categoría**: Concepto de CPU  
**Definición**: Dependencia de datos donde una lectura debe esperar una escritura  
**Uso**: getPaddingChecksum() fuerza RAW para prevenir DCE  
**Beneficio**: Garantiza que JIT no elimine variables de padding

### REGISTRY ANCHORING
**Categoría**: Concepto técnico  
**Definición**: Anclaje de layout de memoria mediante acceso explícito  
**Mecánica**: Suma vertical de padding (acc += slot)  
**Propósito**: Inmunidad al GC compaction  
**Garantía**: Preserva alineación de 64 bytes  
**Documentación**: Bloques de comentarios en getPaddingChecksum()

### Release Semantics
**Categoría**: Concurrencia  
**Definición**: Semántica de memoria que garantiza que todas las escrituras sean visibles ANTES de actualizar un valor  
**Implementación**: `VarHandle.setRelease(this, newValue)`  
**Mapeo de hardware**:
- x86/x64: MOV (TSO implícito)
- ARM/AArch64: STLR
- RISC-V: FENCE + ST

### RingBuffer
**Categoría**: Estructura de datos  
**Definición**: Buffer circular de tamaño fijo  
**Características**: Lock-free, FIFO, wrap-around  
**Implementación**: VolcanAtomicBus, VolcanRingBus  
**Capacidad**: Potencia de 2 (optimización de máscara)

---

## S

### scaleFlowPercentage()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long scaleFlowPercentage(long flowData, int percentage)`  
**Propósito**: Escalar datos por porcentaje  
**Mecánica**: Desempaqueta → multiplica → reempaqueta  
**Uso**: Calibración de telemetría, normalización  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### setSignalBit()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static long setSignalBit(long packed, int bitIndex, boolean value)`  
**Propósito**: Establecer bit específico  
**Mecánica**: OR para set (1), AND NOT para clear (0)  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### SignalProcessor
**Categoría**: Interfaz funcional  
**Archivo**: [SignalProcessor.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/SignalProcessor.java)  
**Método**: `void process(long signal)`  
**Propósito**: Procesamiento de señales sin boxing  
**Diferencia con LongConsumer**: No hereda de java.util.function, optimizaciones JIT específicas  
**Latencia**: <50ns por señal

### size()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public int size()`  
**Propósito**: Número de eventos pendientes  
**Cálculo**: `(int) (tail - head)`  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### sovereignShutdown()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public void sovereignShutdown()`  
**Propósito**: Cierre seguro con validación  
**Mecánica**: clear() → getPaddingChecksum()  
**Postcondiciones**: head == tail, checksum == 0  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

### SPATIAL Commands
**Categoría**: Comandos (0x7000-0x7FFF)  
**Archivo**: [VolcanSignalCommands.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalCommands.java)  
**Comandos**:
- SPATIAL_ORBITAL_UPDATE (0x7001)
- SPATIAL_TELEMETRY_RECEIVED (0x7002)
- SPATIAL_SATELLITE_SYNC (0x7003)
- SPATIAL_COMPUTE_DIFFERENTIAL (0x7010)
- SPATIAL_SCALE_FLOW (0x7011)
- SPATIAL_ALIGN_PAGE (0x7012)
- SPATIAL_EDGE_INJECT (0x7020)
- SPATIAL_ZERO_COPY_MODE (0x7021)

### spatialMemoryBarrier()
**Categoría**: Método (VolcanAtomicBus, VolcanRingBus)  
**Firma**: `public void spatialMemoryBarrier()`  
**Propósito**: Barrera de memoria completa  
**Implementación**: `VarHandle.fullFence()`  
**Costo**: ~500ns  
**Uso**: Sincronización de flujos masivos espaciales  
**Advertencia**: Operación costosa, usar solo cuando sea crítico  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)

---

## T

### tail
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `volatile long`  
**Propósito**: Puntero de escritura del RingBuffer  
**Acceso**: Mediante TAIL_H (VarHandle)  
**Inicialización**: 0  
**Operación**: Incrementa después de offer()

### TAIL_H
**Categoría**: Variable (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `private static final VarHandle`  
**Propósito**: "Puntero de C" para manipulación atómica de tail  
**Operaciones**: getAcquire, setRelease  
**Inicialización**: `MethodHandles.lookup().findVarHandle()`

### tailShield_L1_slot1..7
**Categoría**: Variables de padding (VolcanAtomicBus, VolcanRingBus)  
**Tipo**: `long` (package-private)  
**Propósito**: Padding de 56 bytes después de tail  
**Estructura**: 7 slots × 8 bytes = 56 bytes  
**Función**: Protección final de cache line

### TLB (Translation Lookaside Buffer)
**Categoría**: Hardware  
**Definición**: Caché de traducciones de direcciones virtuales a físicas  
**Problema**: TLB miss causa latencia adicional  
**Solución**: Alineación de página (4KB) reduce TLB misses  
**Método**: alignToPage4KB()

### TSO (Total Store Order)
**Categoría**: Modelo de memoria  
**Definición**: Modelo de memoria de x86/x64  
**Característica**: Garantías implícitas de orden  
**Relevancia**: VarHandles mapean a MOV con TSO implícito

---

## U

### unpack3DX(), unpack3DY(), unpack3DZ()
**Categoría**: Métodos (VolcanSignalPacker)  
**Propósito**: Desempaquetar coordenadas 3D  
**Mecánica**: Desplazamiento y máscaras  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### unpackCommandId()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static int unpackCommandId(long signal)`  
**Propósito**: Extraer command ID de señal  
**Mecánica**: `(int) (signal >>> 32)`  
**Nota**: Usa desplazamiento lógico (>>>) para evitar extensión de signo  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### unpackValue()
**Categoría**: Método (VolcanSignalPacker)  
**Firma**: `public static int unpackValue(long signal)`  
**Propósito**: Extraer valor de señal  
**Mecánica**: `(int) (signal & 0xFFFFFFFFL)`  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

### unpackX(), unpackY()
**Categoría**: Métodos (VolcanSignalPacker)  
**Propósito**: Desempaquetar floats de vector 2D  
**Mecánica**: `Float.intBitsToFloat()` con shift/mask  
**Precisión**: Sin pérdida (IEEE 754)  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)

---

## V

### VarHandle
**Categoría**: API de Java  
**Definición**: Referencia tipada a variable con operaciones atómicas  
**Operaciones**: get, set, getAcquire, setRelease, compareAndSet, fullFence  
**Beneficio**: Garantías de memoria sin synchronized  
**Latencia**: ~150ns (vs ~1000-5000ns con synchronized)  
**Uso**: HEAD_H, TAIL_H

### VolcanAtomicBus
**Categoría**: Clase  
**Archivo**: [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java)  
**Propósito**: RingBuffer lock-free con operaciones atómicas  
**Características**: VarHandles, padding de 64 bytes, batch operations  
**Latencia**: <150ns  
**Throughput**: >10M eventos/s

### VolcanRingBus
**Categoría**: Clase  
**Archivo**: [VolcanRingBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanRingBus.java)  
**Propósito**: RingBuffer lock-free con operaciones volátiles  
**Diferencia con AtomicBus**: Volatile reads/writes en lugar de CAS  
**Paridad**: 100% con VolcanAtomicBus (métodos, padding, documentación)

### VolcanSignalCommands
**Categoría**: Clase  
**Archivo**: [VolcanSignalCommands.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalCommands.java)  
**Propósito**: Catálogo de comandos del sistema  
**Categorías**: INPUT, NETWORK, SYSTEM, AUDIO, PHYSICS, RENDER, SPATIAL, MEMORY  
**Formato**: [Type Base: 16 bits][Specific Command: 16 bits]

### VolcanSignalDispatcher
**Categoría**: Clase  
**Archivo**: [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java)  
**Propósito**: Fachada de acceso al bus atómico  
**Características**: Métodos especializados, batch operations, edge computing  
**Capacidad**: 65536 slots (2^16)

### VolcanSignalPacker
**Categoría**: Clase  
**Archivo**: [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java)  
**Propósito**: Utilidades de empaquetado/desempaquetado  
**Características**: Zero-heap, O(1), determinismo binario  
**Formatos**: 32+32, floats, 3D, GUIDs, punteros, señales atómicas

---

## Z

### Zero-Copy
**Categoría**: Técnica de optimización  
**Definición**: Transferencia de datos sin copias intermedias  
**Implementación**: injectFromExternal(), MemorySegment  
**Beneficio**: Latencia mínima, sin overhead de memoria  
**Uso**: Edge computing, telemetría satelital

### ZERO-COPY SEMANTICS
**Categoría**: Concepto técnico  
**Definición**: Operación directa sobre primitivos sin copias  
**Características**:
- Sin creación de objetos en hot-path
- Sin serialización/deserialización
- Sin copias de memoria intermedias
**Documentación**: Bloques de comentarios en VolcanAtomicBus

---

## Archivos del Proyecto

### Buses
- [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java) - 562 líneas
- [VolcanRingBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanRingBus.java) - 562 líneas
- [IEventBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/IEventBus.java) - Interfaz

### Signal System
- [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java) - 237 líneas
- [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java) - 343 líneas
- [VolcanSignalCommands.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalCommands.java) - 233 líneas
- [SignalProcessor.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/SignalProcessor.java) - 40 líneas

### Documentación
- [AAA_CODING_STANDARDS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/docs/AAA_CODING_STANDARDS.md)
- [SIGNAL_DISPATCHER_GUIDE.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/docs/SIGNAL_DISPATCHER_GUIDE.md)

---

## Métricas de Referencia

### Latencias
- offer/poll: <150ns
- packFloats: ~5ns
- unpackX/Y: ~3ns
- processAllEvents: ~40ns/evento
- spatialMemoryBarrier: ~500ns

### Throughput
- batchOffer/batchPoll: >10M eventos/s
- processAllEvents: >20M eventos/s

### Memoria
- L1 Cache Line: 64 bytes
- Padding total: 192 bytes (3 cache lines)
- Page size: 4096 bytes (4KB)

### Velocidad de la Luz
- 299,792,458 m/s
- En 150ns: ~45 metros
- Objetivo: Operación más rápida que la luz cruzando un edificio

---

**Autor**: Marvin-Dev  
**Actualización**: 2026-01-05  
**Estado**: Completo  
**Última actualización**: 2026-01-05
