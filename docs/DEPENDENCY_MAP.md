# 🌐 GLOSARIO DE DEPENDENCIAS (AAA+ Dependencies Map)

**Autoridad**: Volcan Engine Architecture  
**Propósito**: Mapa de trazabilidad de dependencias para Auditoría Bio-Segura.  
**Estado**: AAA+ Compliant

---

## 🏛️ CORE (Kernel Domain)

### `SovereignKernel`
*   **Identidad**: Procesador Central Neuronal (Reading Order: `00001010`)
*   **Dependencias**:
    *   `TimeKeeper`: Control temporal de 60 FPS (TSC).
    *   `SystemRegistry`: Registro de sistemas activos.
    *   `VolcanStateVault`: Memoria a largo plazo (State Persistence).
    *   `VolcanEventDispatcher`: Router de eventos (Synapse Router).
    *   `SectorMemoryVault`: Gestión de memoria física por sectores.
    *   `KernelControlRegister`: Máquina de estados de arranque/control.
    *   `MetricsPacker`: Empaquetado de telemetría para Control Plane.

### `UltraFastBootSequence`
*   **Identidad**: Secuencia de Arranque (Reading Order: `N/A`)
*   **Dependencias**:
    *   `SovereignExecutionIntegrity`: Verificación de ambiente seguro.
    *   `VolcanAtomicBus`: Validación de firmas térmicas en buses.

---

## ⚡ BUS & SIGNALS (Transport Domain)

### `VolcanAtomicBus`
*   **Identidad**: Flagship RingBuffer (Reading Order: `00000110`)
*   **Dependencias**:
    *   `IEventBus`: Contrato de interfaz.
    *   `MemorySegment`: Acceso a memoria nativa (si off-heap).
    *   `Unsafe/VarHandles`: Primitivas de concurrencia de hardware.
    *   `@AAACertified`: Metadatos de rectitud de diseño.

### `VolcanSignalDispatcher`
*   **Identidad**: Fachada de Señales (Reading Order: `00001000`)
*   **Dependencias**:
    *   `VolcanAtomicBus`: Motor de transporte subyacente.
    *   `VolcanSignalPacker`: Utilidad para empaquetado binario.

### `VolcanSignalPacker`
*   **Identidad**: Utilidad Binaria (Reading Order: `00000101`)
*   **Dependencias**:
    *   **Ninguna** (Pure Static Utility).
    *   Operaciones de bits nativas (CPU Instructions).

---

## 🧠 MEMORY (State Domain)

### `VolcanStateVault`
*   **Identidad**: Neurona de Memoria (Reading Order: `00000011`)
*   **Dependencias**:
    *   `Java Panama (Foreign Memory API)`: Acceso directo a RAM.
    *   `Arena`: Gestión de ciclo de vida de memoria segura.

### `SectorMemoryVault`
*   **Identidad**: Bóveda Física
*   **Dependencias**:
    *   `Unsafe`: Allocación de memoria cruda (Raw Memory).

---

## 🤖 JARVIS (Sovereign OS Domain)

### `JarvisTcpServer`
*   **Identidad**: uplink Server
*   **Dependencias**:
    *   `VolcanEventDispatcher`: Para inyectar comandos de voz/remotos al Kernel.
    *   `java.net.ServerSocket`: Comunicación TCP estándar.
    *   `VolcanSignalCommands`: Diccionario de comandos reconocidos.

### `JarvisVoiceInterface`
*   **Identidad**: Módulo Auditivo
*   **Dependencias**:
    *   `VolcanAtomicBus`: Para enviar comandos de voz reconocidos al motor.
    *   `SpeechRecognizer (Android)`: *Dependencia Externa (Cliente Android).*

### `WhatsAppBridge`
*   **Identidad**: Puente de Mensajería
*   **Dependencias**:
    *   `JarvisMobileConnector`: Enlace de transporte.
    *   `Android Intent API`: *Dependencia Externa (Cliente Android).*

---

## 🔗 DEPENDENCIAS EXTERNAS DE SISTEMA

*   **Java 25 (LTS)**: Runtime environment base.
*   **GraalVM Native Image**: Compilación AOT (Ahead-Of-Time).
*   **Project Panama**: Acceso a memoria y funciones nativas.
*   **Vector API**: Instrucciones SIMD (AVX-512) para procesamiento masivo.
