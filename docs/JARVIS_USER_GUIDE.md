# JARVIS (MarvinDev OS) - Manual de Operaciones

## 1. Instalación del Cliente Móvil (Android)
**Requisitos**: Dispositivo Android (min SDK 26), Android Studio / Gradle.

1.  **Localizar el Proyecto**:
    El código fuente del cliente se encuentra en:
    `clients/android/JarvisClient`
2.  **Compilar APK**:
    Abra la carpeta en Android Studio o ejecute via terminal:
    ```bash
    cd clients/android/JarvisClient
    ./gradlew assembleDebug
    ```
3.  **Instalar**:
    Transfiera el archivo `.apk` generado (`app/build/outputs/apk/debug/app-debug.apk`) a su dispositivo y ejecútelo.

---

## 2. Ignición del Motor (PC Host)
Antes de conectar el cliente, el núcleo Volcan debe estar activo.

1.  **Ejecutar Script de Ignición**:
    Doble clic en `ignite.bat` en la raíz del proyecto.
2.  **Verificación**:
    Espere a ver el mensaje: `[KERNEL] Sovereign Loop started`.

---

## 3. Activación de Protocolo de Voz (Hardware Intercept)
El sistema cuenta con un disparador físico de seguridad para habilitar la escucha.

1.  **Foco en Consola**: Asegúrese de que la ventana de la consola del motor tenga el foco.
2.  **Tecla de Activación**: Presione la tecla **`J`** (Mayúscula o minúscula).
3.  **Confirmación Visual**:
    La consola mostrará:
    ```
    [HARDWARE INTERCEPT] KEY 'J' DETECTED -> JARVIS PROTOCOL ENGAGED.
    [JARVIS] Voice Mode: ACTIVATED.
    ```

---

## 4. Conexión del Cliente Móvil (Uplink)
Una vez activado el protocolo en el PC:

1.  **Abrir App**: Inicie "Jarvis Client" en su Android.
2.  **Configurar IP** (Si es la primera vez): Ingrese la IP local de su PC.
3.  **Conectar**: Toque el botón **"CONNECT UPLINK"**.
4.  **Confirmación**:
    - App: Estado cambia a "SECURE LINK ESTABLISHED".
    - PC: `[JARVIS MOBILE LINK] CONNECTED to Samsung S22+`.

---

## 5. Comandos de Voz y Control
Con el micrófono activado (PC o Móvil), use los **Comandos Mágicos**:

| Comando de Voz | Acción |
| :--- | :--- |
| **"MarvinDevOn"** | Ejecuta secuencia de Ignición total. |
| **"MarvinDevoff"** | Inicia apagado seguro del sistema. |
| **"MarvinDevsv"** | (Solo Admin) Resurrección de emergencia del Kernel. |

---

## 6. Referencia de Rutas (Deploy Manual)

### A. En tu PC (Origen)
Una vez compilado, el archivo **APK** aparecerá aquí:
`clients/android/JarvisClient/app/build/outputs/apk/debug/app-debug.apk`

### B. En tu Teléfono (Destino Sugerido)
Si lo envías por WhatsApp/USB, búscalo en:
`/storage/emulated/0/Download/` (Carpeta de Descargas)
Ó
`/storage/emulated/0/WhatsApp/Media/WhatsApp Documents/`
