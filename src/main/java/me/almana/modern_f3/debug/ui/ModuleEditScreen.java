package me.almana.modern_f3.debug.ui;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.config.OverlayConfig;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.overlay.OverlayModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ModuleEditScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.modern_f3.module_editor.title");
    private static final Component ENABLED = Component.translatable("screen.modern_f3.module_editor.enabled");
    private static final Component BACKGROUND = Component.translatable("screen.modern_f3.module_editor.background");
    private static final Component PREVIEW = Component.translatable("screen.modern_f3.module_editor.preview");
    private static final Component TEXT_COLOR = Component.translatable("screen.modern_f3.module_editor.text_color");
    private static final Component BACKGROUND_COLOR = Component.translatable("screen.modern_f3.module_editor.background_color");
    private static final Component COPY_STYLE = Component.translatable("button.modern_f3.copy_style");
    private static final Component PASTE_HERE = Component.translatable("button.modern_f3.paste_here");
    private static final Component PASTE_TO_ALL = Component.translatable("button.modern_f3.paste_to_all");
    private static final Component DONE = Component.translatable("button.modern_f3.done");
    private static final Component RESET = Component.translatable("button.modern_f3.reset");
    private static final int MAX_PICKER_HEIGHT = 116;
    private static final int MIN_PICKER_HEIGHT = 72;
    private static final int BUTTON_HEIGHT = 11;
    private static final int COLUMN_GAP = 10;
    private static final int ROW_GAP = 5;
    private static final int SMALL_BUTTON_WIDTH = 48;
    private static final int ACTION_BUTTON_WIDTH = 56;
    private static final float BUTTON_TEXT_SCALE = 0.7F;
    private static ModuleStyle copiedStyle;

    private final Screen parent;
    private final DebugOverlay overlay = DebugOverlay.get();
    private final OverlayModule module;
    private EditBox colorInput;
    private ValueSlider textBrightnessSlider;
    private ValueSlider backgroundBrightnessSlider;
    private Button pasteHereButton;
    private Button pasteToAllButton;
    private boolean syncingInputs;
    private boolean colorPickerOpen;
    private ColorTarget activeColorTarget = ColorTarget.TEXT;
    private float textHue;
    private float textSaturation;
    private float textBrightness;
    private float backgroundHue;
    private float backgroundSaturation;
    private float backgroundBrightness;

    public ModuleEditScreen(Screen parent, OverlayModule module) {
        super(TITLE);
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        overlay.setEditMode(true);
        loadColors();

        if (colorPickerOpen) {
            initPopup();
        } else {
            initMain();
        }
    }

    private void initMain() {
        module.tick();
        module.updateSize();
        ModuleEditorLayout layout = ModuleEditorLayout.calculate(width, module.getHeight());
        int controlWidth = layout.controlWidth();
        int left = width / 2 - (controlWidth * 2 + COLUMN_GAP) / 2;
        int right = left + controlWidth + COLUMN_GAP;
        int actionTotalWidth = ACTION_BUTTON_WIDTH * 3 + ROW_GAP * 2;
        int actionStartX = width / 2 - actionTotalWidth / 2;

        addRenderableWidget(compactButton(enabledMessage(), left, layout.toggleY(), controlWidth, button -> {
            module.setEnabled(!module.isEnabled());
            button.setMessage(enabledMessage());
            OverlayConfig.save(overlay);
        }));

        addRenderableWidget(compactButton(backgroundMessage(), right, layout.toggleY(), controlWidth, button -> {
            module.setShowBackground(!module.showBackground());
            button.setMessage(backgroundMessage());
            OverlayConfig.save(overlay);
        }));

        addRenderableWidget(compactButton(TEXT_COLOR, left, layout.targetY(), controlWidth, button -> {
            activeColorTarget = ColorTarget.TEXT;
            colorPickerOpen = true;
            rebuildWidgets();
        }));
        addRenderableWidget(compactButton(BACKGROUND_COLOR, right, layout.targetY(), controlWidth, button -> {
            activeColorTarget = ColorTarget.BACKGROUND;
            colorPickerOpen = true;
            rebuildWidgets();
        }));

        addRenderableWidget(new ValueSlider(
            left, layout.sliderY(), controlWidth, layout.sliderHeight(), "screen.modern_f3.module_editor.scale",
            Math.round(module.getScale() * 100.0F), 50, 200,
            value -> module.setScale(value / 100.0F)
        ));

        addRenderableWidget(new ValueSlider(
            right, layout.sliderY(), controlWidth, layout.sliderHeight(), "screen.modern_f3.module_editor.background_opacity",
            Math.round(module.getBackgroundOpacity() * 100.0F / 255.0F), 100,
            value -> module.setBackgroundOpacity(Math.round(value * 255.0F / 100.0F))
        ));

        addRenderableWidget(compactButton(COPY_STYLE, actionStartX, layout.actionY(), ACTION_BUTTON_WIDTH, button -> copyStyle()));
        pasteHereButton = addRenderableWidget(compactButton(PASTE_HERE, actionStartX + ACTION_BUTTON_WIDTH + ROW_GAP, layout.actionY(), ACTION_BUTTON_WIDTH, button -> pasteHere()));
        pasteToAllButton = addRenderableWidget(compactButton(PASTE_TO_ALL, actionStartX + (ACTION_BUTTON_WIDTH + ROW_GAP) * 2, layout.actionY(), ACTION_BUTTON_WIDTH, button -> pasteToAll()));
        addRenderableWidget(compactButton(RESET, width / 2 - SMALL_BUTTON_WIDTH - 6, height - 26, SMALL_BUTTON_WIDTH, button -> resetModule()));
        addRenderableWidget(compactButton(DONE, width / 2 + 6, height - 26, SMALL_BUTTON_WIDTH, button -> onClose()));
        updatePasteButtons();
    }

    private void initPopup() {
        int popupW = Math.min(220, width - 20);
        int pickerHeight = Math.min(MAX_PICKER_HEIGHT, Math.max(MIN_PICKER_HEIGHT, height - 120));
        int popupContentH = BUTTON_HEIGHT + ROW_GAP + pickerHeight + ROW_GAP + BUTTON_HEIGHT + ROW_GAP + BUTTON_HEIGHT + ROW_GAP + BUTTON_HEIGHT;
        int popupH = popupContentH + 16;
        int popupX = width / 2 - popupW / 2;
        int popupY = height / 2 - popupH / 2;
        int contentX = popupX + 8;
        int contentW = popupW - 16;
        int y = popupY + 8;

        Component label = activeColorTarget == ColorTarget.TEXT ? TEXT_COLOR : BACKGROUND_COLOR;
        addRenderableWidget(compactButton(Component.literal("[ ").append(label).append(" ]"), contentX, y, contentW, button -> {})).active = false;
        y += BUTTON_HEIGHT + ROW_GAP;

        addRenderableWidget(new SharedColorPicker(
            contentX, y, contentW, pickerHeight,
            this::activeHue, this::activeSaturation, this::activeBrightness,
            this::setActiveHsb, this::activeColorValue
        ));
        y += pickerHeight + ROW_GAP;

        colorInput = addRenderableWidget(createColorInput(
            contentX, y, contentW,
            Component.translatable("screen.modern_f3.module_editor.text_color_hex"),
            this::handleActiveColorInput
        ));
        syncColorInput();
        y += BUTTON_HEIGHT + ROW_GAP;

        if (activeColorTarget == ColorTarget.TEXT) {
            textBrightnessSlider = addRenderableWidget(new ValueSlider(
                contentX, y, contentW, "screen.modern_f3.module_editor.text_brightness",
                Math.round(textBrightness * 100.0F), 100,
                value -> { textBrightness = value / 100.0F; applyTextColor(); }
            ));
        } else {
            backgroundBrightnessSlider = addRenderableWidget(new ValueSlider(
                contentX, y, contentW, "screen.modern_f3.module_editor.background_brightness",
                Math.round(backgroundBrightness * 100.0F), 100,
                value -> { backgroundBrightness = value / 100.0F; applyBackgroundColor(); }
            ));
        }
        y += BUTTON_HEIGHT + ROW_GAP;

        addRenderableWidget(compactButton(DONE, contentX, y, contentW, button -> {
            colorPickerOpen = false;
            rebuildWidgets();
        }));
    }

    @Override
    //? if >=26.1 {
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    //?} else {
    /*public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    *///?}
        graphics.fill(0, 0, width, height, 0xC0101010);
    }

    @Override
    //? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    //?} else {
    /*public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    *///?}
        //? if <26.1
        /*renderBackground(graphics, mouseX, mouseY, a);*/
        if (colorPickerOpen) {
            drawPopupBackground(graphics);
        } else {
            String titleText = TITLE.getString();
            String moduleText = module.displayName();
            graphics.text(font, TITLE, width / 2 - font.width(titleText) / 2, 14, 0xFFFFFFFF, true);
            graphics.text(font, Component.literal(moduleText), width / 2 - font.width(moduleText) / 2, 28, 0xFFCCCCCC, false);
            drawPreview(graphics, a);
        }
        //? if >=26.1 {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        //?} else {
        /*super.render(graphics, mouseX, mouseY, a);
        *///?}
    }

    private void drawPopupBackground(GuiGraphicsExtractor graphics) {
        int popupW = Math.min(220, width - 20);
        int pickerHeight = Math.min(MAX_PICKER_HEIGHT, Math.max(MIN_PICKER_HEIGHT, height - 120));
        int popupContentH = BUTTON_HEIGHT + ROW_GAP + pickerHeight + ROW_GAP + BUTTON_HEIGHT + ROW_GAP + BUTTON_HEIGHT + ROW_GAP + BUTTON_HEIGHT;
        int popupH = popupContentH + 16;
        int popupX = width / 2 - popupW / 2;
        int popupY = height / 2 - popupH / 2;

        Component label = activeColorTarget == ColorTarget.TEXT ? TEXT_COLOR : BACKGROUND_COLOR;
        graphics.text(font, label, width / 2 - font.width(label) / 2, popupY - 14, 0xFFCCCCCC, true);
        graphics.fill(popupX, popupY, popupX + popupW, popupY + popupH, 0xD0101010);
        drawBorder(graphics, popupX, popupY, popupW, popupH, 0x80FFFFFF);
    }

    @Override
    public void onClose() {
        if (colorPickerOpen) {
            colorPickerOpen = false;
            rebuildWidgets();
            return;
        }
        Compat.setScreen(minecraft, parent);
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

    private void drawPreview(GuiGraphicsExtractor graphics, float a) {
        module.tick();
        module.updateSize();
        int moduleWidth = Math.max(module.getWidth(), 40);
        int moduleHeight = Math.max(module.getHeight(), 11);
        ModuleEditorLayout layout = ModuleEditorLayout.calculate(width, moduleHeight);
        int boxWidth = Math.min(Math.max(moduleWidth + 32, 220), previewWidth());
        int boxX = width / 2 - boxWidth / 2;
        int boxY = layout.previewY();
        int savedX = module.getConfigX();
        int savedY = module.getConfigY();

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + layout.previewHeight(), 0x50000000);
        drawBorder(graphics, boxX, boxY, boxWidth, layout.previewHeight(), 0x80FFFFFF);
        graphics.text(font, PREVIEW, width / 2 - font.width(PREVIEW.getString()) / 2, boxY + 4, 0xFFCCCCCC, false);

        module.setPosition(width / 2 - moduleWidth / 2, layout.moduleY());
        graphics.enableScissor(boxX + 1, layout.previewContentTop(), boxX + boxWidth - 1, layout.previewContentBottom());
        module.render(graphics, a);
        graphics.disableScissor();
        module.setPosition(savedX, savedY);
    }

    private int previewWidth() {
        return Math.max(220, width - 20);
    }

    private void resetModule() {
        module.setEnabled(true);
        module.setShowBackground(true);
        module.setTextColor(0xFFFFFFFF);
        module.setBackgroundColor(0x101010);
        module.setBackgroundOpacity(0xB0);
        module.setTextShadow(true);
        module.setScale(module.defaultScale());
        OverlayConfig.save(overlay);
        rebuildWidgets();
    }

    private void copyStyle() {
        copiedStyle = new ModuleStyle(
            module.isEnabled(),
            module.showBackground(),
            module.getTextColor(),
            module.getBackgroundColor(),
            module.getBackgroundOpacity(),
            module.useTextShadow(),
            module.getScale()
        );
        updatePasteButtons();
    }

    private void pasteHere() {
        if (copiedStyle == null) {
            return;
        }
        copiedStyle.apply(module);
        OverlayConfig.save(overlay);
        rebuildWidgets();
    }

    private void pasteToAll() {
        if (copiedStyle == null) {
            return;
        }
        for (OverlayModule overlayModule : overlay.getModules()) {
            copiedStyle.apply(overlayModule);
        }
        OverlayConfig.save(overlay);
        rebuildWidgets();
    }

    private void updatePasteButtons() {
        boolean hasCopiedStyle = copiedStyle != null;
        if (pasteHereButton != null) {
            pasteHereButton.active = hasCopiedStyle;
        }
        if (pasteToAllButton != null) {
            pasteToAllButton.active = hasCopiedStyle;
        }
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private void loadColors() {
        float[] textColor = Color.RGBtoHSB(red(module.getTextColor()), green(module.getTextColor()), blue(module.getTextColor()), null);
        float[] backgroundColor = Color.RGBtoHSB(
            red(module.getBackgroundColor()), green(module.getBackgroundColor()), blue(module.getBackgroundColor()), null
        );
        textHue = textColor[0];
        textSaturation = textColor[1];
        textBrightness = textColor[2];
        backgroundHue = backgroundColor[0];
        backgroundSaturation = backgroundColor[1];
        backgroundBrightness = backgroundColor[2];
    }

    private void applyTextColor() {
        module.setTextColor(0xFF000000 | (Color.HSBtoRGB(textHue, textSaturation, textBrightness) & 0x00FFFFFF));
        syncColorInput();
    }

    private void applyBackgroundColor() {
        module.setBackgroundColor(Color.HSBtoRGB(backgroundHue, backgroundSaturation, backgroundBrightness) & 0x00FFFFFF);
        syncColorInput();
    }

    private EditBox createColorInput(int x, int y, int width, Component narration, Consumer<String> responder) {
        EditBox input = new EditBox(font, x, y, width, BUTTON_HEIGHT, narration);
        input.setMaxLength(7);
        input.setHint(Component.translatable("screen.modern_f3.module_editor.color_hint"));
        //? if neoforge
        input.setFilter(value -> value.matches("#?[0-9a-fA-F]{0,6}"));
        input.setResponder(responder);
        return input;
    }

    private Component enabledMessage() {
        return Component.translatable(
            "screen.modern_f3.module_editor.state",
            ENABLED,
            Component.translatable(module.isEnabled() ? "options.on" : "options.off")
        );
    }

    private Component backgroundMessage() {
        return Component.translatable(
            "screen.modern_f3.module_editor.state",
            BACKGROUND,
            Component.translatable(module.showBackground() ? "options.on" : "options.off")
        );
    }

    private Button compactButton(Component message, int x, int y, int width, Button.OnPress onPress) {
        return new CompactButton(x, y, width, BUTTON_HEIGHT, message, onPress);
    }

    private float activeHue() {
        return activeColorTarget == ColorTarget.TEXT ? textHue : backgroundHue;
    }

    private float activeSaturation() {
        return activeColorTarget == ColorTarget.TEXT ? textSaturation : backgroundSaturation;
    }

    private float activeBrightness() {
        return activeColorTarget == ColorTarget.TEXT ? textBrightness : backgroundBrightness;
    }

    private int activeColorValue() {
        return activeColorTarget == ColorTarget.TEXT ? module.getTextColor() : module.getBackgroundColor();
    }

    private void setActiveHsb(float hue, float saturation, float brightness) {
        if (activeColorTarget == ColorTarget.TEXT) {
            textHue = hue;
            textSaturation = saturation;
            textBrightness = brightness;
            if (textBrightnessSlider != null) {
                textBrightnessSlider.setCurrentValue(Math.round(textBrightness * 100.0F));
            }
            applyTextColor();
            return;
        }

        backgroundHue = hue;
        backgroundSaturation = saturation;
        backgroundBrightness = brightness;
        if (backgroundBrightnessSlider != null) {
            backgroundBrightnessSlider.setCurrentValue(Math.round(backgroundBrightness * 100.0F));
        }
        applyBackgroundColor();
    }

    private void handleActiveColorInput(String value) {
        if (syncingInputs) {
            return;
        }
        Integer color = parseColor(value);
        if (color == null) {
            return;
        }
        float[] hsb = Color.RGBtoHSB(red(color), green(color), blue(color), null);
        setActiveHsb(hsb[0], hsb[1], hsb[2]);
    }

    private Integer parseColor(String value) {
        String normalized = normalizeColor(value);
        if (normalized.length() != 6) {
            return null;
        }
        return Integer.parseInt(normalized, 16);
    }

    private String normalizeColor(String value) {
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private void syncColorInput() {
        syncingInputs = true;
        try {
            if (colorInput != null) {
                colorInput.setValue(formatColor(activeColorValue()));
            }
        } finally {
            syncingInputs = false;
        }
    }

    private String formatColor(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    private int red(int color) {
        return (color >> 16) & 0xFF;
    }

    private int green(int color) {
        return (color >> 8) & 0xFF;
    }

    private int blue(int color) {
        return color & 0xFF;
    }

    private interface HsvSetter {
        void accept(float hue, float saturation, float brightness);
    }

    private class SharedColorPicker extends AbstractWidget {
        private static final int PADDING = 6;
        private static final int HUE_BAR_WIDTH = 14;
        private final Supplier<Float> hue;
        private final Supplier<Float> saturation;
        private final Supplier<Float> brightness;
        private final HsvSetter setter;
        private final IntSupplier previewColorSupplier;
        private DragMode dragMode = DragMode.NONE;

        SharedColorPicker(
            int x,
            int y,
            int width,
            int height,
            Supplier<Float> hue,
            Supplier<Float> saturation,
            Supplier<Float> brightness,
            HsvSetter setter,
            IntSupplier previewColorSupplier
        ) {
            super(x, y, width, height, Component.empty());
            this.hue = hue;
            this.saturation = saturation;
            this.brightness = brightness;
            this.setter = setter;
            this.previewColorSupplier = previewColorSupplier;
        }

        @Override
        //? if >=26.1 {
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //?} else {
        /*protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        *///?}
            PickerLayout layout = layout();
            int step = 2;
            float currentHue = hue.get();

            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x50000000);
            drawBorder(graphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused() ? 0x80FFFFFF : 0x50FFFFFF);

            for (int x = 0; x < layout.squareSize; x += step) {
                float sat = x / (float)Math.max(1, layout.squareSize - 1);
                for (int y = 0; y < layout.squareSize; y += step) {
                    float value = 1.0F - y / (float)Math.max(1, layout.squareSize - 1);
                    int color = Color.HSBtoRGB(currentHue, sat, value) | 0xFF000000;
                    graphics.fill(
                        layout.squareX + x,
                        layout.squareY + y,
                        layout.squareX + Math.min(layout.squareSize, x + step),
                        layout.squareY + Math.min(layout.squareSize, y + step),
                        color
                    );
                }
            }

            for (int y = 0; y < layout.hueHeight; y += step) {
                float hue = y / (float)Math.max(1, layout.hueHeight - 1);
                int color = Color.HSBtoRGB(hue, 1.0F, 1.0F) | 0xFF000000;
                graphics.fill(
                    layout.hueX,
                    layout.hueY + y,
                    layout.hueX + layout.hueWidth,
                    layout.hueY + Math.min(layout.hueHeight, y + step),
                    color
                );
            }

            drawBorder(graphics, layout.squareX - 1, layout.squareY - 1, layout.squareSize + 2, layout.squareSize + 2, 0xB0FFFFFF);
            drawBorder(graphics, layout.hueX - 1, layout.hueY - 1, layout.hueWidth + 2, layout.hueHeight + 2, 0xB0FFFFFF);

            int markerX = layout.squareX + Math.round(saturation.get() * (layout.squareSize - 1));
            int markerY = layout.squareY + Math.round((1.0F - brightness.get()) * (layout.squareSize - 1));
            graphics.fill(markerX - 3, markerY - 3, markerX + 4, markerY + 4, 0xFF000000);
            graphics.fill(markerX - 2, markerY - 2, markerX + 3, markerY + 3, 0xFFFFFFFF);

            int hueMarkerY = layout.hueY + Math.round(currentHue * (layout.hueHeight - 1));
            graphics.fill(layout.hueX - 2, hueMarkerY - 1, layout.hueX + layout.hueWidth + 2, hueMarkerY + 2, 0xFF000000);
            graphics.fill(layout.hueX - 1, hueMarkerY, layout.hueX + layout.hueWidth + 1, hueMarkerY + 1, 0xFFFFFFFF);

            int previewColorValue = previewColorSupplier.getAsInt();
            graphics.fill(
                layout.previewX,
                layout.previewY,
                layout.previewX + layout.previewSize,
                layout.previewY + layout.previewSize,
                0xFF000000 | (previewColorValue & 0x00FFFFFF)
            );
            drawBorder(graphics, layout.previewX - 1, layout.previewY - 1, layout.previewSize + 2, layout.previewSize + 2, 0xB0FFFFFF);

            //? if >=26.1
            handleCursor(graphics);
        }

        @Override
        //? if >=26.1 {
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            updateFromClick(event.x(), event.y());
        }
        //?} else {
        /*public void onClick(double mouseX, double mouseY) {
            updateFromClick(mouseX, mouseY);
        }
        *///?}

        private void updateFromClick(double mouseX, double mouseY) {
            PickerLayout layout = layout();
            dragMode = dragMode(layout, mouseX, mouseY);
            updateFromMouse(layout, mouseX, mouseY, dragMode);
        }

        @Override
        //? if >=26.1 {
        protected void onDrag(MouseButtonEvent event, double dx, double dy) {
            updateFromDrag(event.x(), event.y());
        }
        //?} else {
        /*protected void onDrag(double mouseX, double mouseY, double dx, double dy) {
            updateFromDrag(mouseX, mouseY);
        }
        *///?}

        private void updateFromDrag(double mouseX, double mouseY) {
            PickerLayout layout = layout();
            if (dragMode == DragMode.NONE) {
                dragMode = dragMode(layout, mouseX, mouseY);
            }
            updateFromMouse(layout, mouseX, mouseY, dragMode);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, activeColorTarget == ColorTarget.TEXT ? TEXT_COLOR : BACKGROUND_COLOR);
        }

        private PickerLayout layout() {
            int squareX = getX() + PADDING;
            int squareY = getY() + PADDING;
            int innerWidth = Math.max(80, getWidth() - PADDING * 2);
            int innerHeight = Math.max(80, getHeight() - PADDING * 2);
            int previewSize = 28;
            int squareSize = Math.max(56, Math.min(innerHeight, innerWidth - HUE_BAR_WIDTH - previewSize - 18));
            int hueX = squareX + squareSize + 6;
            int previewX = hueX + HUE_BAR_WIDTH + 6;
            if (previewX + previewSize > getX() + getWidth() - PADDING) {
                previewX = getX() + getWidth() - PADDING - previewSize;
            }
            int previewY = squareY + 2;
            return new PickerLayout(squareX, squareY, squareSize, hueX, squareY, HUE_BAR_WIDTH, squareSize, previewX, previewY, previewSize);
        }

        private DragMode dragMode(PickerLayout layout, double mouseX, double mouseY) {
            if (inside(layout.squareX, layout.squareY, layout.squareSize, layout.squareSize, mouseX, mouseY)) {
                return DragMode.SQUARE;
            }
            if (inside(layout.hueX, layout.hueY, layout.hueWidth, layout.hueHeight, mouseX, mouseY)) {
                return DragMode.HUE;
            }
            return DragMode.NONE;
        }

        private void updateFromMouse(PickerLayout layout, double mouseX, double mouseY, DragMode mode) {
            float newHue = hue.get();
            float newSaturation = saturation.get();
            float newBrightness = brightness.get();

            if (mode == DragMode.SQUARE) {
                newSaturation = Mth.clamp((float)((mouseX - layout.squareX) / Math.max(1.0, layout.squareSize - 1.0)), 0.0F, 1.0F);
                newBrightness = 1.0F - Mth.clamp((float)((mouseY - layout.squareY) / Math.max(1.0, layout.squareSize - 1.0)), 0.0F, 1.0F);
            } else if (mode == DragMode.HUE) {
                newHue = Mth.clamp((float)((mouseY - layout.hueY) / Math.max(1.0, layout.hueHeight - 1.0)), 0.0F, 1.0F);
            } else {
                return;
            }

            setter.accept(newHue, newSaturation, newBrightness);
        }

        private boolean inside(int x, int y, int width, int height, double mouseX, double mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }

        private record PickerLayout(
            int squareX,
            int squareY,
            int squareSize,
            int hueX,
            int hueY,
            int hueWidth,
            int hueHeight,
            int previewX,
            int previewY,
            int previewSize
        ) {}

        private enum DragMode {
            NONE,
            SQUARE,
            HUE
        }
    }

    private class ValueSlider extends AbstractSliderButton {
        private final String translationKey;
        private final IntConsumer setter;
        private final int maxValue;

        private final int minValue;

        ValueSlider(int x, int y, int width, String translationKey, int initialValue, int maxValue, IntConsumer setter) {
            this(x, y, width, translationKey, initialValue, 0, maxValue, setter);
        }

        ValueSlider(int x, int y, int width, int height, String translationKey, int initialValue, int maxValue, IntConsumer setter) {
            this(x, y, width, height, translationKey, initialValue, 0, maxValue, setter);
        }

        ValueSlider(int x, int y, int width, String translationKey, int initialValue, int minValue, int maxValue, IntConsumer setter) {
            this(x, y, width, BUTTON_HEIGHT, translationKey, initialValue, minValue, maxValue, setter);
        }

        ValueSlider(int x, int y, int width, int height, String translationKey, int initialValue, int minValue, int maxValue, IntConsumer setter) {
            super(x, y, width, height, CommonComponents.EMPTY, maxValue == minValue ? 0.0 : (initialValue - minValue) / (double)(maxValue - minValue));
            this.translationKey = translationKey;
            this.setter = setter;
            this.minValue = minValue;
            this.maxValue = maxValue;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey, currentValue()));
        }

        @Override
        protected void applyValue() {
            setter.accept(currentValue());
            updateMessage();
        }

        private void setCurrentValue(int currentValue) {
            value = maxValue == minValue ? 0.0 : Mth.clamp((currentValue - minValue) / (double)(maxValue - minValue), 0.0, 1.0);
            updateMessage();
        }

        private int currentValue() {
            return minValue + (int)Math.round(value * (maxValue - minValue));
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

    private enum ColorTarget {
        TEXT,
        BACKGROUND
    }

    private record ModuleStyle(
        boolean enabled,
        boolean backgroundVisible,
        int textColor,
        int backgroundColor,
        int backgroundOpacity,
        boolean textShadow,
        float scale
    ) {
        private void apply(OverlayModule module) {
            module.setEnabled(enabled);
            module.setShowBackground(backgroundVisible);
            module.setTextColor(textColor);
            module.setBackgroundColor(backgroundColor);
            module.setBackgroundOpacity(backgroundOpacity);
            module.setTextShadow(textShadow);
            module.setScale(scale);
        }
    }
}
