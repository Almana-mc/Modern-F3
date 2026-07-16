package me.almana.modern_f3.debug.ui;

import com.mojang.blaze3d.platform.InputConstants;
import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.config.OverlayConfig;
import me.almana.modern_f3.debug.config.OverlayProfiles;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.overlay.OverlayModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class OverlayEditScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.modern_f3.edit_overlay.title");
    private static final Component DONE = Component.translatable("button.modern_f3.done");
    private static final Component RESET = Component.translatable("button.modern_f3.reset");
    private static final Component NEW_PROFILE = Component.translatable("button.modern_f3.new_profile");
    private static final Component HELP = Component.translatable("screen.modern_f3.edit_overlay.help");
    private static final int BUTTON_HEIGHT = 12;
    private static final int SMALL_BUTTON_WIDTH = 48;
    private static final int TOP_BUTTON_WIDTH = 82;
    private static final int PROFILE_BUTTON_WIDTH = 96;
    private static final int BAR_PADDING = 4;
    private static final int BAR_HEIGHT = BUTTON_HEIGHT + BAR_PADDING * 2;
    private static final float BUTTON_TEXT_SCALE = 0.7F;
    private static final float HELP_TEXT_SCALE = 0.6F;
    private static boolean hideDisabledModules;

    private final DebugOverlay overlay = DebugOverlay.get();
    private OverlayModule dragged;
    private int dragOffX, dragOffY;
    private Button profileButton;
    private Button hideDisabledButton;
    private List<OverlayProfiles.ProfileOption> profiles = List.of();
    private int currentProfileIndex;
    private boolean profileMenuOpen;
    private int dropdownX, dropdownY, dropdownW, dropdownH;

    public OverlayEditScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        overlay.setEditMode(true);
        refreshProfiles();
        int barY = height - BAR_HEIGHT;
        int btnY = barY + BAR_PADDING;

        int gap = 6;
        int totalW = TOP_BUTTON_WIDTH + PROFILE_BUTTON_WIDTH + SMALL_BUTTON_WIDTH + SMALL_BUTTON_WIDTH + SMALL_BUTTON_WIDTH + gap * 4;
        int startX = (width - totalW) / 2;

        hideDisabledButton = addRenderableWidget(compactButton(hideDisabledMessage(), startX, btnY, TOP_BUTTON_WIDTH, btn -> toggleHideDisabled()));
        startX += TOP_BUTTON_WIDTH + gap;
        profileButton = addRenderableWidget(compactButton(currentProfileMessage(), startX, btnY, PROFILE_BUTTON_WIDTH, btn -> toggleProfileMenu()));
        startX += PROFILE_BUTTON_WIDTH + gap;
        addRenderableWidget(compactButton(NEW_PROFILE, startX, btnY, SMALL_BUTTON_WIDTH, btn -> {
            profileMenuOpen = false;
            Compat.setScreen(minecraft, new ProfileCreateScreen(this));
        }));
        startX += SMALL_BUTTON_WIDTH + gap;
        addRenderableWidget(compactButton(RESET, startX, btnY, SMALL_BUTTON_WIDTH, btn -> resetLayout()));
        startX += SMALL_BUTTON_WIDTH + gap;
        addRenderableWidget(compactButton(DONE, startX, btnY, SMALL_BUTTON_WIDTH, btn -> onClose()));

        if (profileMenuOpen && !profiles.isEmpty()) {
            int optionX = width / 2 - PROFILE_BUTTON_WIDTH / 2;
            int firstOptionY = Math.max(24, barY - profiles.size() * (BUTTON_HEIGHT + 2) - 4);
            dropdownX = optionX - 4;
            dropdownY = firstOptionY - 4;
            dropdownW = PROFILE_BUTTON_WIDTH + 8;
            dropdownH = profiles.size() * (BUTTON_HEIGHT + 2) + 6;

            String activeId = OverlayProfiles.activeProfileId();
            int optionY = firstOptionY;
            for (OverlayProfiles.ProfileOption profile : profiles) {
                boolean active = profile.id().equals(activeId);
                Component label = active
                    ? Component.literal("\u25B6 ").append(profile.label())
                    : profile.label();
                addRenderableWidget(compactButton(label, optionX, optionY, PROFILE_BUTTON_WIDTH, btn -> selectProfile(profile.id())));
                optionY += BUTTON_HEIGHT + 2;
            }
        } else {
            dropdownH = 0;
        }
    }

    private void resetLayout() {
        OverlayProfiles.setActiveProfile(overlay, OverlayProfiles.activeProfileId());
        refreshProfiles();
        updateButtons();
        dragged = null;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.fill(0, 0, width, height, 0x60000000);
        int barY = height - BAR_HEIGHT;
        graphics.fill(0, barY, width, height, 0xB0101010);
        graphics.fill(0, barY, width, barY + 1, 0x40FFFFFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Font font = this.font;
        List<OverlayModule> modules = overlay.getModules();

        if (isShiftDown()) {
            drawSnapGrid(graphics);
        }

        for (OverlayModule m : modules) {
            m.tick();

            if (hideDisabledModules && !m.isActive()) {
                continue;
            }

            m.render(graphics, a);

            if (!m.isActive()) {
                graphics.fill(m.getX(), m.getY(),
                    m.getX() + m.getWidth(), m.getY() + m.getHeight(), 0xA0000000);
            }

            boolean hovered = isInside(m, mouseX, mouseY);
            if (hovered || m == dragged) {
                drawBorder(graphics, m.getX(), m.getY(), m.getWidth(), m.getHeight(), 0xFFFFFF00);
            }
        }

        if (profileMenuOpen && dropdownH > 0) {
            graphics.fill(dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + dropdownH, 0xD0101010);
            drawBorder(graphics, dropdownX, dropdownY, dropdownW, dropdownH, 0x60FFFFFF);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(4, height - BAR_HEIGHT - 12);
        graphics.pose().scale(HELP_TEXT_SCALE, HELP_TEXT_SCALE);
        graphics.text(font, HELP, 0, 0, 0xFFCCCCCC, true);
        graphics.pose().popMatrix();

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void drawSnapGrid(GuiGraphicsExtractor graphics) {
        int color = 0x50FFFFFF;

        for (int x = DebugOverlay.DEFAULT_X; x < width; x += DebugOverlay.SNAP_X_STEP) {
            graphics.fill(x, 0, x + 1, height, color);
        }

        for (int y = DebugOverlay.DEFAULT_Y; y < height; y += DebugOverlay.DEFAULT_ROW_STEP) {
            graphics.fill(0, y, width, y + 1, color);
        }
    }

    private void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        List<OverlayModule> modules = visibleModules();
        for (int i = modules.size() - 1; i >= 0; i--) {
            OverlayModule m = modules.get(i);
            if (isInside(m, event.x(), event.y())) {
                if (event.button() == 0) {
                    dragged = m;
                    dragOffX = (int) event.x() - m.getX();
                    dragOffY = (int) event.y() - m.getY();
                    return true;
                }
                if (event.button() == 1) {
                    Compat.setScreen(minecraft, new ModuleEditScreen(this, m));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragged != null && event.button() == 0) {
            int nx = (int) event.x() - dragOffX;
            int ny = (int) event.y() - dragOffY;

            if (event.hasShiftDown()) {
                nx = snap(nx, DebugOverlay.DEFAULT_X, DebugOverlay.SNAP_X_STEP);
                ny = snap(ny, DebugOverlay.DEFAULT_Y, DebugOverlay.DEFAULT_ROW_STEP);
            }

            nx = Math.max(0, Math.min(nx, width - dragged.getWidth()));
            ny = Math.max(0, Math.min(ny, height - dragged.getHeight()));
            dragged.setPosition(nx, ny);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragged != null && event.button() == 0) {
            dragged = null;
            OverlayConfig.save(overlay);
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean isInside(OverlayModule m, double mx, double my) {
        return mx >= m.getX() && mx < m.getX() + m.getWidth()
            && my >= m.getY() && my < m.getY() + m.getHeight();
    }

    private void refreshProfiles() {
        profiles = OverlayProfiles.options();
        String activeProfileId = OverlayProfiles.activeProfileId();
        currentProfileIndex = 0;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).id().equals(activeProfileId)) {
                currentProfileIndex = i;
                break;
            }
        }
    }

    private void selectProfile(String profileId) {
        OverlayProfiles.setActiveProfile(overlay, profileId);
        profileMenuOpen = false;
        refreshProfiles();
        rebuildWidgets();
    }

    private void toggleProfileMenu() {
        profileMenuOpen = !profileMenuOpen;
        rebuildWidgets();
    }

    private void toggleHideDisabled() {
        hideDisabledModules = !hideDisabledModules;
        if (hideDisabledModules && dragged != null && !dragged.isActive()) {
            dragged = null;
        }
        updateButtons();
    }

    private void updateButtons() {
        if (profileButton != null) {
            profileButton.setMessage(currentProfileMessage());
        }
        if (hideDisabledButton != null) {
            hideDisabledButton.setMessage(hideDisabledMessage());
        }
    }

    private Component currentProfileMessage() {
        if (profiles.isEmpty()) {
            return Component.translatable("screen.modern_f3.edit_overlay.profile", Component.literal("-"));
        }
        return Component.translatable("screen.modern_f3.edit_overlay.profile", profiles.get(currentProfileIndex).label());
    }

    private Component hideDisabledMessage() {
        return Component.translatable(
            "button.modern_f3.hide_disabled",
            Component.translatable(hideDisabledModules ? "options.on" : "options.off")
        );
    }

    private List<OverlayModule> visibleModules() {
        if (!hideDisabledModules) {
            return overlay.getModules();
        }

        List<OverlayModule> visible = new ArrayList<>();
        for (OverlayModule module : overlay.getModules()) {
            if (module.isActive()) {
                visible.add(module);
            }
        }
        return visible;
    }

    private int snap(int value, int origin, int step) {
        return origin + Math.round((value - origin) / (float) step) * step;
    }

    private Button compactButton(Component message, int x, int y, int width, Button.OnPress onPress) {
        return new CompactButton(x, y, width, BUTTON_HEIGHT, message, onPress);
    }

    private boolean isShiftDown() {
        return InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_LSHIFT)
            || InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_RSHIFT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        overlay.setEditMode(false);
        OverlayConfig.save(overlay);
    }

    private class CompactButton extends Button {
        protected CompactButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            boolean hovered = isHoveredOrFocused();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();

            graphics.fill(x, y, x + w, y + h, hovered ? 0xC0383838 : 0xA0202020);
            graphics.fill(x, y + h - 1, x + w, y + h, hovered ? 0x50FFFFFF : 0x20FFFFFF);

            int textWidth = Math.round(font.width(getMessage()) * BUTTON_TEXT_SCALE);
            int textHeight = Math.round(font.lineHeight * BUTTON_TEXT_SCALE);
            float textX = x + (w - textWidth) * 0.5F;
            float textY = y + (h - textHeight) * 0.5F;

            graphics.pose().pushMatrix();
            graphics.pose().translate(textX, textY);
            graphics.pose().scale(BUTTON_TEXT_SCALE, BUTTON_TEXT_SCALE);
            graphics.text(font, getMessage(), 0, 0, hovered ? 0xFFFFFFFF : (active ? 0xFFBBBBBB : 0xFF666666), false);
            graphics.pose().popMatrix();

            handleCursor(graphics);
        }
    }
}
