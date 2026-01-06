# Documentación Volcan Engine - Índice Maestro

## Autoridad

**Propósito**: Índice centralizado de toda la documentación técnica del motor Volcan  
**Versión**: 2.0  
**Fecha**: 2026-01-05  
**Estado**: Actualizado con Signal Dispatcher AAA+

---

## 📚 Documentación Principal

### 1. Arquitectura General

**[ARQUITECTURA_VOLCAN_ENGINE.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/ARQUITECTURA_VOLCAN_ENGINE.md)**
- Visión completa del motor
- Filosofía y pilares fundamentales
- Componentes principales (Kernel, Bus, State, Systems)
- Flujo de datos completo
- Ventajas competitivas vs Unreal/RAGE/Star Engine
- Roadmap hacia AAA

### 2. Estándares de Código

**[AAA_CODING_STANDARDS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/AAA_CODING_STANDARDS.md)**
- Formato de documentación técnica
- Métricas de rendimiento (<150ns, >10M eventos/s)
- Patrones de empaquetado de datos
- Arquitectura de VarHandles
- Estructura de padding (64 bytes)
- Signal Dispatcher - Datos especializados
- Comandos del sistema (8 categorías)
- Ejemplos de uso óptimo
- Anti-patrones prohibidos

---

## 🔧 Documentación Técnica Especializada

### Bus Atómico

**[DOCUMENTACION_BUS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/DOCUMENTACION_BUS.md)**
- VolcanAtomicBus: Arquitectura lock-free
- VolcanRingBus: Implementación alternativa
- Padding y False Sharing
- VarHandles y semántica Acquire/Release
- Métodos avanzados AAA+
- Métricas y benchmarking

### Signal Dispatcher

**[SIGNAL_DISPATCHER_GUIDE.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/SIGNAL_DISPATCHER_GUIDE.md)**
- Arquitectura del sistema de señales
- VolcanSignalDispatcher: Métodos y garantías
- VolcanSignalPacker: Formatos especializados
  - Vectores 2D (2 floats en 1 long)
  - Coordenadas 3D comprimidas
  - GUIDs y punteros off-heap
  - Señales atómicas (63 bits)
- VolcanSignalCommands: 8 categorías
- SignalProcessor: Interfaz sin boxing
- Patrones de uso
- Anti-patrones

### Glosario Técnico

**[TECHNICAL_GLOSSARY.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/TECHNICAL_GLOSSARY.md)**
- Glosario alfabético (A-Z)
- Tecnologías (AAA+ Standards, VarHandle, Project Panama)
- Métodos (todos documentados con firma, propósito, latencia)
- Archivos (referencias con líneas de código)
- Variables (head, tail, mask, padding)
- Conceptos técnicos (BARRIER DETERMINISM, REGISTRY ANCHORING)
- Hardware (L1 Cache, TLB, ALU)
- Comandos (8 categorías completas)

---

## 📋 Documentación de Planificación

### Plan de Implementación

**[implementation_plan.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/implementation_plan.md)**
- Análisis del estado actual
- Auditoría técnica de componentes
- Cambios propuestos
- Operaciones aritméticas en hot-path
- Edge computing integration
- Cronograma de implementación (6 fases)
- Riesgos y mitigaciones

### Checklist de Tareas

**[task.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/task.md)**
- Fase 1: Correcciones críticas ✅
- Fase 2: Formatos especializados ✅
- Fase 3: Comandos espaciales ✅
- Fase 4: Operaciones aritméticas ✅
- Fase 5: Edge computing ✅
- Fase 6: Testing y verificación (en progreso)

### Walkthrough

**[walkthrough.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/walkthrough.md)**
- Resumen de cambios implementados
- Estado final de componentes
- Archivos modificados
- Próximos pasos

---

## 🗂️ Estructura de Archivos del Proyecto

### Bus System (`src/sv/volcan/bus/`)

| Archivo | Líneas | Descripción |
|---------|--------|-------------|
| [IEventBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/IEventBus.java) | ~50 | Interfaz de abstracción |
| [VolcanAtomicBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanAtomicBus.java) | 562 | Ring Buffer lock-free |
| [VolcanRingBus.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanRingBus.java) | 562 | Implementación alternativa |
| [VolcanSignalDispatcher.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalDispatcher.java) | 237 | Fachada de acceso |
| [VolcanSignalPacker.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalPacker.java) | 343 | Empaquetado especializado |
| [VolcanSignalCommands.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/VolcanSignalCommands.java) | 233 | Catálogo de comandos |
| [SignalProcessor.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/bus/SignalProcessor.java) | 40 | Interfaz sin boxing |

### Kernel System (`src/sv/volcan/kernel/`)

| Archivo | Descripción |
|---------|-------------|
| [SovereignKernel.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/kernel/SovereignKernel.java) | Loop de 4 fases |
| [TimeKeeper.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/kernel/TimeKeeper.java) | Guardián del tiempo |
| [SystemRegistry.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/kernel/SystemRegistry.java) | Orquestador de sistemas |

### State System (`src/sv/volcan/state/`)

| Archivo | Descripción |
|---------|-------------|
| [VolcanStateLayout.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/state/VolcanStateLayout.java) | ABI (direccionamiento) |
| [VolcanStateVault.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/state/VolcanStateVault.java) | Storage off-heap |
| [WorldStateFrame.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/state/WorldStateFrame.java) | Snapshots inmutables |
| [VolcanEngineMaster.java](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/state/VolcanEngineMaster.java) | Punto de entrada |

---

## 🎯 Guías de Referencia Rápida

### Métricas de Rendimiento

| Métrica | Objetivo AAA+ | Componente |
|---------|---------------|------------|
| Latencia offer/poll | <150ns | VolcanAtomicBus |
| Throughput batch | >10M eventos/s | batchOffer/batchPoll |
| Latencia packFloats | ~5ns | VolcanSignalPacker |
| Latencia unpackX/Y | ~3ns | VolcanSignalPacker |
| Alineación de memoria | 64 bytes | Padding (L1 Cache Line) |
| GC pauses | 0 | Off-Heap Memory |

### Comandos del Sistema

| Categoría | Rango | Uso |
|-----------|-------|-----|
| INPUT | 0x1000-0x1FFF | Teclado, mouse, gamepad |
| NETWORK | 0x2000-0x2FFF | Sincronización, paquetes |
| SYSTEM | 0x3000-0x3FFF | Entidades, motor |
| AUDIO | 0x4000-0x4FFF | Sonidos, volumen |
| PHYSICS | 0x5000-0x5FFF | Fuerzas, colisiones |
| RENDER | 0x6000-0x6FFF | Shaders, texturas |
| SPATIAL | 0x7000-0x7FFF | Telemetría, órbitas |
| MEMORY | 0x8000-0x8FFF | Off-heap, alineación |

### Formatos de Datos Especializados

| Formato | Estructura | Uso |
|---------|------------|-----|
| Básico | [CommandID:32][Value:32] | Comandos generales |
| Vectores 2D | [X:32][Y:32] floats | Posición, velocidad |
| Coordenadas 3D | [X:16][Y:16][Z:32] | Telemetría espacial |
| GUID | [64 bits] | Identificadores únicos |
| Off-Heap Pointer | [64 bits] | Referencias a MemorySegment |
| Señales Atómicas | [63 bits flags] | Estados booleanos |

---

## 🚀 Flujos de Trabajo Comunes

### 1. Despachar Evento Simple

```java
VolcanSignalDispatcher dispatcher = new VolcanSignalDispatcher();
long event = VolcanSignalPacker.pack(CMD_MOVE, entityId);
boolean success = dispatcher.dispatch(event);
```

### 2. Procesar Eventos sin Boxing

```java
dispatcher.processAllEvents(signal -> {
    int cmdId = VolcanSignalPacker.unpackCommandId(signal);
    int value = VolcanSignalPacker.unpackValue(signal);
    handleCommand(cmdId, value);
});
```

### 3. Telemetría Espacial

```java
// Productor
dispatcher.dispatchVector2D(orbitX, orbitY);

// Consumidor
long telemetry = dispatcher.pollEvent();
float x = VolcanSignalPacker.unpackX(telemetry);
float y = VolcanSignalPacker.unpackY(telemetry);
```

### 4. Procesamiento Masivo

```java
// Batch write
long[] events = new long[10000];
int written = dispatcher.dispatchBatch(events, 0, events.length);

// Batch read
long[] buffer = new long[10000];
int read = dispatcher.pollBatch(buffer, 10000);
```

---

## 📖 Orden de Lectura Recomendado

### Para Nuevos Desarrolladores

1. **[ARQUITECTURA_VOLCAN_ENGINE.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/ARQUITECTURA_VOLCAN_ENGINE.md)** - Visión general
2. **[AAA_CODING_STANDARDS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/AAA_CODING_STANDARDS.md)** - Estándares de código
3. **[DOCUMENTACION_BUS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/DOCUMENTACION_BUS.md)** - Sistema de comunicación
4. **[SIGNAL_DISPATCHER_GUIDE.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/SIGNAL_DISPATCHER_GUIDE.md)** - Sistema de señales

### Para Desarrollo Activo

1. **[task.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/task.md)** - Checklist actual
2. **[implementation_plan.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/implementation_plan.md)** - Plan detallado
3. **[TECHNICAL_GLOSSARY.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/TECHNICAL_GLOSSARY.md)** - Referencia rápida

### Para Debugging

1. **[TECHNICAL_GLOSSARY.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/TECHNICAL_GLOSSARY.md)** - Buscar términos
2. **[AAA_CODING_STANDARDS.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/AAA_CODING_STANDARDS.md)** - Anti-patrones
3. **[walkthrough.md](file:///c:/Users/theca/Documents/GitHub/VolcanEngine/src/sv/volcan/test/documentacion/walkthrough.md)** - Cambios recientes

---

## 🔍 Búsqueda Rápida

### Por Concepto

- **Latencia**: AAA_CODING_STANDARDS.md, DOCUMENTACION_BUS.md
- **Padding**: AAA_CODING_STANDARDS.md, TECHNICAL_GLOSSARY.md
- **VarHandle**: AAA_CODING_STANDARDS.md, DOCUMENTACION_BUS.md
- **Empaquetado**: AAA_CODING_STANDARDS.md, SIGNAL_DISPATCHER_GUIDE.md
- **Edge Computing**: SIGNAL_DISPATCHER_GUIDE.md, implementation_plan.md

### Por Componente

- **VolcanAtomicBus**: DOCUMENTACION_BUS.md, TECHNICAL_GLOSSARY.md
- **VolcanSignalDispatcher**: SIGNAL_DISPATCHER_GUIDE.md, implementation_plan.md
- **VolcanSignalPacker**: SIGNAL_DISPATCHER_GUIDE.md, AAA_CODING_STANDARDS.md
- **SovereignKernel**: ARQUITECTURA_VOLCAN_ENGINE.md

---

## ✅ Estado de Documentación

| Documento | Estado | Última Actualización |
|-----------|--------|---------------------|
| ARQUITECTURA_VOLCAN_ENGINE.md | ✅ Completo | 2026-01-04 |
| AAA_CODING_STANDARDS.md | ✅ Actualizado | 2026-01-05 |
| DOCUMENTACION_BUS.md | ✅ Completo | 2026-01-04 |
| SIGNAL_DISPATCHER_GUIDE.md | ✅ Completo | 2026-01-05 |
| TECHNICAL_GLOSSARY.md | ✅ Completo | 2026-01-05 |
| implementation_plan.md | ✅ Completo | 2026-01-05 |
| task.md | 🔄 En progreso | 2026-01-05 |
| walkthrough.md | ✅ Completo | 2026-01-05 |

---

**Autor**: Marvin-Dev  
**Fecha**: 2026-01-04  
**Versión**: 1.0  
**Estado**: Documentación Unificada  
**Última actualización**: 2026-01-05
