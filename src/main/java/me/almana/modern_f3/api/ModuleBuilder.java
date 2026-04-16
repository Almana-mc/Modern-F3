package me.almana.modern_f3.api;

import me.almana.modern_f3.debug.overlay.OverlayModule;

import java.util.function.Supplier;

public final class ModuleBuilder {
    private final Supplier<? extends OverlayModule> factory;

    private Integer x, y;
    private Boolean enabled;
    private Integer textColor;
    private Boolean background;
    private Integer backgroundColor;
    private Integer backgroundOpacity;
    private Boolean textShadow;
    private Float scale;

    ModuleBuilder(Supplier<? extends OverlayModule> factory) {
        this.factory = factory;
    }

    public ModuleBuilder position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ModuleBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ModuleBuilder textColor(int color) {
        this.textColor = color;
        return this;
    }

    public ModuleBuilder background(boolean show) {
        this.background = show;
        return this;
    }

    public ModuleBuilder backgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public ModuleBuilder backgroundOpacity(int opacity) {
        this.backgroundOpacity = opacity;
        return this;
    }

    public ModuleBuilder textShadow(boolean shadow) {
        this.textShadow = shadow;
        return this;
    }

    public ModuleBuilder scale(float scale) {
        this.scale = scale;
        return this;
    }

    public void register() {
        ModernF3Api.registerModule(this::build);
    }

    private OverlayModule build() {
        OverlayModule module = factory.get();
        if (x != null && y != null) module.setPosition(x, y);
        if (enabled != null) module.setEnabled(enabled);
        if (textColor != null) module.setTextColor(textColor);
        if (background != null) module.setShowBackground(background);
        if (backgroundColor != null) module.setBackgroundColor(backgroundColor);
        if (backgroundOpacity != null) module.setBackgroundOpacity(backgroundOpacity);
        if (textShadow != null) module.setTextShadow(textShadow);
        if (scale != null) module.setScale(scale);
        return module;
    }
}
