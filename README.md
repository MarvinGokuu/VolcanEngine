# HIGH-PERFORMANCE JAVA RUNTIME

**Subsistema**: Kernel / Core  
**Tecnología**: Java 25 (Panama, Vector, Loom)  
**Estado**: Production Ready (Certified)  

> **🚀 NEW:** [Quick Start Guide](docs/QUICK_START.md) - De 0 a Running en 5 minutos

---

## 1. Visión General del Sistema
Este proyecto implementa un runtime de simulación determinista de alta frecuencia (60Hz) diseñado para maximizar el throughput de instrucciones y minimizar la latencia de memoria en hardware x86_64 moderno.

### Principios de Ingeniería
*   **Gestión de Memoria**: Uso exclusivo de segmentos off-heap (`java.lang.foreign.MemorySegment`) para evitar interferencia del Garbage Collector.
*   **Paralelismo de Datos**: Procesamiento vectorial (SIMD) mediante el módulo incubadora `jdk.incubator.vector`.
*   **Concurrencia**: Comunicación lock-free entre hilos via Ring Buffers y VarHandles (Acquire/Release fences).

---

## 2. Métricas de Certificación (Verificado 2026-01-27)

| Métrica | Target | Typical | Best | Delta | Unidad |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Atomic Bus Latency** | < 150 | **23.35** | **23.35** | -84% | ns |
| **Event Throughput** | > 10.0 | **185.0** | **185.0** | +1750% | M/s |
| **SIMD Bandwidth** | > 4.0 | **4.17** | **4.17** | +4.2% | GB/s |
| **Boot Latency** | < 1.0 | **0.221-0.427** | **0.167** | -78% to -57% | ms |

**Notas de Verificación**:
*   **Typical**: Rango observado en test suite completo (7/7 tests, 2026-01-27)
*   **Best**: Récord histórico bajo condiciones óptimas (JIT warm, cache hot)
*   **Test Coverage**: 100% (BusBenchmark, BusCoordination, BusHardware, UltraFastBoot, GracefulShutdown, PowerSaving, Governor)

### 2.1. Características AAA+ Implementadas

*   **Graceful Shutdown**: Shutdown Hook con 6 pasos deterministas, liberación 100% de recursos nativos (Arena, MemorySegments)
*   **Baseline Validation (A/B/C)**: Protocolo científico para detección de memory leaks con validación automática
*   **3-Tier Power Saving**: Escalado progresivo de CPU (Tier 1: SpinWait → Tier 2: Sleep 1ms → Tier 3: Sleep 100ms)
*   **Deterministic 4-Phase Loop**: Input Latch → Bus Processing → Systems Execution → State Audit

---

## 3. Guía de Inicio Rápido (Bootstrapping)

### 3.1. Prerrequisitos de Compilación
*   **JDK**: OpenJDK 25 (con módulos incubadora habilitados).
*   **OS**: Windows 11 / Linux Kernel 6.x (Soporte para Huge Pages recomendado).

### 3.2. Secuencia de Ejecución

**Para certificación AAA+ y métricas récord:**
```bash
# 1. Compilación optimizada (incluye auto-limpieza)
build.bat
# [Cerrar motor auto-ejecutado con Ctrl+C]

# 2. Validación completa (7/7 tests)
test.bat
```

**Para desarrollo rápido (hot reload):**
```bash
# Compilar una vez
build.bat
# [Cerrar motor]

# Ejecutar sin recompilar
run.bat
```

> 📖 **Referencia completa**: Ver [docs/BUILD_WORKFLOWS.md](docs/BUILD_WORKFLOWS.md) para workflows detallados, flags de compilación, y casos de uso.


### 3.3. Perfiles de Configuración

#### Production Profile (Default)
- **Logging**: DISABLED (0ns overhead)
- **Metrics Sampling**: 0.1% (5ns overhead)
- **Validation**: DISABLED (0ns overhead)
- **Target Latency**: <150ns ✅

#### Development Profile
- **Logging**: ENABLED (full debug)
- **Metrics Sampling**: 100% (measure everything)
- **Validation**: ENABLED (all checks)
- **Target**: Maximum observability

**Archivos de configuración**:
- `config/volcan-production.properties`
- `config/volcan-development.properties`
```

### 3.4. Tests de Validación

```bash
# Benchmark de rendimiento AAA+
java -cp bin sv.volcan.bus.BusBenchmarkTest

# Validación de Graceful Shutdown (Protocolo A/B/C)
java -cp bin sv.volcan.test.Test_GracefulShutdown

# Validación de Power Saving (3 niveles)
java -cp bin sv.volcan.test.Test_PowerSaving
```

**Resultados esperados**:
*   ✅ Latencia < 150ns (AAA+ Target)
*   ✅ Throughput > 10M ops/s (AAA+ Target)
*   ✅ Shutdown 100% limpio (0 memory leaks)
*   ✅ CPU escalado progresivo (100% → 0%)

---

## 4. Mapa de Documentación Técnica

### Inicio Rápido
*   **[Quick Start Guide](docs/QUICK_START.md)** - De 0 a Running en 5 minutos
*   [Guía de Desarrollo](docs/DEVELOPMENT_GUIDE.md) - Desarrollo activo
*   [Resumen Ejecutivo](docs/README_DOCS.md) - Estado del proyecto

### Estándares y Especificaciones
*   [Estándar de Documentación v2.0](docs/standards/ESTANDAR_DOCUMENTACION.md)
*   [Certificación Vectorial (SIMD)](docs/standards/ACCELERATOR_CERTIFICATION.md)
*   [Estándares de Codificación AAA](docs/standards/AAA_CODING_STANDARDS.md)

### Arquitectura de Sistemas
*   [Especificación de Arquitectura](docs/architecture/ARQUITECTURA_VOLCAN_ENGINE.md)
*   [Especificación del Bus de Eventos](docs/manuals/DOCUMENTACION_BUS.md)
*   [Glosario Técnico de Runtime](docs/glossary/TECHNICAL_GLOSSARY.md)

### Guías de Operación
*   [Bootstrapping de Documentación](DOCUMENTATION_BOOTSTRAP.md)
*   [Protocolo de Commits](docs/manuals/GUIA_COMMITS.md)

---

## 5. Reporte de Estado

**Versión del Runtime**: v2.3-mvp  
**Última Validación**: 2026-01-21  
**Autoridad**: System Architect

> **Nota Técnica**: Este runtime requiere habilitar `jdk.incubator.vector` en tiempo de ejecución. El incumplimiento resultará en `NoClassDefFoundError`.
