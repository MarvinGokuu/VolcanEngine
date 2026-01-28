# GUÍA DE DESARROLLO COMPLETA - VOLCAN ENGINE
## Todo lo que Necesitas para Desarrollar en el Motor

**Fecha:** 2026-01-19  
**Nivel:** Intermedio a Avanzado  
**Objetivo:** Guía práctica para desarrollo activo

---

## 🎯 INTRODUCCIÓN

Esta guía cubre **todo lo que falta** en la documentación existente para que puedas desarrollar efectivamente en el VolcanEngine.

---

## 📋 TABLA DE CONTENIDOS

1. [Setup del Entorno de Desarrollo](#setup-del-entorno)
2. [Flujo de Trabajo Diario](#flujo-de-trabajo-diario)
3. [Crear Nuevos Componentes](#crear-nuevos-componentes)
4. [Debugging Avanzado](#debugging-avanzado)
5. [Profiling y Optimización](#profiling-y-optimización)
6. [Testing](#testing)
7. [Contribuir al Proyecto](#contribuir-al-proyecto)
8. [Troubleshooting Común](#troubleshooting-común)

---

## 🔧 SETUP DEL ENTORNO

### **1. Requisitos**

#### **Software Necesario:**
- **Java 25** (JDK)
- **Git**
- **IDE:** IntelliJ IDEA, VS Code, o Eclipse
- **Terminal:** CMD (Windows), Bash (Linux/Mac)

#### **Hardware Recomendado:**
- **CPU:** 4+ cores (mejor con AVX2/AVX-512)
- **RAM:** 8GB+ (16GB recomendado)
- **Disco:** SSD (para compilación rápida)

---

### **2. Instalación de Java 25**

#### **Windows:**
```batch
# Descargar de Oracle o OpenJDK
https://www.oracle.com/java/technologies/downloads/
https://jdk.java.net/25/

# Verificar instalación
java --version
javac --version
```

#### **Linux/Mac:**
```bash
# Usando SDKMAN
sdk install java 25-open

# Verificar
java --version
```

---

### **3. Configuración del IDE**

#### **IntelliJ IDEA (Recomendado):**

1. **Abrir Proyecto:**
   ```
   File → Open → Seleccionar carpeta VolcanEngine
   ```

2. **Configurar JDK:**
   ```
   File → Project Structure → Project
   SDK: Java 25
   Language Level: 25 (Preview)
   ```

3. **Habilitar Preview Features:**
   ```
   File → Settings → Build, Execution, Deployment → Compiler → Java Compiler
   Additional command line parameters: --enable-preview --add-modules jdk.incubator.vector
   ```

4. **Configurar Run Configuration:**
   ```
   Run → Edit Configurations → Add New → Application
   Main class: sv.volcan.state.VolcanEngineMaster
   VM options: --enable-preview --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector
   ```

---

#### **VS Code:**

1. **Instalar Extensiones:**
   - Extension Pack for Java
   - Debugger for Java

2. **Configurar `.vscode/settings.json`:**
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-25",
         "path": "C:\\Program Files\\Java\\jdk-25",
         "default": true
       }
     ],
     "java.compile.nullAnalysis.mode": "automatic",
     "java.jdt.ls.vmargs": "--enable-preview --add-modules jdk.incubator.vector"
   }
   ```

3. **Configurar `.vscode/launch.json`:**
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "VolcanEngine",
         "request": "launch",
         "mainClass": "sv.volcan.state.VolcanEngineMaster",
         "vmArgs": "--enable-preview --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector"
       }
     ]
   }
   ```

---

## 🔄 FLUJO DE TRABAJO DIARIO

### **Workflow Típico:**

```
1. Pull latest changes
   ↓
2. Create feature branch
   ↓
3. Develop & Test
   ↓
4. Commit changes
   ↓
5. Push & Create PR
```

---

### **1. Actualizar Código**

```batch
git pull origin main
```

---

### **2. Crear Branch de Feature**

```batch
git checkout -b feature/nombre-descriptivo

# Ejemplos:
git checkout -b feature/collision-system
git checkout -b fix/bus-memory-leak
git checkout -b perf/simd-optimization
```

---

### **3. Desarrollo Iterativo**

#### **Compilar:**
```batch
build.bat
```

#### **Ejecutar:**
```batch
run.bat
```

#### **Compilar Solo Cambios:**
```batch
compile.bat
```

---

### **4. Testing**

```batch
# Test específico
java -cp bin sv.volcan.bus.BusBenchmarkTest

# Todos los tests
java -cp bin sv.volcan.test.UltraFastBootTest
java -cp bin sv.volcan.test.GracefulShutdownTest
java -cp bin sv.volcan.test.PowerSavingTest
```

---

### **5. Commit Changes**

```batch
git add .
git commit -m "feat: Add collision detection system"

# Convención de commits:
# feat: Nueva funcionalidad
# fix: Corrección de bug
# perf: Mejora de rendimiento
# docs: Cambios en documentación
# test: Agregar o modificar tests
# refactor: Refactorización de código
```

---

### **6. Push & Pull Request**

```batch
git push origin feature/nombre-descriptivo

# Luego crear PR en GitHub
```

---

## 🏗️ CREAR NUEVOS COMPONENTES

### **1. Crear un Sistema Nuevo**

#### **Paso 1: Crear el Archivo**

```java
// src/sv/volcan/core/systems/CollisionSystem.java
package sv.volcan.core.systems;

import sv.volcan.state.WorldStateFrame;

/**
 * AUTORIDAD: [Tu Nombre]
 * RESPONSABILIDAD: Detección de colisiones entre entidades
 * DEPENDENCIAS: WorldStateFrame
 * MÉTRICAS: Latencia <1ms para 1000 entidades
 */
public class CollisionSystem implements GameSystem {
    
    @Override
    public String getName() {
        return "CollisionSystem";
    }
    
    @Override
    public void update(WorldStateFrame state, double deltaTime) {
        // Tu lógica aquí
        System.out.println("[COLLISION] Checking collisions...");
    }
}
```

---

#### **Paso 2: Registrar el Sistema**

```java
// En EngineKernel.java o VolcanEngineMaster.java
import sv.volcan.core.systems.CollisionSystem;

// En el método de inicialización:
systemRegistry.registerSystem(new CollisionSystem());
```

---

#### **Paso 3: Compilar y Probar**

```batch
build.bat
```

---

### **2. Crear un Bus Especializado**

#### **Ejemplo: Bus de Colisiones**

```java
// src/sv/volcan/bus/CollisionBus.java
package sv.volcan.bus;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class CollisionBus implements IEventBus {
    
    // Padding para cache line alignment
    long headShield_L1_slot1, headShield_L1_slot2, headShield_L1_slot3,
         headShield_L1_slot4, headShield_L1_slot5, headShield_L1_slot6,
         headShield_L1_slot7;
    
    volatile long head = 0;
    
    long isolationBridge_slot1, isolationBridge_slot2, isolationBridge_slot3,
         isolationBridge_slot4, isolationBridge_slot5, isolationBridge_slot6,
         isolationBridge_slot7;
    
    volatile long tail = 0;
    
    long tailShield_L1_slot1, tailShield_L1_slot2, tailShield_L1_slot3,
         tailShield_L1_slot4, tailShield_L1_slot5, tailShield_L1_slot6,
         tailShield_L1_slot7;
    
    private final long[] buffer;
    private final int mask;
    
    private static final VarHandle HEAD_H;
    private static final VarHandle TAIL_H;
    
    static {
        try {
            var lookup = MethodHandles.lookup();
            HEAD_H = lookup.findVarHandle(CollisionBus.class, "head", long.class);
            TAIL_H = lookup.findVarHandle(CollisionBus.class, "tail", long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error("Fallo crítico en CollisionBus");
        }
    }
    
    public CollisionBus(int powerOfTwo) {
        int capacity = 1 << powerOfTwo;
        this.buffer = new long[capacity];
        this.mask = capacity - 1;
    }
    
    @Override
    public boolean offer(long eventData) {
        long currentTail = (long) TAIL_H.getAcquire(this);
        long currentHead = (long) HEAD_H.getAcquire(this);
        
        if (currentTail - currentHead >= buffer.length) {
            return false;
        }
        
        buffer[(int) (currentTail & mask)] = eventData;
        TAIL_H.setRelease(this, currentTail + 1);
        return true;
    }
    
    @Override
    public long poll() {
        long currentHead = (long) HEAD_H.getAcquire(this);
        long currentTail = (long) TAIL_H.getAcquire(this);
        
        if (currentHead >= currentTail) {
            return -1L;
        }
        
        long eventData = buffer[(int) (currentHead & mask)];
        HEAD_H.setRelease(this, currentHead + 1);
        return eventData;
    }
    
    // Implementar otros métodos de IEventBus...
}
```

---

### **3. Crear un Test**

```java
// src/sv/volcan/test/CollisionSystemTest.java
package sv.volcan.test;

import sv.volcan.core.systems.CollisionSystem;
import sv.volcan.state.WorldStateFrame;

public class CollisionSystemTest {
    
    public static void main(String[] args) {
        System.out.println("[TEST] CollisionSystem");
        
        // Setup
        CollisionSystem system = new CollisionSystem();
        
        // Test 1: Nombre del sistema
        String name = system.getName();
        assert name.equals("CollisionSystem") : "Nombre incorrecto";
        System.out.println("✅ Test 1: Nombre correcto");
        
        // Test 2: Update sin errores
        try {
            system.update(null, 0.016); // 60 FPS
            System.out.println("✅ Test 2: Update sin errores");
        } catch (Exception e) {
            System.out.println("❌ Test 2: Update falló");
            e.printStackTrace();
        }
        
        System.out.println("[TEST] CollisionSystem - COMPLETADO");
    }
}
```

---

## 🐛 DEBUGGING AVANZADO

### **1. Logs del Motor**

El motor imprime logs detallados en consola:

```
[KERNEL] STARTUP SEQUENCE START
[KERNEL] Logic Thread PINNED to Core 1
[KERNEL] EXECUTING JIT WARM-UP...
[KERNEL] EXECUTING BOOT SEQUENCE...
[KERNEL] Boot Time: 0.290ms
[KERNEL] MAIN LOOP STARTED
```

---

### **2. Agregar Logs Personalizados**

```java
public class MiSistema implements GameSystem {
    
    @Override
    public void update(WorldStateFrame state, double deltaTime) {
        System.out.println("[MI_SISTEMA] Delta: " + deltaTime + "ms");
        
        // Tu lógica
        int entities = countEntities(state);
        System.out.println("[MI_SISTEMA] Entities: " + entities);
    }
}
```

---

### **3. Debugging con IntelliJ**

#### **Breakpoints:**
1. Click en el margen izquierdo del editor
2. Run → Debug 'VolcanEngine'
3. El programa se detendrá en el breakpoint

#### **Inspeccionar Variables:**
- Hover sobre variables para ver valores
- Ventana "Variables" muestra todas las variables locales
- Ventana "Watches" para expresiones personalizadas

#### **Step Through Code:**
- **F8:** Step Over (siguiente línea)
- **F7:** Step Into (entrar en método)
- **Shift+F8:** Step Out (salir de método)

---

### **4. Debugging de VarHandles**

```java
// Agregar logs para ver valores
public boolean offer(long eventData) {
    long currentTail = (long) TAIL_H.getAcquire(this);
    long currentHead = (long) HEAD_H.getAcquire(this);
    
    System.out.println("[DEBUG] head=" + currentHead + ", tail=" + currentTail);
    
    if (currentTail - currentHead >= buffer.length) {
        System.out.println("[DEBUG] Buffer lleno!");
        return false;
    }
    
    // ...
}
```

---

## 📊 PROFILING Y OPTIMIZACIÓN

### **1. JDK Flight Recorder (JFR)**

#### **Grabar:**
```batch
java -XX:StartFlightRecording=filename=recording.jfr,duration=60s -cp bin sv.volcan.state.VolcanEngineMaster
```

#### **Analizar:**
```batch
jfr print recording.jfr > analysis.txt
```

#### **Visualizar (JMC):**
```batch
# Descargar JDK Mission Control
https://www.oracle.com/java/technologies/jdk-mission-control.html

# Abrir recording.jfr
```

---

### **2. GC Logs**

Ya configurado en `build.bat`:

```batch
-Xlog:gc*:file=gc_production.log:time,uptime,level,tags
```

#### **Analizar:**
```batch
type gc_production.log | findstr "Pause"
```

**Buscar:**
- Pausas largas (>1ms)
- Frecuencia de GC
- Heap usage

---

### **3. JIT Compilation Logs**

```batch
java -XX:+PrintCompilation -cp bin sv.volcan.state.VolcanEngineMaster
```

**Output:**
```
    1    1       3       java.lang.String::hashCode (49 bytes)
    2    2       3       java.lang.String::charAt (25 bytes)
  100   50  n    0       java.lang.System::arraycopy (native)
```

**Columnas:**
- Timestamp
- Compilation ID
- Tier (0-4, 4 = C2 optimizado)
- Method

---

### **4. Benchmarking Manual**

```java
public class Benchmark {
    
    public static void main(String[] args) {
        VolcanAtomicBus bus = new VolcanAtomicBus(14); // 16K capacity
        
        // Warm-up
        for (int i = 0; i < 100_000; i++) {
            bus.offer(i);
            bus.poll();
        }
        
        // Benchmark
        int iterations = 10_000_000;
        long start = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            bus.offer(i);
        }
        
        long end = System.nanoTime();
        long totalNs = end - start;
        double avgNs = (double) totalNs / iterations;
        
        System.out.println("Total: " + totalNs + "ns");
        System.out.println("Avg: " + avgNs + "ns per operation");
        System.out.println("Throughput: " + (iterations / (totalNs / 1_000_000_000.0)) + " ops/s");
    }
}
```

---

## 🧪 TESTING

### **1. Estructura de Tests**

```
src/sv/volcan/test/
├── Test_BusBenchmark.java       # Benchmark de buses
├── UltraFastBootTest.java      # Boot time <1ms
├── GracefulShutdownTest.java   # Shutdown limpio
├── PowerSavingTest.java        # Idle system
└── Test_MiComponente.java       # Tu test
```

---

### **2. Template de Test**

```java
package sv.volcan.test;

public class MiComponenteTest {
    
    public static void main(String[] args) {
        System.out.println("[TEST] MiComponente - START");
        
        try {
            testFuncionalidad1();
            testFuncionalidad2();
            testRendimiento();
            
            System.out.println("[TEST] MiComponente - ✅ PASSED");
        } catch (AssertionError e) {
            System.out.println("[TEST] MiComponente - ❌ FAILED");
            e.printStackTrace();
        }
    }
    
    private static void testFuncionalidad1() {
        // Arrange
        MiComponente comp = new MiComponente();
        
        // Act
        int result = comp.doSomething();
        
        // Assert
        assert result == 42 : "Expected 42, got " + result;
        System.out.println("✅ testFuncionalidad1");
    }
    
    private static void testFuncionalidad2() {
        // ...
    }
    
    private static void testRendimiento() {
        MiComponente comp = new MiComponente();
        
        long start = System.nanoTime();
        comp.doSomething();
        long end = System.nanoTime();
        
        long latency = end - start;
        assert latency < 1_000_000 : "Latencia muy alta: " + latency + "ns";
        System.out.println("✅ testRendimiento: " + latency + "ns");
    }
}
```

---

### **3. Ejecutar Tests**

```batch
# Compilar
build.bat

# Ejecutar test específico
java -cp bin sv.volcan.test.MiComponenteTest

# Ejecutar todos los tests (crear script)
@echo off
echo [TESTS] Running all tests...
java -cp bin sv.volcan.bus.BusBenchmarkTest
java -cp bin sv.volcan.test.UltraFastBootTest
java -cp bin sv.volcan.test.GracefulShutdownTest
java -cp bin sv.volcan.test.PowerSavingTest
echo [TESTS] Complete
```

---

## 🤝 CONTRIBUIR AL PROYECTO

### **1. Estándares de Código**

#### **Naming Conventions:**
- **Clases:** `PascalCase` (ej: `VolcanAtomicBus`)
- **Métodos:** `camelCase` (ej: `getAcquire`)
- **Variables:** `camelCase` (ej: `currentHead`)
- **Constantes:** `UPPER_SNAKE_CASE` (ej: `MEMORY_SIGNATURE`)
- **VarHandles:** `UPPER_SNAKE_CASE_H` (ej: `HEAD_H`)

#### **Documentación:**
```java
/**
 * AUTORIDAD: [Tu Nombre]
 * RESPONSABILIDAD: [Qué hace este componente]
 * DEPENDENCIAS: [Qué otros componentes usa]
 * MÉTRICAS: [Latencia, throughput, etc.]
 */
```

---

### **2. Pull Request Checklist**

- [ ] Código compila sin errores
- [ ] Tests pasan
- [ ] Documentación actualizada
- [ ] Commit messages descriptivos
- [ ] Sin warnings del compilador
- [ ] Performance no degradado

---

### **3. Code Review**

**Qué buscar:**
- Correctness (funciona correctamente)
- Performance (no introduce latencia)
- Memory safety (sin leaks)
- Thread safety (si aplica)
- Documentation (comentarios claros)

---

## 🚨 TROUBLESHOOTING COMÚN

### **Problema 1: Compilación Falla**

**Síntoma:**
```
error: cannot find symbol
  symbol:   class Arena
  location: package java.lang.foreign
```

**Solución:**
```batch
# Verificar Java 25
java --version

# Verificar flags en build.bat
--enable-preview
--add-modules jdk.incubator.vector
```

---

### **Problema 2: Runtime Crash**

**Síntoma:**
```
java.lang.IllegalAccessError: class ... cannot access class java.lang.foreign.Arena
```

**Solución:**
```batch
# Agregar flags en runtime
--enable-preview
--enable-native-access=ALL-UNNAMED
```

---

### **Problema 3: Performance Degradado**

**Síntoma:**
- FPS bajo
- Latencia alta
- GC pausas largas

**Solución:**
```batch
# 1. Verificar GC logs
type gc_production.log | findstr "Pause"

# 2. Verificar JIT compilation
java -XX:+PrintCompilation -cp bin ...

# 3. Profiling con JFR
java -XX:StartFlightRecording=... -cp bin ...
```

---

### **Problema 4: Memory Leak**

**Síntoma:**
- Heap usage crece constantemente
- OutOfMemoryError

**Solución:**
```batch
# 1. Heap dump
java -XX:+HeapDumpOnOutOfMemoryError -cp bin ...

# 2. Analizar con VisualVM o JProfiler

# 3. Buscar:
# - Arenas no cerradas
# - Buses no limpiados
# - Referencias no liberadas
```

---

## 📚 RECURSOS ADICIONALES

### **Documentación del Proyecto:**
- `docs/README_DOCS.md` - Punto de entrada
- `docs/PROJECT_HEALTH_REPORT.md` - Estado del proyecto
- `docs/VARHANDLE_PANAMA_MASTERY.md` - Guía de VarHandles
- `docs/DOCUMENTATION_INDEX.md` - Índice completo

### **Documentación Externa:**
- [Java 25 Documentation](https://docs.oracle.com/en/java/javase/25/)
- [Project Panama](https://openjdk.org/projects/panama/)
- [Vector API](https://openjdk.org/jeps/338)
- [ZGC](https://wiki.openjdk.org/display/zgc)

---

## ✅ CHECKLIST DE DESARROLLO

### **Setup Inicial:**
- [ ] Java 25 instalado
- [ ] Proyecto clonado
- [ ] IDE configurado
- [ ] Compilación exitosa
- [ ] Tests ejecutados

### **Desarrollo Diario:**
- [ ] Pull latest changes
- [ ] Create feature branch
- [ ] Develop & test
- [ ] Commit with descriptive message
- [ ] Push & create PR

### **Antes de Commit:**
- [ ] Código compila
- [ ] Tests pasan
- [ ] Sin warnings
- [ ] Documentación actualizada
- [ ] Performance validado

---

**Última Actualización:** 2026-01-19  
**Autor:** System Architect  
**Estado:** ✅ Guía Completa
