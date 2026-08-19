package me.almana.modern_f3.debug.ui;

record ModuleEditorLayout(
    int controlWidth,
    int sliderHeight,
    int previewY,
    int previewHeight,
    int previewContentTop,
    int previewContentBottom,
    int moduleY,
    int toggleY,
    int targetY,
    int sliderY,
    int actionY
) {
    private static final int PREVIEW_Y = 48;
    private static final int PREVIEW_HEIGHT = 46;
    private static final int PREVIEW_LABEL_HEIGHT = 17;
    private static final int BUTTON_HEIGHT = 11;
    private static final int SLIDER_HEIGHT = 16;
    private static final int ROW_GAP = 5;

    static ModuleEditorLayout calculate(int screenWidth, int moduleHeight) {
        int controlWidth = Math.min(150, Math.max(80, (screenWidth - 30) / 2));
        int previewContentTop = PREVIEW_Y + PREVIEW_LABEL_HEIGHT;
        int previewContentBottom = PREVIEW_Y + PREVIEW_HEIGHT - 4;
        int contentHeight = previewContentBottom - previewContentTop;
        int moduleY = previewContentTop + Math.max(0, (contentHeight - moduleHeight) / 2);
        int toggleY = PREVIEW_Y + PREVIEW_HEIGHT + 8;
        int targetY = toggleY + BUTTON_HEIGHT + ROW_GAP;
        int sliderY = targetY + BUTTON_HEIGHT + ROW_GAP;
        int actionY = sliderY + SLIDER_HEIGHT + ROW_GAP;
        return new ModuleEditorLayout(
            controlWidth,
            SLIDER_HEIGHT,
            PREVIEW_Y,
            PREVIEW_HEIGHT,
            previewContentTop,
            previewContentBottom,
            moduleY,
            toggleY,
            targetY,
            sliderY,
            actionY
        );
    }
}
