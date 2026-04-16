package me.almana.modern_f3.debug.overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface OverlayModule {
    String id();
    String displayName();

    boolean isEnabled();
    void setEnabled(boolean enabled);
    default boolean isActive() { return isEnabled(); }

    int getX();
    int getY();
    void setPosition(int x, int y);
    default int getConfigX() { return getX(); }
    default int getConfigY() { return getY(); }

    int getWidth();
    int getHeight();
    float getScale();
    void setScale(float scale);
    float defaultScale();

    int getTextColor();
    void setTextColor(int color);

    boolean showBackground();
    void setShowBackground(boolean show);
    int getBackgroundColor();
    void setBackgroundColor(int color);
    int getBackgroundOpacity();
    void setBackgroundOpacity(int opacity);

    boolean useTextShadow();
    void setTextShadow(boolean shadow);

    void tick();
    void updateSize();
    void render(GuiGraphicsExtractor graphics, float partialTick);

    int defaultX();
    int defaultY();
}
