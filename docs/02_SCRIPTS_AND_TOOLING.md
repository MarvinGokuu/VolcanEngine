# 02 - SCRIPTS AND TOOLING GUIDE

En VolcanEngine existen múltiples niveles de scripts porque algunos están orientados a depuración rápida, otros a integración continua y otros a mantenimiento interno del código. Aquí tienes la referencia oficial de cómo usar la infraestructura de scripts.

## 1. 🚀 Producción y Ejecución
Estos scripts están diseñados para compilar y ejecutar el servidor Backend (Headless).

*   **`build.bat` (Compilador Incremental)**
    *   **Propósito**: Compila los archivos listados en `compile_list.txt` de manera extremadamente rápida inyectando las directivas necesarias (SIMD, Panama).
    *   **Uso**: `.\build.bat`

*   **`run.bat` (El Lanzador del Servidor)**
    *   **Propósito**: Levanta `VolcanEngineMaster.java` con las banderas de JVM para ultra-baja latencia (ZGC, PreTouch).
    *   **Uso**: `.\run.bat`

*   **`build_release.bat` (Empaquetador de Distribución)**
    *   **Propósito**: Genera la compilación final "Zero-Debug" (-g:none) descartando archivos de test y usando `jpackage` para crear un ejecutable puro (Standalone) que no requiere que el servidor destino tenga Java instalado.
    *   **Uso**: `.\build_release.bat`

*   **`clean.bat` (Limpiador de Estado)**
    *   **Propósito**: Destruye la carpeta `bin/` y elimina todos los logs obsoletos garantizando una compilación limpia.
    *   **Uso**: `.\clean.bat`

## 2. 🧪 Testing y Certificación (AAA+)
La validación de latencia y la salud de la memoria son imperativos en VolcanEngine.

*   **`test.bat` (La Suite Maestra de Pruebas)**
    *   **Propósito**: Ejecuta secuencialmente las pruebas unitarias y de estrés del núcleo.
    *   **Salida**: Genera un archivo de logs y la salida estándar de errores, certificando que no hay violaciones de memoria, Zero-GC leaks ni dependencias circulares.
    *   **Uso**: `.\test.bat`

*   **`benchmark.bat` (JMH Micro-Profiler)**
    *   **Propósito**: Compila y lanza la suite de micro-perfilado (Java Microbenchmark Harness). Se utiliza para medir tiempos de ejecución de las rutinas SIMD y SoA en nanosegundos puros (midiendo Caché L1/L2 hits).
    *   **Uso**: `.\benchmark.bat` (Toma varios minutos por la fase de pre-calentamiento del JIT Compiler).

## 3. 🛡️ Herramientas de Diagnóstico (`tools/`)
Scripts especializados en la auditoría técnica de la infraestructura del servidor.

*   **`tools\monitoreo.ps1` (Guardián del Entorno)**
    *   **Propósito**: Realiza una auditoría Post-Mortem del sistema. Escanea el sistema operativo en busca de procesos Java zombies (fugas de memoria) y permite aniquilarlos. Además, escanea puertos UDP abiertos y lanza una pasada rápida de validación del Kernel.
    *   **Uso**: `powershell.exe -File .\tools\monitoreo.ps1`
    *   **Propósito**: Ejecuta secuencialmente las pruebas unitarias y de estrés del núcleo.
    *   **Salida**: Genera un archivo de logs y la salida estándar de errores, certificando que no hay violaciones de memoria, Zero-GC leaks ni dependencias circulares.
    *   **Uso**: `.\test.bat`

*   **`benchmark.bat` (JMH Micro-Profiler)**
    *   **Propósito**: Compila y lanza la suite de micro-perfilado (Java Microbenchmark Harness). Se utiliza para medir tiempos de ejecución de las rutinas SIMD y SoA en nanosegundos puros (midiendo Caché L1/L2 hits).
    *   **Uso**: `.\benchmark.bat` (Toma varios minutos por la fase de pre-calentamiento del JIT Compiler).

## 3. 🛠️ Limpieza de Scripts Obsoletos
Como parte de la migración Headless Server, han sido depurados y destruidos permanentemente scripts obsoletos como `run_audio_test.bat`, `exe.bat`, `bbuild.bat`, así como herramientas nativas asociadas a la VRAM o Audio.
