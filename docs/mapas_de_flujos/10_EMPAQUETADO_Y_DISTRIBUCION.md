# 🗺️ Mapa de Ciclo de Vida y Empaquetado (Capa 4: Distribución de Servidor)

Este mapa revela el proceso industrial (Build Pipeline) que ocurre al ejecutar la construcción final y cómo interactúa con nuestro sistema de Integración Continua. El código fuente debe ser destilado, ofuscado y fusionado en un único binario ejecutable (`.exe` o Linux Server ELF) sin dependencias gráficas, asegurando que no se empaquete código de prueba ni el historial del repositorio.



```mermaid
graph TD
    subgraph codigo_fuente [Código Fuente Headless]
        A0(Filtro de Exclusión de Tests/JMH) --> A
        A(Java Code)
        B(Configuraciones Server JSON)
    end

    subgraph compilacion_cruda [1. Compilación Extrema (javac)]
        A -->|javac -g:none| D{Despojado de Símbolos Debug}
        B --> E{Copiado a Carpeta de Salida}
    end

    subgraph empaquetado_jar [2. Ensamblaje Interno (Fat-JAR)]
        D --> F[VolcanEngine-Server-v1.0.jar]
        E -->|Inyección de Config| F
    end

    subgraph jpackage [3. Empaquetado Nativo Aislado]
        F --> I[Mover a carpeta 'release_input']
        I --> G{JPackage / JVM Shrinking}
        G -->|Bundling puro sin historial Git| H((Volcan-Server-Runtime Final))
    end

    %% Estilos AAA
    classDef source fill:#2c3e50,stroke:#34495e,stroke-width:2px,color:#fff;
    classDef javac fill:#c0392b,stroke:#e74c3c,stroke-width:2px,color:#fff;
    classDef jar fill:#f39c12,stroke:#d35400,stroke-width:2px,color:#fff;
    classDef native fill:#27ae60,stroke:#2ecc71,stroke-width:3px,color:#fff;

    class A,B source;
    class D,E javac;
    class F,I jar;
    class G,H native;
```



## Leyenda Técnica de CI/CD:
*   **Filtro de Exclusión de Tests:** Al descubrir los archivos con `dir /s /B`, aplicamos filtros `findstr /v "\test\"` y `\benchmark\` para garantizar que el servidor final no incluya código de pruebas, previniendo errores de dependencias en producción.
*   **-g:none (Zero Debug):** Parámetro del compilador que elimina intencionalmente el número de líneas y nombres locales para máxima velocidad y ofuscación de la lógica del servidor.
*   **Aislamiento de Input (JPackage):** En lugar de empaquetar el directorio raíz completo (`--input .`), se mueve el JAR a una carpeta aislada (`release_input`). Esto evita que la herramienta intente empaquetar la carpeta oculta `.git` (que pesa gigabytes), optimizando tiempos de DevOps.
*   **Zero Dependencies (No-GUI):** El binario final ya no inyecta bibliotecas C++ ni shaders. Es un proceso puro Headless de alta latencia diseñado para correr directamente en la terminal de contenedores de Linux o Windows Server.
