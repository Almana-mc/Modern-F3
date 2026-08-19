package me.almana.modern_f3.debug.ui;

import com.mojang.blaze3d.platform.InputConstants;
import me.almana.modern_f3.ModernF3;
import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.config.OverlayConfig;
import me.almana.modern_f3.debug.config.OverlayProfiles;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.overlay.ModuleEnabledState;
import me.almana.modern_f3.debug.overlay.OverlayModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class OverlayEditScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.modern_f3.edit_overlay.title");
    private static final Component DONE = Component.translatable("button.modern_f3.done");
    private static final Component RESET = Component.translatable("button.modern_f3.reset");
    private static final Component NEW_PROFILE = Component.translatable("button.modern_f3.new_profile");
    private static final Component ENABLE_ALL = Component.translatable("button.modern_f3.enable_all");
    private static final Component DISABLE_ALL = Component.translatable("button.modern_f3.disable_all");
    private static final Component HIDE_CONTROLS = Component.translatable("button.modern_f3.hide_controls");
    private static final Component SHOW_CONTROLS = Component.translatable("button.modern_f3.show_controls");
    private static final int BUTTON_HEIGHT = 12;
    private static final int PROFILE_BUTTON_WIDTH = 96;
    private static final float BUTTON_TEXT_SCALE = 0.7F;
    private static final Identifier EYE_ICON = icon("eye");
    private static final Identifier USER_ICON = icon("user");
    private static final Identifier USER_ADD_ICON = icon("user_add");
    private static final Identifier MENU_ICON = icon("menu");
    private static final Identifier CHECK_ICON = icon("check");
    private static final Identifier CROSS_ICON = icon("cross");
    private static final Identifier REFRESH_ICON = icon("refresh");
    private static final Identifier ARROW_RIGHT_ICON = icon("arrow_right");
    private static boolean hideDisabledModules;

    private final DebugOverlay overlay = DebugOverlay.get();
    private OverlayModule dragged;
    private int dragOffX, dragOffY;
    private IconButton profileButton;
    private IconButton hideDisabledButton;
    private List<OverlayProfiles.ProfileOption> profiles = List.of();
    private int currentProfileIndex;
    private boolean profileMenuOpen;
    private boolean dockExpanded = true;
    private int dropdownX, dropdownY, dropdownW, dropdownH;

    private static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(ModernF3.MODID, "textures/gui/editor/" + name + ".png");
    }

    public OverlayEditScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        overlay.setEditMode(true);
        refreshProfiles();
        OverlayEditorLayout.PanelBounds panel = panelBounds();
        if (!dockExpanded) {
            int buttonX = panel.x() + OverlayEditorLayout.PANEL_PADDING;
            int buttonY = panel.y() + 2;
            addRenderableWidget(iconButton(SHOW_CONTROLS, MENU_ICON, buttonX, buttonY, btn -> toggleDock()));
            dropdownH = 0;
            return;
        }

        int firstRowY = panel.y() + 5;
        int secondRowY = panel.y() + 25;
        int startX = panel.x() + OverlayEditorLayout.PANEL_PADDING;

        hideDisabledButton = addRenderableWidget(iconButton(hideDisabledMessage(), EYE_ICON, startX, firstRowY, btn -> toggleHideDisabled()));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        profileButton = addRenderableWidget(iconButton(currentProfileMessage(), USER_ICON, startX, firstRowY, btn -> toggleProfileMenu()));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        addRenderableWidget(iconButton(NEW_PROFILE, USER_ADD_ICON, startX, firstRowY, btn -> {
            profileMenuOpen = false;
            Compat.setScreen(minecraft, new ProfileCreateScreen(this));
        }));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        addRenderableWidget(iconButton(HIDE_CONTROLS, MENU_ICON, startX, firstRowY, btn -> toggleDock()));

        startX = panel.x() + OverlayEditorLayout.PANEL_PADDING;
        addRenderableWidget(iconButton(ENABLE_ALL, CHECK_ICON, startX, secondRowY, btn -> setAllModulesEnabled(true)));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        addRenderableWidget(iconButton(DISABLE_ALL, CROSS_ICON, startX, secondRowY, btn -> setAllModulesEnabled(false)));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        addRenderableWidget(iconButton(RESET, REFRESH_ICON, startX, secondRowY, btn -> resetLayout()));
        startX += OverlayEditorLayout.ICON_BUTTON_SIZE + OverlayEditorLayout.BUTTON_GAP;
        addRenderableWidget(iconButton(DONE, ARROW_RIGHT_ICON, startX, secondRowY, btn -> onClose()));

        if (profileMenuOpen && !profiles.isEmpty()) {
            int optionX = profileButton.getX();
            int firstOptionY = Math.max(24, panel.y() - profiles.size() * (BUTTON_HEIGHT + 2) - 4);
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
    //? if >=26.1 {
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    //?} else {
    /*public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    *///?}
        graphics.fill(0, 0, width, height, 0x60000000);
    }

    @Override
    //? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    //?} else {
    /*public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    *///?}
        //? if <26.1
        /*renderBackground(graphics, mouseX, mouseY, a);*/
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

        //? if >=26.1 {
        graphics.nextStratum();
        //?} else {
        /*graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        *///?}

        drawDockBackground(graphics);

        if (profileMenuOpen && dropdownH > 0) {
            graphics.fill(dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + dropdownH, 0xD0101010);
            drawBorder(graphics, dropdownX, dropdownY, dropdownW, dropdownH, 0x60FFFFFF);
        }

        //? if >=26.1 {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        //?} else {
        /*super.render(graphics, mouseX, mouseY, a);
        *///?}

        //? if <26.1
        /*graphics.pose().popPose();*/
    }

    private void drawDockBackground(GuiGraphicsExtractor graphics) {
        OverlayEditorLayout.PanelBounds panel = panelBounds();
        graphics.fill(panel.x(), panel.y(), panel.x() + panel.width(), panel.y() + panel.height(), 0xFF101010);
        drawBorder(graphics, panel.x(), panel.y(), panel.width(), panel.height(), 0x60FFFFFF);
    }

    private OverlayEditorLayout.PanelBounds panelBounds() {
        return OverlayEditorLayout.panelBounds(width, height, dockExpanded);
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
    //? if >=26.1 {
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        return handleMouseClicked(event.x(), event.y(), event.button());
    }
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        return handleMouseClicked(mouseX, mouseY, button);
    }
    *///?}

    private boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (panelBounds().contains(mouseX, mouseY)) {
            return false;
        }
        List<OverlayModule> modules = visibleModules();
        for (int i = modules.size() - 1; i >= 0; i--) {
            OverlayModule m = modules.get(i);
            if (isInside(m, mouseX, mouseY)) {
                if (button == 0) {
                    dragged = m;
                    dragOffX = (int) mouseX - m.getX();
                    dragOffY = (int) mouseY - m.getY();
                    return true;
                }
                if (button == 1) {
                    Compat.setScreen(minecraft, new ModuleEditScreen(this, m));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    //? if >=26.1 {
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (handleMouseDragged(event.x(), event.y(), event.button())) return true;
        return super.mouseDragged(event, dx, dy);
    }
    //?} else {
    /*public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (handleMouseDragged(mouseX, mouseY, button)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    *///?}

    private boolean handleMouseDragged(double mouseX, double mouseY, int button) {
        if (dragged != null && button == 0) {
            int nx = (int) mouseX - dragOffX;
            int ny = (int) mouseY - dragOffY;

            if (isShiftDown()) {
                nx = snap(nx, DebugOverlay.DEFAULT_X, DebugOverlay.SNAP_X_STEP);
                ny = snap(ny, DebugOverlay.DEFAULT_Y, DebugOverlay.DEFAULT_ROW_STEP);
            }

            nx = Math.max(0, Math.min(nx, width - dragged.getWidth()));
            ny = OverlayEditorLayout.clampDraggedY(ny, dragged.getHeight(), height);
            dragged.setPosition(nx, ny);
            return true;
        }
        return false;
    }

    @Override
    //? if >=26.1 {
    public boolean mouseReleased(MouseButtonEvent event) {
        if (handleMouseReleased(event.button())) return true;
        return super.mouseReleased(event);
    }
    //?} else {
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (handleMouseReleased(button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *///?}

    private boolean handleMouseReleased(int button) {
        if (dragged != null && button == 0) {
            dragged = null;
            OverlayConfig.save(overlay);
            return true;
        }
        return false;
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

    private void toggleDock() {
        dockExpanded = !dockExpanded;
        profileMenuOpen = false;
        rebuildWidgets();
    }

    private void setAllModulesEnabled(boolean enabled) {
        ModuleEnabledState.setAll(overlay.getModules(), enabled);
        if (!enabled) {
            dragged = null;
        }
        OverlayConfig.save(overlay);
    }

    private void updateButtons() {
        if (profileButton != null) {
            profileButton.setLabel(currentProfileMessage());
        }
        if (hideDisabledButton != null) {
            hideDisabledButton.setLabel(hideDisabledMessage());
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

    private IconButton iconButton(Component message, Identifier icon, int x, int y, Button.OnPress onPress) {
        return new IconButton(x, y, message, icon, onPress);
    }

    private boolean isShiftDown() {
        //? if >=26.1 {
        return InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_LSHIFT)
            || InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_RSHIFT);
        //?} else {
        /*return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LSHIFT)
            || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RSHIFT);
        *///?}
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

    private class IconButton extends Button {
        private final Identifier icon;

        private IconButton(int x, int y, Component message, Identifier icon, Button.OnPress onPress) {
            super(
                x,
                y,
                OverlayEditorLayout.ICON_BUTTON_SIZE,
                OverlayEditorLayout.ICON_BUTTON_SIZE,
                message,
                onPress,
                DEFAULT_NARRATION
            );
            this.icon = icon;
            setTooltip(Tooltip.create(message));
        }

        private void setLabel(Component message) {
            setMessage(message);
            setTooltip(Tooltip.create(message));
        }

        @Override
        //? if >=26.1 {
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //?} else {
        /*protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        *///?}
            boolean hovered = isHoveredOrFocused();
            int x = getX(), y = getY(), size = getWidth();
            int background = active ? (hovered ? 0xFFFFFFFF : 0xFFD0D0D0) : 0xFF707070;

            graphics.fill(x, y, x + size, y + size, background);
            graphics.fill(x, y, x + size, y + 1, 0xFFFFFFFF);
            graphics.fill(x, y, x + 1, y + size, 0xFFFFFFFF);
            graphics.fill(x, y + size - 1, x + size, y + size, 0xFF555555);
            graphics.fill(x + size - 1, y, x + size, y + size, 0xFF555555);
            Compat.blitIcon(graphics, icon, x + 3, y + 3, size - 6);

            //? if >=26.1
            handleCursor(graphics);
        }
    }

    private class CompactButton extends Button {
        protected CompactButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        //? if >=26.1 {
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //?} else {
        /*protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        *///?}
            boolean hovered = isHoveredOrFocused();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();

            graphics.fill(x, y, x + w, y + h, hovered ? 0xC0383838 : 0xA0202020);
            graphics.fill(x, y + h - 1, x + w, y + h, hovered ? 0x50FFFFFF : 0x20FFFFFF);

            int textWidth = Math.round(font.width(getMessage()) * BUTTON_TEXT_SCALE);
            int textHeight = Math.round(font.lineHeight * BUTTON_TEXT_SCALE);
            float textX = x + (w - textWidth) * 0.5F;
            float textY = y + (h - textHeight) * 0.5F;

            Compat.pushPose(graphics);
            Compat.translate(graphics, textX, textY);
            Compat.scale(graphics, BUTTON_TEXT_SCALE);
            graphics.text(font, getMessage(), 0, 0, hovered ? 0xFFFFFFFF : (active ? 0xFFBBBBBB : 0xFF666666), false);
            Compat.popPose(graphics);

            //? if >=26.1
            handleCursor(graphics);
        }
    }
}
