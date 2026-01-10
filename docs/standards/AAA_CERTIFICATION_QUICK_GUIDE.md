# 🚀 QUICK REFERENCE GUIDE - Certificación AAA+

## 📋 Template Copiable

```java
import sv.volcan.core.AAACertified;

// ═══════════════════════════════════════════════════════════════════════════════
// CERTIFICACIÓN AAA+ - [NOMBRE DEL COMPONENTE EN MAYÚSCULAS]
// ═══════════════════════════════════════════════════════════════════════════════
//
// PORQUÉ:
// - La anotación @AAACertified documenta las garantías de rendimiento inline
// - RetentionPolicy.SOURCE = 0ns overhead (eliminada en bytecode)
// - Metadata visible para humanos, invisible para la JVM
// - [Explicar qué hace este componente en el contexto del motor]
//
// TÉCNICA:
// - maxLatencyNs: [valor] = [Explicación técnica de por qué este valor]
// - minThroughput: [valor] = [Explicación técnica de por qué este valor]
// - alignment: [valor] = [Explicación técnica de alineación de memoria]
// - lockFree: [true/false] = [Explicación de concurrencia]
// - offHeap: [true/false] = [Explicación de gestión de memoria]
//
// GARANTÍA:
// - Esta anotación NO afecta el rendimiento en runtime
// - Solo documenta las métricas esperadas del componente
// - Validable con herramientas estáticas en build-time
// - Overhead medido: 0ns (confirmado con javap)
//
@AAACertified(
    date = "YYYY-MM-DD",
    maxLatencyNs = [valor],
    minThroughput = [valor],
    alignment = [valor],
    lockFree = [true/false],
    offHeap = [true/false],
    notes = "[Descripción concisa del componente]"
)
public final class ComponentName {
    // ...
}
```

---

## 📊 Valores Comunes

### **Latencia (maxLatencyNs)**

| Componente | Valor | Justificación |
|------------|-------|---------------|
| **TimeKeeper** | 1 | Lectura directa del TSC (Time Stamp Counter) |
| **VolcanAtomicBus** | 150 | VarHandles con Acquire/Release |
| **VolcanRingBus** | 150 | VarHandles con Acquire/Release |
| **VolcanStateVault** | 150 | Acceso off-heap directo |
| **SovereignKernel** | 16_666_000 | Fixed timestep a 60 FPS (16.666ms) |

### **Throughput (minThroughput)**

| Componente | Valor | Justificación |
|------------|-------|---------------|
| **TimeKeeper** | 60 | 60 FPS (frames por segundo) |
| **VolcanAtomicBus** | 10_000_000 | 10M eventos/segundo (batch) |
| **VolcanRingBus** | 10_000_000 | 10M eventos/segundo (batch) |
| **SovereignKernel** | 60 | 60 frames por segundo |

### **Alignment (alignment)**

| Valor | Uso |
|-------|-----|
| **64** | Cache line alignment (estándar para x86-64) |
| **4096** | Page alignment (4KB para TLB optimization) |

### **Lock-Free (lockFree)**

| Valor | Uso |
|-------|-----|
| **true** | Ring buffers, TimeKeeper (spin-wait), componentes sin synchronized |
| **false** | Kernel (orquestador), componentes con coordinación compleja |

### **Off-Heap (offHeap)**

| Valor | Uso |
|-------|-----|
| **true** | VolcanStateVault (MemorySegment), componentes con datos masivos |
| **false** | Buses (long[] primitivo), Kernel (orquestador) |

---

## 🎯 Ejemplos de Referencia

### **Ejemplo 1: Bus Atómico (Sinapsis Neuronal)**

```java
// PORQUÉ:
// - Este bus es una sinapsis neuronal: transmite señales entre componentes
//
// TÉCNICA:
// - maxLatencyNs: 150 = VarHandles con Acquire/Release (sin synchronized)
// - minThroughput: 10_000_000 = 10M eventos/segundo (batch operations)
// - alignment: 64 = Cache line alignment para evitar False Sharing
// - lockFree: true = Ring buffer sin locks (1 productor + 1 consumidor)
// - offHeap: false = Buffer vive en heap (long[] primitivo)
//
@AAACertified(
    date = "2026-01-06",
    maxLatencyNs = 150,
    minThroughput = 10_000_000,
    alignment = 64,
    lockFree = true,
    offHeap = false,
    notes = "Lock-Free Ring Buffer with VarHandles and Cache Line Padding"
)
public final class VolcanAtomicBus implements IEventBus {
```

### **Ejemplo 2: Kernel (Procesador Central)**

```java
// PORQUÉ:
// - Este kernel es el cerebro: orquesta el flujo de datos en 4 fases
//
// TÉCNICA:
// - maxLatencyNs: 16_666_000 = Fixed timestep a 60 FPS (16.666ms por frame)
// - minThroughput: 60 = 60 frames por segundo (determinismo temporal)
// - alignment: 64 = Cache line alignment para variables críticas
// - lockFree: false = Usa TimeKeeper (spin-wait) pero no locks pesados
// - offHeap: false = Kernel vive en heap (orquestador, no datos)
//
@AAACertified(
    date = "2026-01-06",
    maxLatencyNs = 16_666_000,
    minThroughput = 60,
    alignment = 64,
    lockFree = false,
    offHeap = false,
    notes = "Central neural processor - 4-phase deterministic loop at 60 FPS"
)
public final class SovereignKernel {
```

### **Ejemplo 3: TimeKeeper (Sensor Temporal)**

```java
// PORQUÉ:
// - TimeKeeper es la neurona sensorial: captura tiempo determinista
//
// TÉCNICA:
// - maxLatencyNs: 1 = Lectura directa del TSC (Time Stamp Counter)
// - minThroughput: 60 = Fixed timestep a 60 FPS
// - alignment: 64 = Cache line alignment para evitar False Sharing
// - lockFree: true = Sin synchronized, solo operaciones atómicas
// - offHeap: false = TimeKeeper vive en heap (no requiere memoria nativa)
//
@AAACertified(
    date = "2026-01-06",
    maxLatencyNs = 1,
    minThroughput = 60,
    alignment = 64,
    lockFree = true,
    offHeap = false,
    notes = "Sensory neuron - TSC-based temporal determinism at 60 FPS"
)
public final class TimeKeeper {
```

---

## 🔬 Validación de Overhead

### **Comando para verificar 0ns overhead**:

```bash
# Compilar el componente
javac -d bin src/sv/volcan/[path]/ComponentName.java

# Verificar que la anotación NO está en bytecode
javap -c bin/sv/volcan/[path]/ComponentName.class | grep "AAACertified"

# Resultado esperado: (vacío)
# Si aparece algo = ERROR (cambiar RetentionPolicy)
```

---

## 📝 Checklist de Certificación

Antes de certificar un componente, verificar:

- [ ] **Nombre descriptivo** en el bloque de comentarios
- [ ] **PORQUÉ** explica el rol del componente en el motor
- [ ] **TÉCNICA** justifica cada parámetro con datos técnicos
- [ ] **GARANTÍA** confirma 0ns overhead
- [ ] **maxLatencyNs** basado en mediciones reales o estimaciones fundadas
- [ ] **minThroughput** basado en requisitos del motor
- [ ] **alignment** apropiado para el tipo de datos
- [ ] **lockFree** correcto según mecanismo de concurrencia
- [ ] **offHeap** correcto según gestión de memoria
- [ ] **notes** conciso pero descriptivo
- [ ] **date** actualizada a la fecha de certificación

---

## 🎯 Componentes Certificados

### **Completados** ✅
1. VolcanAtomicBus - Sinapsis neuronal (<150ns)
2. VolcanRingBus - Sinapsis observable (<150ns)
3. SovereignKernel - Procesador central (60 FPS)
4. VolcanStateVault - Memoria a largo plazo (<150ns)
5. TimeKeeper - Sensor temporal (<1ns)

### **Pendientes** ⏳
- [ ] VolcanEventDispatcher
- [ ] VolcanEventLane
- [ ] SystemRegistry
- [ ] WorldStateFrame
- [ ] MovementSystem
- [ ] PhysicsSystem

---

**Versión**: 1.0  
**Autor**: Marvin-Dev  
**Fecha**: 2026-01-06T20:55:16-06:00  
**Uso**: Copiar/pegar para certificar nuevos componentes
