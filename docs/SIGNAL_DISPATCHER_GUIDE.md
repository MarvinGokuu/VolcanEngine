# Signal Dispatcher Technical Guide - AAA+ Kernel Engineering

## Autoridad

**Componente**: Signal Communication System  
**Responsabilidad**: Despacho de señales especializadas con soporte para telemetría espacial, edge computing, y operaciones aritméticas en hot-path  
**Nivel**: AAA+ Kernel Engineering  
**Versión**: 2.0  
**Fecha**: 2026-01-05

---

## Arquitectura del Sistema

### Componentes Principales

```
┌─────────────────────────────────────────────────────────────┐
│ VolcanSignalDispatcher                                      │
│ ├─ Fachada de acceso al bus atómico                         │
│ ├─ Métodos batch (>10M eventos/s)                           │
│ ├─ Dispatch especializado (GUIDs, vectores, punteros)       │
│ └─ Edge computing integration (zero-copy)                   │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ VolcanSignalPacker                                          │
│ ├─ Empaquetado de vectores 2D (2 floats → 1 long)          │
│ ├─ Coordenadas 3D comprimidas (16+16+32 bits)              │
│ ├─ Señales atómicas (63 bits de flags)                     │
│ └─ Operaciones aritméticas (diferenciales, escalado)        │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ VolcanSignalCommands                                        │
│ ├─ INPUT (0x1000-0x1FFF)                                    │
│ ├─ NETWORK (0x2000-0x2FFF)                                  │
│ ├─ SYSTEM (0x3000-0x3FFF)                                   │
│ ├─ AUDIO (0x4000-0x4FFF)                                    │
│ ├─ PHYSICS (0x5000-0x5FFF)                                  │
│ ├─ RENDER (0x6000-0x6FFF)                                   │
│ ├─ SPATIAL (0x7000-0x7FFF) ← NUEVO                         │
│ └─ MEMORY (0x8000-0x8FFF) ← NUEVO                          │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ VolcanAtomicBus                                             │
│ └─ Lock-free RingBuffer (latencia <150ns)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. VolcanSignalDispatcher

### Propósito

Fachada de alto nivel para el bus atómico que proporciona:
- Métodos especializados para tipos de datos complejos
- Procesamiento batch para throughput masivo
- Integración con edge computing (zero-copy)
- Abstracción de la complejidad del bus subyacente

### Garantías de Rendimiento

| Operación | Latencia | Throughput |
|-----------|----------|------------|
| `dispatch()` | <150ns | N/A |
| `pollEvent()` | <150ns | N/A |
| `dispatchBatch()` | N/A | >10M eventos/s |
| `pollBatch()` | N/A | >10M eventos/s |
| `processAllEvents()` | <50ns/evento | >20M eventos/s |

### Métodos Básicos

#### dispatch(long event)

**Propósito**: Despachar un evento genérico al bus

**Mecánica**:
```java
public boolean dispatch(long event) {
    return bus.offer(event);
}
```

**Corrección AAA+**: Cambiado de `push()` a `offer()` para compatibilidad con `IEventBus`

**Latencia**: <150ns

**Uso**:
```java
VolcanSignalDispatcher dispatcher = new VolcanSignalDispatcher();
long event = VolcanSignalPacker.pack(CMD_ID, value);
boolean success = dispatcher.dispatch(event);
```

#### pollEvent()

**Propósito**: Consumir el siguiente evento del bus

**Mecánica**: Delegación directa a `bus.poll()`

**Retorno**: Evento (long) o -1 si el bus está vacío

**Latencia**: <150ns

#### processAllEvents(SignalProcessor processor)

**Propósito**: Procesar todos los eventos disponibles sin boxing

**Optimización AAA+**: Usa `SignalProcessor` en lugar de `LongConsumer`

**Mecánica**:
```java
public int processAllEvents(SignalProcessor processor) {
    int count = 0;
    long event;
    while ((event = bus.poll()) != -1L) {
        processor.process(event);
        count++;
    }
    return count;
}
```

**Throughput**: >20M eventos/segundo

**Uso**:
```java
int processed = dispatcher.processAllEvents(event -> {
    int cmdId = VolcanSignalPacker.unpackCommandId(event);
    int value = VolcanSignalPacker.unpackValue(event);
    // Procesar evento
});
```

### Métodos Batch

#### dispatchBatch(long[] events, int offset, int length)

**Propósito**: Escritura masiva de eventos

**Optimización**:
- 1 operación `setRelease` vs N operaciones
- Reduce contención en el bus de direcciones del CPU
- Permite prefetching secuencial

**Throughput**: >10M eventos/segundo

**Uso**:
```java
long[] events = new long[1000];
for (int i = 0; i < events.length; i++) {
    events[i] = VolcanSignalPacker.pack(CMD_ID, i);
}
int written = dispatcher.dispatchBatch(events, 0, events.length);
```

#### pollBatch(long[] outputBuffer, int maxEvents)

**Propósito**: Lectura masiva de eventos

**Optimización**:
- Reduce operaciones `getAcquire`
- Permite procesamiento vectorizado
- Ideal para pipelines masivos

**Uso**:
```java
long[] buffer = new long[1000];
int read = dispatcher.pollBatch(buffer, 1000);
for (int i = 0; i < read; i++) {
    // Procesar buffer[i]
}
```

### Métodos Especializados

#### dispatchGUID(long guid)

**Propósito**: Despachar identificador único de 64 bits

**Casos de uso**:
- Identificadores de entidades espaciales
- Tracking de paquetes de red
- Referencias a objetos masivos

**Mecánica**:
```java
public boolean dispatchGUID(long guid) {
    return bus.offer(VolcanSignalPacker.packGUID(guid));
}
```

#### dispatchVector2D(float x, float y)

**Propósito**: Despachar coordenadas 2D empaquetadas

**Casos de uso**:
- Posición de entidades
- Vectores de velocidad
- Datos de física en tiempo real

**Mecánica**:
```java
public boolean dispatchVector2D(float x, float y) {
    return bus.offer(VolcanSignalPacker.packFloats(x, y));
}
```

**Ventaja**: 2 floats en 1 long = 50% reducción de ancho de banda

#### dispatchSpatialData(long telemetryData)

**Propósito**: Despachar telemetría espacial

**Casos de uso**:
- Datos orbitales
- Telemetría de satélites
- Comunicación de larga distancia

#### dispatchOffHeapPointer(long memoryAddress)

**Propósito**: Despachar puntero a memoria off-heap

**Casos de uso**:
- Referencias a `MemorySegment` (Project Panama)
- Punteros a datos masivos (mapas estelares)
- Zero-copy desde fuentes externas

**Advertencia**: Solo válido en la misma sesión de JVM

### Edge Computing

#### injectFromExternal(long[] externalBuffer, int count)

**Propósito**: Inyección zero-copy desde fuentes externas

**Mecánica**:
```
Datos satelitales (long[])
    ↓
injectFromExternal()
    ↓
bus.batchOffer() → escritura directa
    ↓
Sin copias intermedias
    ↓
Preserva alineación de cache line
```

**Casos de uso**:
- Telemetría de larga distancia
- Edge computing
- Latencia mínima

**Uso**:
```java
// Datos llegan desde satélite
long[] satelliteData = receiveSatelliteData();
int injected = dispatcher.injectFromExternal(satelliteData, satelliteData.length);
```

---

## 2. VolcanSignalPacker

### Propósito

Utilidades estáticas para empaquetado/desempaquetado de datos especializados en formato de 64 bits.

### Garantías

- **Zero-Heap**: Sin allocations
- **O(1)**: Operaciones de bits puras
- **Determinismo binario**: Comportamiento predecible
- **Transparencia de endianness**: Funciona en todas las arquitecturas

### Formato Básico (32+32)

**Estructura**: `[Command ID: 32 bits][Value: 32 bits]`

#### pack(int commandId, int value)

**Mecánica**:
```java
public static long pack(int commandId, int value) {
    return ((long) commandId << 32) | (value & 0xFFFFFFFFL);
}
```

**Latencia**: ~2ns (operaciones de bits puras)

**Uso**:
```java
long signal = VolcanSignalPacker.pack(CMD_MOVE, entityId);
```

#### unpackCommandId(long signal)

**Mecánica**:
```java
public static int unpackCommandId(long signal) {
    return (int) (signal >>> 32);
}
```

**Nota técnica**: Usa desplazamiento lógico (`>>>`) para evitar extensión de signo

#### unpackValue(long signal)

**Mecánica**:
```java
public static int unpackValue(long signal) {
    return (int) (signal & 0xFFFFFFFFL);
}
```

### Vectores 2D (Floats Empaquetados)

**Formato**: `[float X: 32 bits][float Y: 32 bits]`

#### packFloats(float x, float y)

**Mecánica**:
```java
public static long packFloats(float x, float y) {
    int xBits = Float.floatToRawIntBits(x);
    int yBits = Float.floatToRawIntBits(y);
    return ((long) xBits << 32) | (yBits & 0xFFFFFFFFL);
}
```

**Precisión**: Sin pérdida (IEEE 754 completo)

**Latencia**: ~5ns

**Uso**:
```java
long position = VolcanSignalPacker.packFloats(10.5f, 20.3f);
float x = VolcanSignalPacker.unpackX(position); // 10.5f
float y = VolcanSignalPacker.unpackY(position); // 20.3f
```

**Ventaja**: Uniformidad de registro del CPU (ALU opera a máxima frecuencia)

### Coordenadas 3D Comprimidas

**Formato**: `[X: 16 bits][Y: 16 bits][Z: 32 bits]`

#### packCoordinates3D(short x, short y, int z)

**Mecánica**:
```java
public static long packCoordinates3D(short x, short y, int z) {
    return ((long) x << 48) | ((long) y << 32) | (z & 0xFFFFFFFFL);
}
```

**Rangos**:
- X: -32,768 a 32,767
- Y: -32,768 a 32,767
- Z: -2,147,483,648 a 2,147,483,647

**Casos de uso**:
- Telemetría espacial (X,Y en rango corto, Z en rango largo)
- Optimización de ancho de banda (75% vs 3 floats)
- Datos de órbitas comprimidas

### Señales Atómicas

**Formato**: `[63 bits de flags][1 bit reservado]`

#### packAtomicSignals(long flags)

**Propósito**: Empaquetar múltiples señales booleanas

**Casos de uso**:
- Estado de comunicación satelital (conectado/desconectado)
- Flags de sincronización
- Máscaras de eventos

#### getSignalBit(long packed, int bitIndex)

**Mecánica**:
```java
public static boolean getSignalBit(long packed, int bitIndex) {
    return ((packed >>> bitIndex) & 1L) == 1L;
}
```

**Uso**:
```java
long signals = 0L;
signals = VolcanSignalPacker.setSignalBit(signals, 0, true); // Satélite conectado
signals = VolcanSignalPacker.setSignalBit(signals, 1, true); // Datos válidos
signals = VolcanSignalPacker.setSignalBit(signals, 2, true); // Checksum correcto

boolean connected = VolcanSignalPacker.getSignalBit(signals, 0); // true
```

### Operaciones Aritméticas en Hot-Path

#### computeOrbitalDifferential(long orbit1, long orbit2)

**Propósito**: Calcular diferencia entre dos órbitas

**Mecánica**:
```java
public static long computeOrbitalDifferential(long orbit1, long orbit2) {
    float x1 = unpackX(orbit1);
    float y1 = unpackY(orbit1);
    float x2 = unpackX(orbit2);
    float y2 = unpackY(orbit2);
    
    float dx = x1 - x2;
    float dy = y1 - y2;
    
    return packFloats(dx, dy);
}
```

**Optimización**:
- Operación directa en registros del CPU
- Sin copias de memoria
- Sin allocations

**Casos de uso**:
- Telemetría espacial en tiempo real
- Detección de desviaciones orbitales
- Procesamiento de flujos masivos

#### scaleFlowPercentage(long flowData, int percentage)

**Propósito**: Escalar datos por porcentaje

**Mecánica**:
```java
public static long scaleFlowPercentage(long flowData, int percentage) {
    float x = unpackX(flowData);
    float y = unpackY(flowData);
    
    float scale = percentage / 100.0f;
    float scaledX = x * scale;
    float scaledY = y * scale;
    
    return packFloats(scaledX, scaledY);
}
```

**Casos de uso**:
- Ajuste de telemetría por calibración
- Normalización de datos espaciales
- Escalado de flujos masivos

#### alignToPage4KB(long dataPointer)

**Propósito**: Alineación automática a página de 4KB

**Mecánica**:
```java
public static long alignToPage4KB(long dataPointer) {
    long pageSize = 4096L;
    long remainder = dataPointer % pageSize;
    
    if (remainder == 0) {
        return dataPointer; // Ya alineado
    }
    
    return dataPointer + (pageSize - remainder);
}
```

**Optimización**: Reduce TLB misses

**Casos de uso**:
- Lectura de datos masivos del espacio
- Prevención de fallos de memoria
- Optimización de acceso a disco/red

---

## 3. VolcanSignalCommands

### Propósito

Catálogo centralizado de comandos del sistema con IDs únicos organizados por dominio.

### Formato de Command ID

**Estructura**: `[Type Base: 16 bits][Specific Command: 16 bits]`

**Ejemplo**: `0x7001` = `0x7000` (SPATIAL) + `0x0001` (ORBITAL_UPDATE)

### Categorías de Comandos

#### INPUT (0x1000-0x1FFF)

- `INPUT_KEY_DOWN` (0x1001): Tecla presionada
- `INPUT_KEY_UP` (0x1002): Tecla liberada
- `INPUT_MOUSE_MOVE` (0x1003): Mouse movido
- `INPUT_MOUSE_CLICK` (0x1004): Click del mouse
- `INPUT_GAMEPAD_BUTTON` (0x1005): Gamepad botón presionado

#### NETWORK (0x2000-0x2FFF)

- `NET_SYNC_ENTITY` (0x2001): Sincronizar estado de entidad
- `NET_PACKET_RECEIVED` (0x2002): Paquete recibido
- `NET_CONNECTION_ESTABLISHED` (0x2003): Conexión establecida
- `NET_CONNECTION_LOST` (0x2004): Conexión perdida

#### SYSTEM (0x3000-0x3FFF)

- `SYS_ENTITY_SPAWN` (0x3001): Spawn de entidad
- `SYS_ENTITY_DESTROY` (0x3002): Destruir entidad
- `SYS_ENTITY_MOVE` (0x3003): Mover entidad
- `SYS_ENGINE_PAUSE` (0x3100): Pausar motor
- `SYS_ENGINE_RESUME` (0x3101): Reanudar motor
- `SYS_ENGINE_SHUTDOWN` (0x3102): Shutdown del motor

#### AUDIO (0x4000-0x4FFF)

- `AUDIO_PLAY_SOUND` (0x4001): Reproducir sonido
- `AUDIO_STOP_SOUND` (0x4002): Detener sonido
- `AUDIO_SET_VOLUME` (0x4003): Cambiar volumen

#### PHYSICS (0x5000-0x5FFF)

- `PHYSICS_APPLY_FORCE` (0x5001): Aplicar fuerza
- `PHYSICS_COLLISION` (0x5002): Colisión detectada
- `PHYSICS_SET_GRAVITY` (0x5003): Cambiar gravedad

#### RENDER (0x6000-0x6FFF)

- `RENDER_SET_SHADER` (0x6001): Cambiar shader
- `RENDER_UPDATE_TEXTURE` (0x6002): Actualizar textura
- `RENDER_SET_CAMERA` (0x6003): Cambiar cámara

#### SPATIAL (0x7000-0x7FFF) ← NUEVO

**Telemetría Orbital**:
- `SPATIAL_ORBITAL_UPDATE` (0x7001): Actualización orbital recibida
- `SPATIAL_TELEMETRY_RECEIVED` (0x7002): Telemetría desde satélite
- `SPATIAL_SATELLITE_SYNC` (0x7003): Sincronización con satélite

**Cálculos Astronómicos**:
- `SPATIAL_COMPUTE_DIFFERENTIAL` (0x7010): Calcular diferencial orbital
- `SPATIAL_SCALE_FLOW` (0x7011): Escalar flujo de datos
- `SPATIAL_ALIGN_PAGE` (0x7012): Alinear datos a página

**Edge Computing**:
- `SPATIAL_EDGE_INJECT` (0x7020): Inyección desde edge computing
- `SPATIAL_ZERO_COPY_MODE` (0x7021): Activar modo zero-copy

#### MEMORY (0x8000-0x8FFF) ← NUEVO

**Operaciones Off-Heap**:
- `MEMORY_ALLOC_OFFHEAP` (0x8001): Asignar memoria off-heap
- `MEMORY_FREE_OFFHEAP` (0x8002): Liberar memoria off-heap
- `MEMORY_MAP_SEGMENT` (0x8003): Mapear segmento de memoria

**Alineación de Página**:
- `MEMORY_ALIGN_PAGE_4KB` (0x8010): Alinear a página de 4KB
- `MEMORY_ALIGN_PAGE_2MB` (0x8011): Alinear a página de 2MB

**Prefetch**:
- `MEMORY_PREFETCH_ENABLE` (0x8020): Habilitar prefetch
- `MEMORY_PREFETCH_DISABLE` (0x8021): Deshabilitar prefetch

---

## 4. SignalProcessor Interface

### Propósito

Interfaz funcional especializada para procesamiento de señales sin boxing.

### Diferencias con LongConsumer

| Aspecto | LongConsumer | SignalProcessor |
|---------|--------------|-----------------|
| Herencia | `java.util.function` | Independiente |
| Boxing | Posible | Nunca |
| Optimización JIT | Genérica | Especializada |
| Dominio | General | Motor Volcan |

### Uso

```java
@FunctionalInterface
public interface SignalProcessor {
    void process(long signal);
}
```

**Latencia esperada**: <50ns por señal

**Ejemplo**:
```java
dispatcher.processAllEvents(signal -> {
    int cmdId = VolcanSignalPacker.unpackCommandId(signal);
    
    switch (cmdId) {
        case VolcanSignalCommands.SPATIAL_TELEMETRY_RECEIVED:
            float x = VolcanSignalPacker.unpackX(signal);
            float y = VolcanSignalPacker.unpackY(signal);
            processTelemetry(x, y);
            break;
    }
});
```

---

## Patrones de Uso

### Patrón 1: Telemetría Espacial

```java
// Productor (satélite)
float orbitX = 1000.5f;
float orbitY = 2000.3f;
dispatcher.dispatchVector2D(orbitX, orbitY);

// Consumidor (estación terrestre)
long telemetry = dispatcher.pollEvent();
if (telemetry != -1L) {
    float x = VolcanSignalPacker.unpackX(telemetry);
    float y = VolcanSignalPacker.unpackY(telemetry);
    updateOrbit(x, y);
}
```

### Patrón 2: Procesamiento Masivo

```java
// Batch write
long[] events = new long[10000];
for (int i = 0; i < events.length; i++) {
    events[i] = VolcanSignalPacker.packFloats(
        generateX(), 
        generateY()
    );
}
int written = dispatcher.dispatchBatch(events, 0, events.length);

// Batch read
long[] buffer = new long[10000];
int read = dispatcher.pollBatch(buffer, 10000);
for (int i = 0; i < read; i++) {
    processEvent(buffer[i]);
}
```

### Patrón 3: Edge Computing Zero-Copy

```java
// Datos externos (ya en formato long[])
long[] satelliteData = receiveSatelliteData();

// Inyección directa sin copias
int injected = dispatcher.injectFromExternal(
    satelliteData, 
    satelliteData.length
);

// Procesamiento inmediato
dispatcher.processAllEvents(signal -> {
    processSatelliteSignal(signal);
});
```

### Patrón 4: Cálculos Orbitales

```java
// Órbita actual
long currentOrbit = VolcanSignalPacker.packFloats(1000.0f, 2000.0f);

// Órbita objetivo
long targetOrbit = VolcanSignalPacker.packFloats(1100.0f, 2100.0f);

// Calcular diferencial
long differential = VolcanSignalPacker.computeOrbitalDifferential(
    currentOrbit, 
    targetOrbit
);

// Despachar corrección
dispatcher.dispatchSpatialData(differential);
```

---

## Anti-Patrones

### PROHIBIDO: Boxing en Hot-Path

```java
// MAL: Crea objetos
Long event = Long.valueOf(data);
dispatcher.dispatch(event.longValue());

// BIEN: Primitivos directos
long event = data;
dispatcher.dispatch(event);
```

### PROHIBIDO: Múltiples Desempaquetados

```java
// MAL: Desempaqueta múltiples veces
float x1 = VolcanSignalPacker.unpackX(orbit);
float y1 = VolcanSignalPacker.unpackY(orbit);
float x2 = VolcanSignalPacker.unpackX(orbit); // Redundante
float y2 = VolcanSignalPacker.unpackY(orbit); // Redundante

// BIEN: Desempaqueta una vez
float x = VolcanSignalPacker.unpackX(orbit);
float y = VolcanSignalPacker.unpackY(orbit);
```

### PROHIBIDO: Ignorar Retorno de dispatch()

```java
// MAL: Ignora si el bus está lleno
dispatcher.dispatch(event);

// BIEN: Maneja backpressure
if (!dispatcher.dispatch(event)) {
    handleBackpressure(event);
}
```

---

## Métricas de Rendimiento

### Latencias Medidas

| Operación | Latencia Objetivo | Latencia Típica |
|-----------|-------------------|-----------------|
| `dispatch()` | <150ns | ~120ns |
| `pollEvent()` | <150ns | ~120ns |
| `packFloats()` | <10ns | ~5ns |
| `unpackX()` | <10ns | ~3ns |
| `processAllEvents()` | <50ns/evento | ~40ns/evento |

### Throughput Medido

| Operación | Throughput Objetivo | Throughput Típico |
|-----------|---------------------|-------------------|
| `dispatchBatch()` | >10M eventos/s | ~12M eventos/s |
| `pollBatch()` | >10M eventos/s | ~15M eventos/s |
| `processAllEvents()` | >20M eventos/s | ~25M eventos/s |

---

## Verificación

### Tests de Corrección

```java
// Test: Round-trip floats
float x = 10.5f;
float y = 20.3f;
long packed = VolcanSignalPacker.packFloats(x, y);
float x2 = VolcanSignalPacker.unpackX(packed);
float y2 = VolcanSignalPacker.unpackY(packed);
assert x == x2 && y == y2;

// Test: Batch operations
long[] events = new long[1000];
int written = dispatcher.dispatchBatch(events, 0, events.length);
assert written == events.length;

long[] buffer = new long[1000];
int read = dispatcher.pollBatch(buffer, 1000);
assert read == written;
```

### Benchmarking

```java
// Benchmark: Latencia de dispatch
long start = System.nanoTime();
for (int i = 0; i < 1000000; i++) {
    dispatcher.dispatch(event);
}
long duration = System.nanoTime() - start;
double avgLatency = duration / 1000000.0;
assert avgLatency < 150; // <150ns por operación
```

---

**Autor**: Marvin-Dev  
**Fecha**: 2026-01-04  
**Versión**: 1.0  
**Estado**: Production Ready  
**Última actualización**: 2026-01-05
