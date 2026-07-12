package sv.volcan.core;

import java.lang.foreign.MemorySegment;

/**
 * Agnostic interface for OS Window and Input management.
 */
public abstract class VolcanPlatformContext {
    
    private static VolcanPlatformContext instance;

    public static void set(VolcanPlatformContext backend) {
        instance = backend;
    }

    public static VolcanPlatformContext get() {
        if (instance == null) {
            throw new IllegalStateException("Platform Context not initialized.");
        }
        return instance;
    }

    public abstract void initWindow(String title, int width, int height);
    public abstract void pollEvents(MemorySegment vaultSegment);
    public abstract boolean shouldClose();
    public abstract MemorySegment getWindowPointer();
    public abstract void makeContextCurrent();
    public abstract void setSwapInterval(int interval);
    public abstract void cleanup();
}
