# HPC RUNTIME TECHNICAL GLOSSARY

**Subsistema**: Documentation / Reference  
**Tecnología**: Engineering Definitons  
**Estado**: V2.0 Standard  

---

## 1. Conceptos de Hardware

### 1.1. Cache Line (L1)
Unidad de transferencia de memoria entre el CPU y la caché L1. En arquitecturas x86_64 modernas, su tamaño es de **64 bytes**.
*   **Impacto**: Variables contiguas en la misma línea de caché que son escritas por diferentes hilos provocan **False Sharing**.
*   **Mitigación**: Padding explícito de 56 bytes alrededor de variables volátiles de 8 bytes (Header/Tail).

### 1.2. Store Buffer
Buffer de escritura intermedio en el CPU.
*   **Relevancia**: Las escrituras no son inmediatamente visibles para otros núcleos.
*   **Control**: Instrucciones de barrera (`SFENCE` o semántica Release de VarHandles) fuerzan el vaciado del Store Buffer.

### 1.3. Branch Prediction
Mecanismo del CPU para adivinar el resultado de una condicional.
*   **Optimización**: El código "branchless" o con patrones predecibles maximiza el throughput del pipeline de instrucciones.

---

## 2. Definiciones de Runtime

### 2.1. Off-Heap Memory
Memoria asignada fuera del control del Garbage Collector de la JVM.
*   **Implementación**: `java.lang.foreign.MemorySegment`.
*   **Ventaja**: Latencia de acceso determinista y cero pausas de GC.

### 2.2. Zero-Copy
Arquitectura de transferencia de datos donde la información no se duplica en memoria intermedia.
*   **Mecánica**: Paso de punteros (direcciones de memoria) en lugar de copiar arrays de bytes.

### 2.3. Vector API (SIMD)
Single Instruction, Multiple Data. Capacidad del CPU para procesar múltiples valores (ej. 8 integers) en un solo ciclo de reloj.
*   **Implementación**: `jdk.incubator.vector.IntVector`.
*   **Uso**: Procesamiento masivo de entidades y cálculos físicos.

---

## 3. Primitivas de Concurrencia

### 3.1. VarHandle
Referencia tipada a una variable que permite acceso atómico y ordenado.
*   **Acquire**: Garantiza visibilidad de lectura.
*   **Release**: Garantiza visibilidad de escritura.
*   **Opaque**: Acceso sin garantías de orden, pero atómico respecto a tearing.

### 3.2. Lock-Free
Algoritmo que garantiza que al menos un hilo del sistema hace progreso en un tiempo finito.
*   **Diferencia**: A diferencia de `Wait-Free`, algunos hilos pueden sufrir starvation, pero el sistema global avanza.

---

## 4. Componentes del Sistema

### 4.1. Atomic Bus
Canal de comunicación de baja latencia basado en Ring Buffer.
*   **Clase**: `VolcanAtomicBus` (Referencia de implementación).
*   **Capacidad**: Throughput > 10M msg/s.

### 4.2. Kernel Loop
Bucle principal de ejecución del runtime.
*   **Frecuencia**: Fija (60 Hz / 16.666 ms).
*   **Fases**: Input -> Bus -> Systems -> Audit.

---

**Autoridad**: System Architect  
**Versión**: 2.0
