package me.almana.modern_f3.debug.overlay;

public interface ModuleEnabledState {
    boolean isEnabled();

    void setEnabled(boolean enabled);

    static void setAll(Iterable<? extends ModuleEnabledState> modules, boolean enabled) {
        for (ModuleEnabledState module : modules) {
            module.setEnabled(enabled);
        }
    }
}
