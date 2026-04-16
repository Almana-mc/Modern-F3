package me.almana.modern_f3.api;

import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.overlay.OverlayModule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModernF3Api {
    private static final List<Supplier<OverlayModule>> pendingModules = new ArrayList<>();
    private static boolean overlayReady;

    private ModernF3Api() {}

    public static ModuleBuilder module(Supplier<? extends OverlayModule> factory) {
        return new ModuleBuilder(factory);
    }

    public static void registerModule(Supplier<OverlayModule> factory) {
        if (overlayReady) {
            DebugOverlay.get().addModule(factory.get());
            return;
        }
        pendingModules.add(factory);
    }

    public static OverlayModule getModule(String id) {
        return DebugOverlay.get().findModule(id);
    }

    public static void flushPending(DebugOverlay overlay) {
        overlayReady = true;
        for (Supplier<OverlayModule> factory : pendingModules) {
            overlay.addModule(factory.get());
        }
        pendingModules.clear();
    }
}
