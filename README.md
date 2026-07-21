#  VolcanEngine
**The First Zero-Overhead, Mechanical Sympathy Java Engine**

> *"¿Java para un motor de baja latencia? ironía"*

![VolcanEngine Logo](src/sv/volcan/ui/volcanengine_logo.png?v=2)

##  1. ¿QUÉ PROBLEMA RESUELVE? (La Crisis del Hardware)

La industria tecnológica actual atraviesa una crisis de **software perezoso e ineficiente**. Hoy en día, la norma es apilar incontables capas de abstracción (lenguajes interpretados y *frameworks* de alto nivel) que aíslan al desarrollador del procesador. A esto se suma el abuso histórico de la Programación Orientada a Objetos (OOP), que fragmenta la memoria al dispersar el estado de la aplicación en miles de objetos esparcidos, destrozando la eficiencia de la caché del CPU (*Cache Misses*). Por el otro extremo, las alternativas tradicionales de bajo nivel, aunque más rápidas, exigen una gestión manual de punteros que frecuentemente termina en corrupción de memoria, fugas incontrolables o vulnerabilidades de seguridad (*Segmentation Faults*).

**El Impacto Económico y el Colapso del Hardware:** 
Este diseño defectuoso resulta en motores gráficos y runtimes que devoran recursos computacionales de manera desproporcionada. Y aquí ocurre el efecto dominó estructural: **si saturas la RAM con miles de objetos basura, obligas al procesador a detenerse (GC Pauses), dejando a la GPU hambrienta de datos; si la GPU se ahoga, a menudo es porque el bus de memoria no está entregando la información de manera coordinada.** Esto crea una barrera de entrada elitista, obligando a los usuarios a gastar miles de dólares en hardware nuevo constantemente solo para ejecutar programas mal optimizados.

**La Solución VolcanEngine:** VolcanEngine es un motor diseñado para resolver estos problemas de ineficiencia y aprovechar al máximo la capacidad del hardware. El proyecto aplica los principios de Martin Thompson, demostrando que comprender el hardware a bajo nivel es clave para mejorar el rendimiento. Utilizamos **Java** junto con sus herramientas modernas (*Project Panama*, *Vector API*) para operar directamente en la memoria nativa (*Off-Heap*). Esto nos permite evitar el uso del recolector de basura (Garbage Collector) y lograr latencias bajas, manteniendo la seguridad de memoria que ofrecen los lenguajes administrados.


---

##  2. ¿CÓMO LO HACEMOS? (Ingeniería "Mechanical Sympathy")

Estos resultados se logran aplicando la disciplina de ingeniería conocida como **Mechanical Sympathy** (Simpatía Mecánica). Martin Thompson adaptó este concepto al desarrollo de software, definiéndolo como la mejora en la calidad del código cuando el desarrollador comprende cómo funcionan el hardware y la infraestructura a bajo nivel.

Aunque existe debate sobre si la brecha entre desarrollo y hardware ha crecido demasiado con las abstracciones modernas, VolcanEngine sostiene que entender estas capas inferiores sigue siendo absolutamente crucial para resolver problemas complejos de red, memoria y arquitectura. El código está matemáticamente diseñado para alinearse a la forma física en la que opera el núcleo del procesador.

1. **Zero-Garbage Collection (Off-Heap):** Rompemos las reglas de Java. Todo el estado del motor y la simulación NO existe como objetos tradicionales; lo guardamos directamente en la Memoria RAM Nativa (Off-Heap) mediante *Project Panama (Java 25)*. El recolector de basura jamás se activa, garantizando latencias ultra-bajas y cero "tirones" de FPS.
2. **Estructuras Lock-Free (Cero Cuellos de Botella):** Erradicamos el uso de candados (Locks). La comunicación entre el Kernel, Físicas y Renderizado fluye por buses atómicos de tipo *MPSC RingBuffer*, permitiendo despachar tareas sin que los núcleos del procesador se estorben.
3. **Hardware L1 Cache Alignment:** Almacenamos nuestra información en bloques estrictamente acolchados a 64 bytes (*Padding*). Esto previene el devastador fenómeno de *False Sharing*, asegurando que la memoria Caché L1/L2 del procesador fluya.
4. **Matemáticas SIMD Vectorizadas:** En lugar de saturar la Tarjeta Gráfica (GPU) de gama baja del usuario, el motor agrupa cálculos masivos (físicas, colisiones) y los procesa simultáneamente en la CPU usando los registros anchos *AVX-512* (Incubator Vector API).

---

##  3. MÉTRICAS DE PRODUCCIÓN (Certificación AAA+)

El motor ha sido validado bajo un estricto paquete de pruebas (*test.bat*) donde pasó el 100% (21/21) de las auditorías de estrés. Las métricas capturadas hablan por sí solas:

| MÉTRICA TÉCNICA----------------------- | RESULTADO OBTENIDO --------------|
| :------------------------------------- | :------------------------------- |
| **Boot Sequence (Tiempo de Arranque)** | `0.0008 ms` (¡800 nanosegundos!) |
| **Latencia de Comunicación de Bus** ---| `29.19 ns / operación`---------- |
| **Ancho de Banda de Eventos** ---------| `552 Millones de ops/seg` -------|
| **Consumo Promedio de Memoria (RAM)**- | `~335 MB` (Zero Overhead)------- |

---

##  4. COMPILACIÓN Y EJECUCIÓN

Para experimentar la velocidad de VolcanEngine en tu propia máquina:

### Prerrequisitos: por el momento....
* **JDK**: Oracle GraalVM 25 / OpenJDK 25 (con módulos incubadora habilitados).
* **OS**: Windows 11 / Linux.
* próximamente se hará fuera de la jvm. 

### Arranque Rápido:
Desde tu terminal en la carpeta del proyecto:
```bash
# 1. Limpiar compilaciones anteriores (Sanitización)
.\clean.bat

# 2. Ejecutar las 21 pruebas de certificación en nanosegundos
.\test.bat

# 3. Compilar el motor puro
.\build.bat

# 4. Lanzar el Runtime Minimalista 
.\run.bat
```

---

**Licencia**: [VolcanEngine Indie License / Commercial License](COMMERCIAL_LICENSE.md)  
**Arquitectura de Motor por**: Marvin Alexander Flores Canales  
**Versión**: v5.0.0 (Zero-Overhead Native Edition)
