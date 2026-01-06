# Estándar de Documentación - Volcan Engine

## Autoridad

**Documento**: Estándar de Documentación Técnica  
**Nivel**: Documentation Engineering  
**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Propósito**: Establecer formato y convenciones para toda la documentación del proyecto

---

## Principios Fundamentales

### 1. Claridad Técnica
- Usar lenguaje técnico preciso, no genérico
- Evitar ambigüedades
- Definir términos especializados

### 2. Consistencia
- Todos los documentos siguen el mismo formato
- Nomenclatura uniforme
- Estructura predecible

### 3. Mantenibilidad
- Documentación actualizada con cada cambio
- Historial de versiones claro
- Referencias cruzadas funcionales

### 4. Profesionalismo
- Sin emojis en documentación técnica
- Ortografía y gramática correctas
- Formato limpio y legible

---

## Formato Estándar de Documento

### Encabezado Obligatorio

Todos los documentos técnicos deben iniciar con:

```markdown
# [Título Descriptivo] - Volcan Engine

## Autoridad

**Documento**: [Nombre completo del documento]  
**Nivel**: [Categoría: Architecture | Implementation | Testing | Documentation]  
**Versión**: [X.Y]  
**Fecha**: [YYYY-MM-DD]  
**Autor**: Marvin-Dev  
**Propósito**: [Descripción breve del propósito del documento]

---
```

### Secciones Estándar

#### Para Documentos de Arquitectura
1. Visión General
2. Componentes Principales
3. Flujo de Datos
4. Decisiones de Diseño
5. Referencias

#### Para Documentos de Implementación
1. Objetivo
2. Cambios Propuestos
3. Plan de Verificación
4. Estrategia de Rollback
5. Orden de Ejecución

#### Para Documentos de Testing
1. Objetivo de las Pruebas
2. Configuración del Entorno
3. Casos de Prueba
4. Criterios de Éxito
5. Resultados

#### Para Documentos de Certificación
1. Definición de Estándares
2. Criterios de Cumplimiento
3. Proceso de Validación
4. Checklist de Certificación
5. Formato de Certificado

---

## Convenciones de Nomenclatura

### Nombres de Archivos

**Formato**: `[CATEGORIA]_[DESCRIPCION_ESPECIFICA].md`

**Categorías válidas**:
- `AAA_` - Estándares y certificación AAA+
- `ARQUITECTURA_` - Documentos de arquitectura
- `DOCUMENTACION_` - Guías y documentación general
- `TECHNICAL_` - Glosarios y referencias técnicas
- `SIGNAL_` - Documentación de sistemas de señales
- `PLAN_` - Planes de implementación/documentación
- `TASK_` - Tareas específicas del proyecto
- `WALKTHROUGH_` - Recorridos de implementación

**Ejemplos correctos**:
- `AAA_CERTIFICATION.md` ✓
- `AAA_CODING_STANDARDS.md` ✓
- `ARQUITECTURA_VOLCAN_ENGINE.md` ✓
- `DOCUMENTACION_BUS.md` ✓
- `TECHNICAL_GLOSSARY.md` ✓
- `PLAN_IMPLEMENTACION_BOOT_SELECTOR.md` ✓
- `TASK_AAA_COMPLIANCE.md` ✓
- `WALKTHROUGH_AUTHOR_UPDATE.md` ✓

**Ejemplos incorrectos**:
- `implementation_plan.md` ✗ (genérico)
- `task.md` ✗ (genérico)
- `walkthrough.md` ✗ (genérico)
- `documentation_plan.md` ✗ (minúsculas + genérico)

---

## Estructura de Directorios

### Documentación del Proyecto

```
src/sv/volcan/test/documentacion/
├── AAA_CERTIFICATION.md
├── AAA_CODING_STANDARDS.md
├── ARQUITECTURA_VOLCAN_ENGINE.md
├── DOCUMENTACION_BUS.md
├── SIGNAL_DISPATCHER_GUIDE.md
├── TECHNICAL_GLOSSARY.md
├── PLAN_DOCUMENTACION_UNIFICADA.md
├── ACTUALIZACIONES_PENDIENTES.md
└── [otros documentos específicos]
```

### Artifacts de Sesión

```
.gemini/antigravity/brain/[session-id]/
├── PLAN_IMPLEMENTACION_[DESCRIPCION].md
├── TASK_[DESCRIPCION].md
├── WALKTHROUGH_[DESCRIPCION].md
└── [otros artifacts específicos]
```

---

## Formato de Código

### Bloques de Código Java

```markdown
**Mecánica**:
\`\`\`java
// Comentario descriptivo
public boolean offer(long eventData) {
    long currentTail = (long) TAIL_H.getAcquire(this);
    // Implementación...
    return true;
}
\`\`\`
```

### Bloques de Código Batch

```markdown
**Script de Compilación**:
\`\`\`batch
@echo off
echo [KERNEL] Iniciando compilación...
javac -d bin --enable-preview --source 25 src\\sv\\volcan\\*.java
\`\`\`
```

---

## Formato de Tablas

### Tablas de Métricas

```markdown
| Métrica | Valor Objetivo | Valor Actual | Estado |
|---------|----------------|--------------|--------|
| Latencia offer | <150ns | 142ns | PASS |
| Throughput batch | >10M/s | 12.3M/s | PASS |
```

### Tablas de Comparación

```markdown
| Aspecto | Antes | Después | Mejora |
|---------|-------|---------|--------|
| Latencia | 1000ns | 150ns | 6.6x |
| Throughput | 1M/s | 10M/s | 10x |
```

---

## Referencias Cruzadas

### Enlaces a Documentos

```markdown
Ver [AAA_CODING_STANDARDS.md](file:///ruta/completa/AAA_CODING_STANDARDS.md)
```

### Enlaces a Código

```markdown
Ver [VolcanAtomicBus.java](file:///ruta/completa/VolcanAtomicBus.java#L100-L150)
```

### Enlaces a Secciones

```markdown
Ver sección [Análisis de Resiliencia](#análisis-de-resiliencia-de-arranque)
```

---

## Formato de Listas

### Listas de Verificación

```markdown
- [ ] Tarea pendiente
- [/] Tarea en progreso
- [x] Tarea completada
```

### Listas de Características

```markdown
**Características**:
- Latencia <150ns
- Throughput >10M eventos/s
- Lock-free implementation
- Zero-GC en hot-path
```

---

## Formato de Alertas y Notas

### Notas Importantes

```markdown
> **NOTA**: Este componente requiere JDK 25 con Project Panama habilitado.
```

### Advertencias

```markdown
> **ADVERTENCIA**: No modificar variables de padding. Rompe alineación de cache line.
```

### Información Crítica

```markdown
> **CRÍTICO**: El sistema debe pasar todas las validaciones antes de certificación AAA+.
```

---

## Versionado de Documentos

### Formato de Versión

**Esquema**: `MAJOR.MINOR`

- **MAJOR**: Cambios estructurales o de contenido significativo
- **MINOR**: Correcciones, actualizaciones menores, clarificaciones

**Ejemplos**:
- `1.0` - Versión inicial
- `1.1` - Correcciones menores
- `2.0` - Reestructuración completa

### Historial de Cambios

Incluir al final del documento:

```markdown
---

## Historial de Versiones

### Versión 1.1 (2026-01-05)
- Corrección de ortografía en sección 3
- Actualización de métricas de rendimiento

### Versión 1.0 (2026-01-04)
- Versión inicial del documento
```

---

## Documento de Actualizaciones Pendientes

Mantener un archivo `ACTUALIZACIONES_PENDIENTES.md` con:

```markdown
# Actualizaciones Pendientes - Volcan Engine

## Documentación

### Alta Prioridad
- [ ] Actualizar ARQUITECTURA_VOLCAN_ENGINE.md con Signal Dispatcher
- [ ] Renombrar documentos genéricos en artifacts

### Media Prioridad
- [ ] Consolidar ARQUITECTURA_VOLCAN1_ENGINE.md y ARQUITECTURA_VOLCAN_ENGINE.md
- [ ] Actualizar diagramas en DOCUMENTACION_BUS.md

### Baja Prioridad
- [ ] Revisar ortografía en todos los documentos
- [ ] Agregar más ejemplos a TECHNICAL_GLOSSARY.md
```

---

## Checklist de Revisión de Documentos

Antes de considerar un documento completo:

- [ ] Encabezado con toda la información requerida
- [ ] Nombre de archivo sigue convenciones (CATEGORIA_DESCRIPCION.md)
- [ ] Sin emojis en contenido técnico
- [ ] Lenguaje técnico preciso (no genérico)
- [ ] Ortografía y gramática correctas
- [ ] Referencias cruzadas funcionales
- [ ] Bloques de código bien formateados
- [ ] Tablas alineadas correctamente
- [ ] Versión y fecha actualizadas
- [ ] Autor: Marvin-Dev

---

## Proceso de Actualización Continua

### Cuando se hace un cambio en código:

1. **Identificar documentos afectados**
2. **Actualizar documentación relevante**
3. **Incrementar versión del documento**
4. **Actualizar fecha**
5. **Agregar entrada en historial de versiones**
6. **Actualizar ACTUALIZACIONES_PENDIENTES.md**

### Cuando se crea nuevo código:

1. **Crear documentación correspondiente**
2. **Seguir estándar de nomenclatura**
3. **Incluir en índice de documentación**
4. **Agregar referencias cruzadas**

---

## Índice de Documentación

Mantener actualizado en README.md:

```markdown
## Documentación Técnica

### Estándares y Certificación
- [AAA_CERTIFICATION.md](src/sv/volcan/test/documentacion/AAA_CERTIFICATION.md)
- [AAA_CODING_STANDARDS.md](src/sv/volcan/test/documentacion/AAA_CODING_STANDARDS.md)

### Arquitectura
- [ARQUITECTURA_VOLCAN_ENGINE.md](src/sv/volcan/test/documentacion/ARQUITECTURA_VOLCAN_ENGINE.md)
- [DOCUMENTACION_BUS.md](src/sv/volcan/test/documentacion/DOCUMENTACION_BUS.md)

### Guías Técnicas
- [SIGNAL_DISPATCHER_GUIDE.md](src/sv/volcan/test/documentacion/SIGNAL_DISPATCHER_GUIDE.md)
- [TECHNICAL_GLOSSARY.md](src/sv/volcan/test/documentacion/TECHNICAL_GLOSSARY.md)
```

---

**Versión**: 1.0  
**Fecha**: 2026-01-05  
**Autor**: Marvin-Dev  
**Estado**: Activo
