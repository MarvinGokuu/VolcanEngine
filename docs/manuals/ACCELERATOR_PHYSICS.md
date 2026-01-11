# ⚛️ Física del Acelerador: De Partículas a Datos

Estableciendo el puente científico entre el Gran Colisionador de Hadrones (LHC) y el Volcan Data Accelerator.

---

## 1. La Física Real (Mundo Subatómico)

La fórmula fundamental que gobierna un acelerador de partículas es la **Fuerza de Lorentz**. Esta fuerza es la que empuja (acelera) y curva (dirige) las partículas.

### **$$ \vec{F} = q(\vec{E} + \vec{v} \times \vec{B}) $$**

Donde:
*   **$\vec{F}$**: Fuerza total aplicada a la partícula.
*   **$q$**: Carga eléctrica de la partícula (nuestro "Dato").
*   **$\vec{E}$**: **Campo Eléctrico**. Es el que da la **Energía/Velocidad** (acelera).
*   **$\vec{B}$**: **Campo Magnético**. Es el que **Curva la trayectoria** (mantiene la partícula en el anillo).
*   **$\vec{v}$**: Velocidad de la partícula.

**En resumen**: Usas electricidad para empujar y magnetismo para guiar.

---

## 2. La Física de Volcan (Mundo de Datos)

En `VolcanDataAccelerator`, aplicamos una versión computacional de esta ley.

### **$$ Throughput = \frac{Bandwidth \times Lanes}{Latencia} $$**

Analogía Directa:
1.  **Partícula ($q$)**: Un `byte` o `int` (el dato crudo).
2.  **Campo Eléctrico ($\vec{E}$)**: **CPU Clock**. Es lo que "empuja" los datos a través del silicio.
3.  **Campo Magnético ($\vec{B}$)**: **Vector Lanes (AVX)**. Son los "imanes" que alinean 8 o 16 datos para que viajen juntos en paralelo sin chocar.
4.  **Colisión**: La operación matemática (Suma, Multiplicación) que ocurre en el ALU.

---

## 3. Comparativa de Energía

| Concepto | LHC (CERN) | Volcan Accelerator |
| :--- | :--- | :--- |
| **Combustible** | Protones | Integers (32-bit) |
| **Acelerador** | Imanes Superconductores | Registros AVX-512/256 |
| **Velocidad** | 99.999% de $c$ (luz) | 50 GB/s (Velocidad RAM) |
| **Resultado** | Bosón de Higgs | Checksum Instantáneo AAA+ |

> **"Un acelerador de datos no es más que un tubo de vacío donde los bits viajan en formación perfecta, empujados por el reloj del CPU."** — *Marvin-Dev*
