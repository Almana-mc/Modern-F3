package me.almana.modern_f3.debug.overlay.modules;

import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.overlay.DebugScreenMirror;
import me.almana.modern_f3.debug.overlay.OverlayModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.BitSet;
import java.util.List;

public class DebugLineModule implements OverlayModule {
    private static final float DEFAULT_SCALE = 0.75F;
    private static final float MIN_SCALE = 0.5F;
    private static final float MAX_SCALE = 2.0F;
    private static final int PAD = 2;
    private static final int ACCENT = 2;

    private final String id;
    private final String displayName;
    private final DebugScreenMirror mirror;
    private final boolean rightColumn;
    private final int lineIndex;
    private final int defX;
    private final int defY;

    private boolean enabled = true;
    private boolean entryActive = true;
    private int x;
    private int y;
    private int textColor = 0xFFFFFFFF;
    private boolean background = true;
    private int backgroundColor = 0x101010;
    private int backgroundOpacity = 0xB0;
    private boolean textShadow = true;
    private float scale = DEFAULT_SCALE;
    private int cachedW = 50;
    private int cachedH = 15;
    private boolean anchoredRight;
    private int renderedX;
    private String line = "";

    public DebugLineModule(String id, String displayName, DebugScreenMirror mirror, boolean rightColumn, int lineIndex, int defX, int defY) {
        this.id = id;
        this.displayName = displayName;
        this.mirror = mirror;
        this.rightColumn = rightColumn;
        this.lineIndex = lineIndex;
        this.defX = defX;
        this.defY = defY;
        this.x = defX;
        this.y = defY;
        this.anchoredRight = rightColumn && defX == 0;
    }

    @Override public String id() { return id; }
    @Override public String displayName() { return displayName; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
    @Override public boolean isActive() { return enabled && entryActive; }
    @Override public int getX() { return anchoredRight ? renderedX : x; }
    @Override public int getY() { return y; }
    @Override public int getConfigX() { return anchoredRight ? defX : x; }
    @Override public int getConfigY() { return y; }
    @Override public int getWidth() { return Math.max(cachedW, 40); }
    @Override public int getHeight() { return Math.max(cachedH, 11); }
    @Override public float getScale() { return scale; }
    @Override public void setScale(float scale) { this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale)); }
    @Override public float defaultScale() { return DEFAULT_SCALE; }
    @Override public int getTextColor() { return textColor; }
    @Override public void setTextColor(int color) { this.textColor = color; }
    @Override public boolean showBackground() { return background; }
    @Override public void setShowBackground(boolean show) { this.background = show; }
    @Override public int getBackgroundColor() { return backgroundColor; }
    @Override public void setBackgroundColor(int color) { this.backgroundColor = color & 0x00FFFFFF; }
    @Override public int getBackgroundOpacity() { return backgroundOpacity; }
    @Override public void setBackgroundOpacity(int opacity) { this.backgroundOpacity = Math.max(0, Math.min(255, opacity)); }
    @Override public boolean useTextShadow() { return textShadow; }
    @Override public void setTextShadow(boolean shadow) { this.textShadow = shadow; }
    @Override public int defaultX() { return defX; }
    @Override public int defaultY() { return defY; }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        anchoredRight = rightColumn && x == defX && y == defY;
    }

    @Override
    public void tick() {
        DebugScreenMirror.Snapshot snap = mirror.snapshot();
        List<String> lines = rightColumn ? snap.rightLines() : snap.leftLines();
        BitSet inactive = rightColumn ? snap.inactiveRight() : snap.inactiveLeft();
        line = lineIndex < lines.size() ? lines.get(lineIndex) : "";
        entryActive = !line.isEmpty() && !inactive.get(lineIndex);
    }

    @Override
    public void updateSize() {
        if (line.isEmpty()) {
            cachedW = 40;
            cachedH = 11;
            return;
        }

        Font font = Minecraft.getInstance().font;
        int pad = Math.max(1, Math.round(PAD * scale));
        int accent = Math.max(1, Math.round(ACCENT * scale));
        int textWidth = Math.max(1, Math.round(font.width(line) * scale));
        int textHeight = Math.max(1, Math.round(font.lineHeight * scale));
        int w = accent + pad + textWidth + pad;
        int h = textHeight + pad * 2;
        cachedW = w;
        cachedH = h;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, float partialTick) {
        updateSize();
        if (line.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int pad = Math.max(1, Math.round(PAD * scale));
        int accent = Math.max(1, Math.round(ACCENT * scale));
        int drawX = x;
        if (anchoredRight) {
            drawX = Math.max(4, graphics.guiWidth() - cachedW - 4);
        }
        renderedX = drawX;

        if (background) {
            graphics.fill(drawX, y, drawX + cachedW, y + cachedH, backgroundArgb());
            graphics.fill(drawX, y, drawX + accent, y + cachedH, textColor | 0xFF000000);
        }

        Compat.pushPose(graphics);
        Compat.translate(graphics, drawX + accent + pad, y + pad);
        Compat.scale(graphics, scale);
        graphics.text(font, line, 0, 0, textColor, textShadow);
        Compat.popPose(graphics);
    }

    private int backgroundArgb() {
        return (backgroundOpacity << 24) | backgroundColor;
    }
}
