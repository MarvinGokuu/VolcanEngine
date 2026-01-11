# Índice Binario Maestro - Volcan Engine (AAA+)

## Autoridad
**Documento**: Índice de Señales Binarias
**Nivel**: AAA+ Kernel Engineering
**Versión**: 2.0 (Sync with VolcanSignalCommands)
**Fecha**: 2026-01-10
**Propósito**: Referencia absoluta de todos los códigos de operación, layouts de memoria y formatos de señal de 64 bits.

---

## 1. Estructura de Señal (64 Bits)

Todas las señales en el Volcan Atomic Bus cumplen con la arquitectura de 64 bits para alineación simple con registros de CPU.

### Formato Estándar (Comando + Valor)
```
[ 63 .................... 32 ] [ 31 ..................... 0 ]
|       COMMAND ID (32b)     |        PAYLOAD (32b)         |
```
- **Command ID**: Identificador único de operación `(Type Base | Specific ID)`.
- **Payload**: Valor entero o bits empaquetados.

### Formato Vectorial (2D Float)
```
[ 63 .................... 32 ] [ 31 ..................... 0 ]
|       FLOAT X (32b)        |        FLOAT Y (32b)         |
```
- **Uso**: Física, Posición, Velocidad.
- **Precisión**: IEEE 754 (Sin pérdida).

### Formato Espacial (3D Compacto)
```
[ 63 ........ 48 ] [ 47 ........ 32 ] [ 31 ..................... 0 ]
|   X (16b)      |    Y (16b)       |          Z (32b)             |
```
- **Uso**: Coordenadas orbitales masivas.
- **Rangos**: X/Y (Short), Z (Int).

---

## 2. Catálogo de Comandos (VolcanSignalCommands)

### INPUT (0x1000 - 0x1FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x1001` | `INPUT_KEY_DOWN` | `KeyCode` | Tecla presionada. |
| `0x1002` | `INPUT_KEY_UP` | `KeyCode` | Tecla liberada. |
| `0x1003` | `INPUT_MOUSE_MOVE` | `Packed XY` | Movimiento del mouse. |
| `0x1004` | `INPUT_MOUSE_CLICK` | `ButtonID` | Click del mouse. |
| `0x1005` | `INPUT_GAMEPAD_BUTTON` | `ButtonID` | Botón de gamepad. |

### NETWORK (0x2000 - 0x2FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x2001` | `NET_SYNC_ENTITY` | `EntityID` | Sincronización de estado. |
| `0x2002` | `NET_PACKET_RECEIVED` | `PacketID` | Paquete binario recibido. |
| `0x2003` | `NET_CONNECTION_ESTABLISHED`| `ClientID` | Nueva conexión. |
| `0x2004` | `NET_CONNECTION_LOST` | `ClientID` | Conexión cerrada. |

### SYSTEM (0x3000 - 0x3FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x3001` | `SYS_ENTITY_SPAWN` | `TypeID` | Crear entidad. |
| `0x3002` | `SYS_ENTITY_DESTROY` | `EntityID` | Destruir entidad. |
| `0x3003` | `SYS_ENTITY_MOVE` | `EntityID` | Actualizar posición. |
| `0x3100` | `SYS_ENGINE_PAUSE` | `0` | Pausar loop. |
| `0x3101` | `SYS_ENGINE_RESUME` | `0` | Reanudar loop. |
| `0x3102` | `SYS_ENGINE_SHUTDOWN` | `ExitCode` | Apagado controlado. |

### AUDIO (0x4000 - 0x4FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x4001` | `AUDIO_PLAY_SOUND` | `SoundID` | Reproducir efecto. |
| `0x4002` | `AUDIO_STOP_SOUND` | `SoundID` | Detener efecto. |
| `0x4003` | `AUDIO_SET_VOLUME` | `0-100` | Volumen global. |

### PHYSICS (0x5000 - 0x5FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x5001` | `PHYSICS_APPLY_FORCE` | `EntityID` | Aplicar vector fuerza. |
| `0x5002` | `PHYSICS_COLLISION` | `EntityID` | Evento de colisión. |
| `0x5003` | `PHYSICS_SET_GRAVITY` | `Value` | Gravedad global. |

### RENDER (0x6000 - 0x6FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x6001` | `RENDER_SET_SHADER` | `ShaderID` | Cambiar shader activo. |
| `0x6002` | `RENDER_UPDATE_TEXTURE` | `TextureID` | Actualizar VRAM. |
| `0x6003` | `RENDER_SET_CAMERA` | `CameraID` | Cambiar viewport. |

### SPATIAL (0x7000 - 0x7FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x7001` | `SPATIAL_ORBITAL_UPDATE` | `OrbitID` | Recálculo orbital. |
| `0x7002` | `SPATIAL_TELEMETRY_RECEIVED`| `TelemID` | Datos satelitales. |
| `0x7003` | `SPATIAL_SATELLITE_SYNC` | `SatID` | Handshake satelital. |
| `0x7010` | `SPATIAL_COMPUTE_DIFFERENTIAL`| `PairID` | Delta orbital. |
| `0x7011` | `SPATIAL_SCALE_FLOW` | `Percent` | Escalado de flujo. |
| `0x7012` | `SPATIAL_ALIGN_PAGE` | `Type` | Alineación de memoria. |
| `0x7020` | `SPATIAL_EDGE_INJECT` | `BufID` | Inyección Edge. |
| `0x7021` | `SPATIAL_ZERO_COPY_MODE` | `0/1` | Toggle Zero-Copy. |

### MEMORY (0x8000 - 0x8FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x8001` | `MEMORY_ALLOC_OFFHEAP` | `Size` | Asignar memoria nativa. |
| `0x8002` | `MEMORY_FREE_OFFHEAP` | `PtrID` | Liberar memoria nativa. |
| `0x8003` | `MEMORY_MAP_SEGMENT` | `SegID` | Mapear archivo/memoria. |
| `0x8010` | `MEMORY_ALIGN_PAGE_4KB` | `PtrID` | Alinear a 4KB. |
| `0x8011` | `MEMORY_ALIGN_PAGE_2MB` | `PtrID` | Alinear a 2MB (Huge Pages). |
| `0x8020` | `MEMORY_PREFETCH_ENABLE` | `BufID` | Activar prefetch HW. |
| `0x8021` | `MEMORY_PREFETCH_DISABLE` | `BufID` | Desactivar prefetch. |

### ACCELERATOR (0x9000 - 0x9FFF)
| ID Hex | Nombre | Payload | Descripción |
| :--- | :--- | :--- | :--- |
| `0x9001` | `ACCEL_IGNITE_SIMD` | `LaneWidth` | Inicia processing vectorial. |
| `0x9002` | `ACCEL_VECTOR_REDUCE` | `MemPtr` | Reducción (Suma). |
| `0x9003` | `ACCEL_VECTOR_TRANSFORM`| `MemPtr` | Transformación. |
| `0x9004` | `ACCEL_DUMP_STATS` | `0` | Reporte throughput. |

---

## 3. Mapa de Memoria (VolcanStateLayout)

El `VolcanStateVault` utiliza direccionamiento directo por offsets (slots de 4 bytes).

### Bloque 0-99: Estado de Actores (Hot Data)
- **0**: `PLAYER_X`
- **4**: `PLAYER_Y`
- **8**: `PLAYER_DIR`
- **12**: `PLAYER_SCORE`

### Bloque 100-199: Control de Kernel (Critical)
- **400**: `SYS_TICK` (Reloj maestro)
- **404**: `SYS_ENGINE_FLAGS` (0=Run, 1=Alert, 2=Healing)
- **408**: `SYS_TARGET_FPS`
- **412**: `SYS_DELTA_TIME` (Escalado)
- **416**: `ENTITY_COUNT`

### Bloque 200-299: Telemetría Hardware
- **800**: `METRIC_CPU_LOAD` (0-10000)
- **804**: `METRIC_RAM_FREE` (MB)
- **808**: `METRIC_RAM_TOTAL` (MB)

### Bloque 300-399: Input Pipeline
- **1200**: `INPUT_MOUSE_X`
- **1204**: `INPUT_MOUSE_Y`
- **1208**: `INPUT_LAST_SIGNAL`

---

## 4. Estados del Kernel (KernelControlRegister)

Máquina de estados atómica gestionada por `KernelControlRegister`.

- **0**: `STATE_OFFLINE` (Apagado)
- **1**: `STATE_BOOTING` (Carga inicial)
- **2**: `STATE_IGNITION` (Warm-up JIT)
- **3**: `STATE_RUNNING` (Operativo)
- **4**: `STATE_PAUSED` (Suspendido)
- **5**: `STATE_HALTING` (Deteniendo)
- **6**: `STATE_TERMINATED` (Finalizado)
- **99**: `STATE_PANIC` (Error fatal)
