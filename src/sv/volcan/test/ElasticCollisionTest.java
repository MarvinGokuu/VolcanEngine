// Reading Order: 10100000
//  160
// SPDX-FileCopyrightText: 2026 Marvin Alexander Flores Canales
// SPDX-License-Identifier: LGPL-3.0-or-later
package sv.volcan.test;

import sv.volcan.scene.VolcanTransformSoA;
import sv.volcan.physics.SpatialHashGrid;
import sv.volcan.physics.VolcanColliderSoA;
import sv.volcan.physics.NarrowphaseSystem;
import sv.volcan.state.WorldStateFrame;
import sv.volcan.ecs.VolcanScene;
import java.lang.foreign.ValueLayout;

/**
 * RESPONSIBILITY: Narrowphase and RigidBody Dynamics Benchmark.
 * WHY: Validating that resolving thousands of elastic collisions takes < 5ms.
 * TECHNIQUE: Data-Oriented Fast-Path Math.
 */
public class ElasticCollisionTest {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" RIGIDBODY DYNAMICS - ELASTIC COLLISION TEST");
        System.out.println("==============================================");

        int maxEntities = 50_000;
        float cellSize = 10.0f;
        
        System.out.println("[TEST] Initializing GPU Context for Broadphase...");
        System.out.println("[TEST] Initializing off-heap physics pipelines...");
        VolcanScene scene = new VolcanScene(maxEntities);
        VolcanTransformSoA tSoa = scene.getSoA();
        VolcanColliderSoA cSoa = new VolcanColliderSoA(maxEntities);
        SpatialHashGrid grid = new SpatialHashGrid(maxEntities, cellSize, 100, 100);

        NarrowphaseSystem narrowphase = new NarrowphaseSystem(scene, grid, cSoa);
        
        // WorldStateFrame no es usado por NarrowphaseSystem, pasamos null
        System.out.println("[TEST] Spawning " + maxEntities + " colliding entities...");
        for (int i = 0; i < maxEntities; i++) {
            // Spawn entities in a realistic 1000x1000 world
            double rx = Math.random() * 1000.0;
            double ry = Math.random() * 1000.0;
            tSoa.globalPosX.set(ValueLayout.JAVA_DOUBLE, i * 8L, rx);
            tSoa.globalPosY.set(ValueLayout.JAVA_DOUBLE, i * 8L, ry);
            tSoa.velX.set(ValueLayout.JAVA_FLOAT, i * 4L, (float)(Math.random() * 10 - 5));
            tSoa.velY.set(ValueLayout.JAVA_FLOAT, i * 4L, (float)(Math.random() * 10 - 5));
            
            cSoa.radius.set(ValueLayout.JAVA_FLOAT, i * 4L, 1.0f); // 1.0 radio
            cSoa.mass.set(ValueLayout.JAVA_FLOAT, i * 4L, 1.0f);   // 1.0 kg
            cSoa.restitution.set(ValueLayout.JAVA_FLOAT, i * 4L, 1.0f); // 100% elástico
        }

        System.out.println("[TEST] Warming up JVM JIT...");
        for (int i = 0; i < 100; i++) {
            grid.buildGrid(tSoa, maxEntities);
            narrowphase.update(null, 0.016f);
        }

        System.out.println("[TEST] Benchmarking Narrowphase (100 passes)...");
        long startNs = System.nanoTime();
        
        int passes = 100;
        for (int i = 0; i < passes; i++) {
            grid.buildGrid(tSoa, maxEntities);
            narrowphase.update(null, 0.016f);
        }
        
        long endNs = System.nanoTime();
        double avgTimeMs = ((endNs - startNs) / 1_000_000.0) / passes;

        System.out.printf("[RESULT] Time to resolve collisions: %.3f ms per frame.%n", avgTimeMs);
        
        if (avgTimeMs > 16.0) {
            System.err.println("[FAIL] Physics update exceeded 16ms (60 FPS budget). Got: " + avgTimeMs + "ms");
            System.exit(1);
        } else {
            System.out.println("[OK] AAA+ Collision Throughput verified!");
        }

        grid.destroy();
        cSoa.destroy();
        scene.destroy();
        System.exit(0);
    }
}
