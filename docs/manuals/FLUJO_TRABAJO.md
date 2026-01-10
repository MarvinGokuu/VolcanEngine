# Flujo de Trabajo - Volcan Engine

## Autoridad

**Documento**: Flujo de Trabajo para Cambios  
**Nivel**: Documentation Engineering  
**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Propósito**: Documentar el flujo completo de trabajo para realizar cambios en el proyecto

---

## Diagrama de Flujo General

```
┌─────────────────────────────────────────────────────────────┐
│                    INICIO: Nueva Tarea                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 1: Consultar Documentación de Referencia             │
├─────────────────────────────────────────────────────────────┤
│  • Revisar LISTA_PENDIENTES.md (prioridades)               │
│  • Revisar ACTUALIZACIONES_PENDIENTES.md (estado)          │
│  • Revisar PLAN_IMPLEMENTACION_AAA_COMPLIANCE.md (plan)    │
│  • Revisar TASK_AAA_COMPLIANCE.md (tareas específicas)     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 2: Identificar Tipo de Cambio                        │
├─────────────────────────────────────────────────────────────┤
│  ¿Qué tipo de cambio es?                                   │
│  ┌─────────────────┬─────────────────┬──────────────────┐  │
│  │ [FEAT]          │ [FIX]           │ [DOCS]           │  │
│  │ Nueva función   │ Corrección bug  │ Documentación    │  │
│  └─────────────────┴─────────────────┴──────────────────┘  │
│  ┌─────────────────┬─────────────────┬──────────────────┐  │
│  │ [REFACTOR]      │ [TEST]          │ [CHORE]          │  │
│  │ Mejora código   │ Tests           │ Mantenimiento    │  │
│  └─────────────────┴─────────────────┴──────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 3: Marcar Tarea como En Progreso                     │
├─────────────────────────────────────────────────────────────┤
│  En TASK_AAA_COMPLIANCE.md:                                │
│  - [ ] Tarea pendiente                                     │
│  - [/] Tarea en progreso  ← ACTUALIZAR AQUÍ               │
│  - [x] Tarea completada                                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 4: Realizar el Cambio                                │
├─────────────────────────────────────────────────────────────┤
│  A. Si es código:                                          │
│     • Editar archivo .java                                 │
│     • Seguir AAA_CODING_STANDARDS.md                       │
│     • Mantener nomenclatura (headShield_L1, etc.)          │
│                                                             │
│  B. Si es documentación:                                   │
│     • Seguir ESTANDAR_DOCUMENTACION.md                     │
│     • Usar formato: CATEGORIA_DESCRIPCION.md               │
│     • Sin emojis, lenguaje técnico preciso                 │
│     • Actualizar versión y fecha                           │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 5: Actualizar Documentación Relacionada              │
├─────────────────────────────────────────────────────────────┤
│  • Si cambió código → Actualizar doc técnica               │
│  • Si cambió arquitectura → Actualizar ARQUITECTURA_*.md   │
│  • Si agregó funcionalidad → Actualizar TECHNICAL_GLOSSARY │
│  • Incrementar versión del documento                       │
│  • Actualizar fecha                                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 6: Ejecutar Verificaciones                           │
├─────────────────────────────────────────────────────────────┤
│  1. Compilar código:                                       │
│     SovereignProtocol.bat                                  │
│                                                             │
│  2. Ejecutar tests (si existen):                           │
│     java -cp bin --enable-preview sv.volcan.bus.Test_*     │
│                                                             │
│  3. Verificar sincronización:                              │
│     update.bat                                             │
│                                                             │
│  4. Revisar reporte:                                       │
│     type sync_report_YYYYMMDD.txt                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 7: Actualizar Documentos de Seguimiento              │
├─────────────────────────────────────────────────────────────┤
│  A. ACTUALIZACIONES_PENDIENTES.md:                         │
│     - [x] Tarea completada (fecha)                         │
│                                                             │
│  B. TASK_AAA_COMPLIANCE.md:                                │
│     - [x] Marcar tarea como completada                     │
│                                                             │
│  C. LISTA_PENDIENTES.md:                                   │
│     - Actualizar métricas de progreso                      │
│     - Actualizar historial de versiones                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  PASO 8: Preparar Commit (si aplica)                       │
├─────────────────────────────────────────────────────────────┤
│  Seguir GUIA_COMMITS.md:                                   │
│                                                             │
│  [CATEGORIA] Descripción breve                             │
│                                                             │
│  Descripción detallada:                                    │
│  - Qué se cambió                                           │
│  - Por qué se cambió                                       │
│  - Impacto del cambio                                      │
│                                                             │
│  Documento: [NOMBRE_DOCUMENTO.md]                          │
│  Versión: [X.Y]                                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    FIN: Cambio Completado                   │
└─────────────────────────────────────────────────────────────┘
```

---

## Ejemplo Práctico: Crear Test_BusBenchmark.java

### Paso 1: Consultar Documentación

```
1. Abrir LISTA_PENDIENTES.md
   → Encontrar: "Crear Test_BusBenchmark.java" (Prioridad CRÍTICA)

2. Abrir PLAN_IMPLEMENTACION_AAA_COMPLIANCE.md
   → Revisar especificación del benchmark

3. Abrir AAA_CERTIFICATION.md
   → Revisar criterios AAA+ (<150ns, >10M/s)
```

---

### Paso 2: Identificar Tipo de Cambio

```
Tipo: [TEST]
Razón: Crear nuevo test de benchmark
```

---

### Paso 3: Marcar como En Progreso

**Archivo**: `TASK_AAA_COMPLIANCE.md`

```markdown
## Fase 3: Creación de Benchmark AAA+
- [/] Crear Test_BusBenchmark.java  ← CAMBIAR AQUÍ
  - [ ] Implementar test de latencia offer()
  - [ ] Implementar test de latencia poll()
  ...
```

---

### Paso 4: Realizar el Cambio

**Crear archivo**: `src/sv/volcan/bus/Test_BusBenchmark.java`

```java
package sv.volcan.bus;

/**
 * AUTORIDAD: Marvin-Dev
 * RESPONSABILIDAD: Validación de métricas AAA+
 * ...
 */
public class Test_BusBenchmark {
    // Implementación...
}
```

---

### Paso 5: Actualizar Documentación Relacionada

**Actualizar**: `AAA_CODING_STANDARDS.md`

```markdown
## Tests de Rendimiento

### Test_BusBenchmark.java

Valida cumplimiento AAA+:
- Latencia <150ns
- Throughput >10M/s
...

**Versión**: 2.0 → 2.1
**Fecha**: 2026-01-05
```

---

### Paso 6: Ejecutar Verificaciones

```batch
# 1. Compilar
SovereignProtocol.bat

# 2. Ejecutar test
java -cp bin --enable-preview sv.volcan.bus.Test_BusBenchmark

# 3. Verificar sincronización
update.bat

# 4. Revisar reporte
type sync_report_20260105.txt
```

---

### Paso 7: Actualizar Seguimiento

**A. ACTUALIZACIONES_PENDIENTES.md**:
```markdown
### Completadas
- [x] Crear Test_BusBenchmark.java (2026-01-05)
```

**B. TASK_AAA_COMPLIANCE.md**:
```markdown
- [x] Crear Test_BusBenchmark.java
  - [x] Implementar test de latencia offer()
  - [x] Implementar test de latencia poll()
```

**C. LISTA_PENDIENTES.md**:
```markdown
## Fase 3: Benchmarks AAA+ (33% Completado)
- [x] Crear Test_BusBenchmark.java (2026-01-05)
```

---

### Paso 8: Preparar Commit

```
[TEST] Crear Test_BusBenchmark para validación AAA+

Implementado:
- Test de latencia offer() (<150ns)
- Test de latencia poll() (<150ns)
- Test de throughput batchOffer() (>10M/s)
- Test de throughput batchPoll() (>10M/s)
- Cálculo de percentiles (p50, p95, p99)
- Warm-up de 100,000 iteraciones

Métricas objetivo:
- Latencia: <150ns
- Throughput: >10M eventos/s

Documento: AAA_CERTIFICATION.md
Versión: 1.0
```

---

## Flujo para Diferentes Tipos de Cambios

### [FEAT] - Nueva Funcionalidad

```
1. Consultar documentación → LISTA_PENDIENTES.md
2. Marcar [/] en TASK_*.md
3. Crear código siguiendo AAA_CODING_STANDARDS.md
4. Actualizar documentación técnica
5. Crear tests si es necesario
6. Compilar y verificar
7. Actualizar ACTUALIZACIONES_PENDIENTES.md
8. Commit con formato [FEAT]
```

---

### [FIX] - Corrección de Bug

```
1. Identificar bug en código
2. Consultar AAA_CODING_STANDARDS.md
3. Marcar [/] en TASK_*.md
4. Corregir código
5. Actualizar documentación si cambió comportamiento
6. Ejecutar tests
7. Verificar que bug está resuelto
8. Actualizar ACTUALIZACIONES_PENDIENTES.md
9. Commit con formato [FIX]
```

---

### [DOCS] - Documentación

```
1. Identificar documento a crear/actualizar
2. Seguir ESTANDAR_DOCUMENTACION.md
3. Usar formato CATEGORIA_DESCRIPCION.md
4. Sin emojis, lenguaje técnico
5. Incrementar versión
6. Actualizar fecha
7. Ejecutar update.bat
8. Actualizar ACTUALIZACIONES_PENDIENTES.md
9. Commit con formato [DOCS]
```

---

### [REFACTOR] - Refactorización

```
1. Identificar código a mejorar
2. Consultar AAA_CODING_STANDARDS.md
3. Marcar [/] en TASK_*.md
4. Refactorizar manteniendo funcionalidad
5. Ejecutar tests (deben pasar todos)
6. Actualizar documentación si cambió estructura
7. Compilar y verificar
8. Actualizar ACTUALIZACIONES_PENDIENTES.md
9. Commit con formato [REFACTOR]
```

---

## Checklist de Verificación

Antes de considerar un cambio completo:

- [ ] Código compila sin errores
- [ ] Tests pasan correctamente (si existen)
- [ ] Documentación actualizada
- [ ] Versión incrementada (si aplica)
- [ ] Fecha actualizada
- [ ] ACTUALIZACIONES_PENDIENTES.md actualizado
- [ ] TASK_*.md marcado como [x]
- [ ] update.bat ejecutado sin errores
- [ ] Mensaje de commit sigue formato GUIA_COMMITS.md

---

## Herramientas de Soporte

### Documentos de Referencia

1. **LISTA_PENDIENTES.md** - Qué hacer y en qué orden
2. **ACTUALIZACIONES_PENDIENTES.md** - Estado actual
3. **ESTANDAR_DOCUMENTACION.md** - Cómo documentar
4. **GUIA_COMMITS.md** - Cómo hacer commits
5. **AAA_CODING_STANDARDS.md** - Cómo escribir código

### Scripts de Automatización

1. **update.bat** - Verificar sincronización
2. **SovereignProtocol.bat** - Compilar proyecto
3. **compile.bat** - Compilación rápida
4. **ignite.bat** - Arranque rápido

---

## Flujo de Decisión

```
┌─────────────────────────────────────┐
│ ¿Qué necesito hacer?                │
└──────────────┬──────────────────────┘
               │
               ▼
    ┌──────────────────────┐
    │ ¿Es código nuevo?    │
    └──┬────────────────┬──┘
       │ Sí             │ No
       ▼                ▼
┌──────────────┐  ┌──────────────┐
│ [FEAT]       │  │ ¿Es bug?     │
│ Crear código │  └──┬────────┬──┘
└──────────────┘     │ Sí     │ No
                     ▼        ▼
              ┌──────────┐  ┌──────────────┐
              │ [FIX]    │  │ ¿Es mejora?  │
              │ Corregir │  └──┬────────┬──┘
              └──────────┘     │ Sí     │ No
                               ▼        ▼
                        ┌──────────┐  ┌──────────┐
                        │[REFACTOR]│  │ [DOCS]   │
                        │ Mejorar  │  │Documentar│
                        └──────────┘  └──────────┘
```

---

## Resumen del Flujo

1. **Consultar** → LISTA_PENDIENTES.md
2. **Marcar** → TASK_*.md como [/]
3. **Hacer** → Cambio en código/docs
4. **Actualizar** → Documentación relacionada
5. **Verificar** → Compilar + Tests + update.bat
6. **Registrar** → ACTUALIZACIONES_PENDIENTES.md
7. **Completar** → TASK_*.md como [x]
8. **Commit** → Seguir GUIA_COMMITS.md

---

**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Estado**: Activo
