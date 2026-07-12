// Reading Order: 10000001
//  129
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.ecs;

import sv.volcan.core.AAACertified;
import sv.volcan.core.systems.GameSystem;
import sv.volcan.scene.VolcanKinematicsSystem;
import sv.volcan.state.WorldStateFrame;

/**
 * ECS Kinematics Adapter (Phase 30).
 * 
 * Conecta el `VolcanScene` (Scene Graph de Alto Nivel) con el
 * `VolcanKinematicsSystem` (SIMD Vectorial de Bajo Nivel)
 * inyectándolo directamente en el loop de `SystemRegistry` / `ParallelSystemExecutor`.
 */
@AAACertified(date = "2026-06-19", maxLatencyNs = 1000, minThroughput = 0, lockFree = true, offHeap = true, notes = "Adapter for High-Level ECS Kinematics")
public final class SceneKinematicsSystem implements GameSystem {

    private final VolcanScene scene;

    public SceneKinematicsSystem(VolcanScene scene) {
        this.scene = scene;
    }

    @Override
    public void update(WorldStateFrame state, float deltaTime) {
        // Ejecuta físicas de 1,000,000 de entidades en AVX-512 usando la memoria contigua SoA
        // Por ahora, asumimos que la cámara está en (0, 0)
        VolcanKinematicsSystem.update(scene.getSoA(), (float) deltaTime, 0.0, 0.0, 0.0);
    }

    @Override
    public String[] getDependencies() {
        return new String[]{}; // No dependencies in Headless mode
    }
}
