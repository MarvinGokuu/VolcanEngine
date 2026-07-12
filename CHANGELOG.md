# Changelog

All notable changes to **VolcanEngine** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-07-12

### Added (Initial Engine Core)
- **`EngineKernel`**: The core hot-path physics and logic orchestrator running at sub-millisecond latencies.
- **`TimeKeeper` (Governor)**: Dynamic FPS scaling (CVT) and uninhibited benchmarking via `UnburnerMode`.
- **Zero-GC & Off-Heap Memory**: Implementation of Project Panama FFI (`java.lang.foreign.MemorySegment`) and `SectorMemoryVault` to eliminate Garbage Collection pauses.
- **Agnostic FFI Architecture**: `VolcanGraphicsLinker` and `VolcanAudioLinker` to interface directly with hardware DLLs (Vulkan/OpenGL, OpenAL) bypassing JNI.
- **Data-Oriented Design (DoD)**: `VolcanTransformSoA`, `VolcanColliderSoA`, and SIMD Vector API usage to maximize L1 Cache coherence and Mechanical Sympathy.

### Added (Tooling & UI)
- **`ServerControlWindow`**: Lightweight Swing-based control plane featuring real-time toggling of Unburner Mode for benchmarking.
- **Telemetry System**: High-precision `MetricsPacker` and `VolcanLogger` writing lock-free states to `volcanengine_metrics.log`.
- **Quick Start Build System**: Windows batch scripts (`build.bat`, `test.bat`, `run.bat`, `clean.bat`) for immediate compilation and auditing.
- **Business Docs**: Configured environment for EWC Pitch Deck and forensic audit reports.
