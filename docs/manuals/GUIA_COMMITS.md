# Guía de Commits - Volcan Engine

## Autoridad

**Documento**: Guía de Commits y Control de Versiones  
**Nivel**: Documentation Engineering  
**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Propósito**: Establecer convenciones para commits y control de versiones del proyecto

---

## Formato de Commit

### Estructura Estándar

```
[CATEGORIA] Descripción breve (máximo 72 caracteres)

Descripción detallada del cambio (opcional):
- Qué se cambió
- Por qué se cambió
- Impacto del cambio

Referencias:
- Issue #XX (si aplica)
- Documento: [NOMBRE_DOCUMENTO.md]
```

---

## Categorías de Commit

### [FEAT] - Nueva Funcionalidad

**Uso**: Cuando se agrega nueva funcionalidad al código

**Ejemplos**:
```
[FEAT] Implementar Boot Selector AAA+ en SovereignProtocol.bat

- Agregado menú interactivo con 4 modos de arranque
- Implementadas 3 capas de validación de seguridad
- Agregado rollback automático en caso de fallo

Documento: AAA_CERTIFICATION.md
```

```
[FEAT] Crear Test_BusBenchmark para validación AAA+

- Implementados tests de latencia (<150ns)
- Implementados tests de throughput (>10M eventos/s)
- Agregado cálculo de percentiles (p50, p95, p99)

Documento: AAA_CODING_STANDARDS.md
```

---

### [FIX] - Corrección de Bug

**Uso**: Cuando se corrige un error en el código

**Ejemplos**:
```
[FIX] Corregir False Sharing en VolcanAtomicBus

- Agregado padding de 64 bytes entre head y tail
- Renombradas variables de padding con nomenclatura de hardware
- Validación de checksum en constructor

Impacto: Latencia reducida de 1000ns a 150ns
Documento: DOCUMENTACION_BUS.md
```

---

### [REFACTOR] - Refactorización

**Uso**: Cuando se mejora el código sin cambiar funcionalidad

**Ejemplos**:
```
[REFACTOR] Renombrar variables de padding en VolcanRingBus

- p1...p7 → headShield_L1_slot1...7
- p10...p16 → isolationBridge_slot1...7
- p20...p26 → tailShield_L1_slot1...7

Beneficio: Mejor trazabilidad en memory dumps
Documento: TECHNICAL_GLOSSARY.md
```

---

### [DOCS] - Documentación

**Uso**: Cuando se actualiza o crea documentación

**Ejemplos**:
```
[DOCS] Crear AAA_CERTIFICATION.md con protocolo de certificación

- Definidos criterios de certificación AAA+
- Documentadas 3 capas de validación de seguridad
- Agregado Boot Selector con 4 modos de arranque
- Incluido checklist de certificación

Versión: 1.0
```

```
[DOCS] Actualizar autoría en todos los documentos

- Cambiado "AAA Kernel Division" → "Marvin-Dev"
- Actualizados 9 archivos (6 .md, 2 .java, 1 artifact)
- Eliminadas referencias a grupos/divisiones

Documento: WALKTHROUGH_AUTHOR_UPDATE.md
```

---

### [STYLE] - Formato y Estilo

**Uso**: Cuando se corrige formato sin cambiar funcionalidad

**Ejemplos**:
```
[STYLE] Eliminar emojis de AAA_CERTIFICATION.md

- Removidos todos los emojis decorativos
- Aplicado lenguaje técnico preciso
- Corregida ortografía y capitalización

Documento: ESTANDAR_DOCUMENTACION.md
```

---

### [TEST] - Tests

**Uso**: Cuando se agregan o modifican tests

**Ejemplos**:
```
[TEST] Agregar Test_BusHardware para validación de padding

- Validación de headShield_L1 (7 slots)
- Validación de isolationBridge (7 slots)
- Validación de tailShield_L1 (7 slots)
- Checksum debe ser 0

Criterio: Padding corruption detection
```

---

### [PERF] - Optimización de Rendimiento

**Uso**: Cuando se mejora el rendimiento

**Ejemplos**:
```
[PERF] Optimizar batchOffer en VolcanAtomicBus

- Reducidas operaciones volatile de N a 1
- Implementado prefetching secuencial
- Throughput mejorado de 5M/s a 12M/s

Métrica: >10M eventos/s (AAA+ compliant)
Documento: AAA_CODING_STANDARDS.md
```

---

### [SECURITY] - Seguridad

**Uso**: Cuando se implementan mejoras de seguridad

**Ejemplos**:
```
[SECURITY] Implementar validación de firma térmica

- Agregado getPaddingChecksum() en arranque
- Detección de modificaciones no autorizadas
- Colapso preventivo si checksum != 0

Documento: AAA_CERTIFICATION.md
```

---

### [BUILD] - Sistema de Build

**Uso**: Cuando se modifica el sistema de compilación

**Ejemplos**:
```
[BUILD] Actualizar SovereignProtocol.bat con flags de JVM

- Agregado --enable-preview para JDK 25
- Agregado --enable-native-access=ALL-UNNAMED
- Agregado -XX:+UseVectorApiIntrinsics

Requisito: Project Panama support
```

---

### [CHORE] - Tareas de Mantenimiento

**Uso**: Para tareas de mantenimiento general

**Ejemplos**:
```
[CHORE] Renombrar documentos genéricos

- implementation_plan.md → PLAN_IMPLEMENTACION_AAA_COMPLIANCE.md
- task.md → TASK_AAA_COMPLIANCE.md
- walkthrough.md → WALKTHROUGH_AUTHOR_UPDATE.md

Documento: ESTANDAR_DOCUMENTACION.md
```

---

## Buenas Prácticas

### 1. Commits Atómicos

Cada commit debe representar un cambio lógico único:

✅ **Correcto**:
```
[FEAT] Agregar validación de firma térmica
[TEST] Crear test para validación de firma térmica
[DOCS] Documentar validación de firma térmica
```

❌ **Incorrecto**:
```
[FEAT] Agregar validación, tests y documentación
```

---

### 2. Mensajes Descriptivos

✅ **Correcto**:
```
[FIX] Corregir False Sharing en VolcanAtomicBus mediante padding de 64 bytes
```

❌ **Incorrecto**:
```
[FIX] Arreglar bug
```

---

### 3. Referencias a Documentación

Siempre incluir referencia al documento relevante:

```
[FEAT] Implementar Boot Selector AAA+

Documento: AAA_CERTIFICATION.md
Sección: Arranque de Doble Vía
```

---

### 4. Impacto Medible

Para cambios de rendimiento, incluir métricas:

```
[PERF] Optimizar batchPoll en VolcanRingBus

Antes: 5M eventos/s
Después: 12M eventos/s
Mejora: 2.4x

Documento: AAA_CODING_STANDARDS.md
```

---

## Flujo de Trabajo

### 1. Antes de Commit

- [ ] Código compila sin errores
- [ ] Tests pasan correctamente
- [ ] Documentación actualizada
- [ ] Formato de código correcto

### 2. Crear Commit

```bash
git add [archivos específicos]
git commit -m "[CATEGORIA] Descripción breve"
```

### 3. Después de Commit

- [ ] Actualizar ACTUALIZACIONES_PENDIENTES.md
- [ ] Marcar tarea como completada en TASK_*.md
- [ ] Incrementar versión de documento si aplica

---

## Commits de Documentación

### Actualización de Versión

Cuando se actualiza un documento, incluir cambio de versión:

```
[DOCS] Actualizar AAA_CERTIFICATION.md v1.0 → v1.1

Cambios:
- Agregada sección de protección contra malware
- Corregida ortografía en sección 3
- Actualizadas métricas de rendimiento

Versión: 1.1
Fecha: 2026-01-05
```

---

### Creación de Documento

```
[DOCS] Crear ESTANDAR_DOCUMENTACION.md

Contenido:
- Formato estándar de documentos
- Convenciones de nomenclatura
- Proceso de actualización continua

Versión: 1.0
Fecha: 2026-01-05
```

---

## Commits de Refactorización

### Renombrado de Variables

```
[REFACTOR] Renombrar variables de padding en VolcanAtomicBus

Cambios:
- p1...p7 → headShield_L1_slot1...7
- p10...p16 → isolationBridge_slot1...7
- p20...p26 → tailShield_L1_slot1...7

Beneficio: Identidad de hardware en memory dumps
Documento: DOCUMENTACION_BUS.md
```

---

### Consolidación de Código

```
[REFACTOR] Consolidar lógica de validación en método único

Antes: Validación dispersa en 3 métodos
Después: Método validateBootIntegrity() centralizado

Beneficio: Mejor mantenibilidad
Documento: AAA_CERTIFICATION.md
```

---

## Historial de Commits Importantes

### 2026-01-05

```
[DOCS] Crear AAA_CERTIFICATION.md con protocolo de certificación AAA+
[DOCS] Actualizar autoría en 9 archivos (Marvin-Dev)
[STYLE] Eliminar emojis y aplicar lenguaje técnico en AAA_CERTIFICATION.md
[DOCS] Crear ESTANDAR_DOCUMENTACION.md con formato oficial
[DOCS] Crear ACTUALIZACIONES_PENDIENTES.md con registro centralizado
[CHORE] Renombrar documentos genéricos según estándar
```

### 2026-01-04

```
[FEAT] Implementar Signal Dispatcher AAA+
[REFACTOR] Renombrar variables de padding en buses
[TEST] Crear Test_BusHardware para validación de padding
[DOCS] Actualizar TECHNICAL_GLOSSARY.md con nuevos términos
```

---

## Plantillas de Commit

### Plantilla para Nueva Funcionalidad

```
[FEAT] [Título breve]

Descripción:
- [Qué se agregó]
- [Cómo funciona]
- [Por qué es necesario]

Impacto:
- [Cambios en rendimiento]
- [Cambios en API]

Documento: [NOMBRE_DOCUMENTO.md]
Versión: [X.Y]
```

---

### Plantilla para Corrección de Bug

```
[FIX] [Título breve]

Problema:
- [Descripción del bug]
- [Cómo se manifestaba]

Solución:
- [Qué se cambió]
- [Por qué funciona]

Impacto:
- [Mejora en rendimiento/estabilidad]

Documento: [NOMBRE_DOCUMENTO.md]
```

---

### Plantilla para Documentación

```
[DOCS] [Título breve]

Contenido:
- [Secciones agregadas/modificadas]
- [Información nueva]

Versión: [X.Y → X.Z]
Fecha: [YYYY-MM-DD]
Autor: Marvin-Dev
```

---

## Verificación Pre-Commit

### Checklist

- [ ] Código compila: `SovereignProtocol.bat`
- [ ] Tests pasan: `java sv.volcan.bus.Test_BusHardware`
- [ ] Documentación actualizada
- [ ] Versión incrementada si aplica
- [ ] ACTUALIZACIONES_PENDIENTES.md actualizado
- [ ] Mensaje de commit sigue formato estándar
- [ ] Categoría de commit correcta

---

## Integración con ACTUALIZACIONES_PENDIENTES.md

Después de cada commit, actualizar el documento de pendientes:

```markdown
### Completadas (2026-01-05)

- [x] Crear AAA_CERTIFICATION.md
- [x] Actualizar autoría en documentos
- [x] Eliminar emojis de documentación técnica
- [x] Crear ESTANDAR_DOCUMENTACION.md
- [x] Renombrar documentos genéricos
```

---

**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Estado**: Activo
