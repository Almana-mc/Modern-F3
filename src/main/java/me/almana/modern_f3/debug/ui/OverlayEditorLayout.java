package me.almana.modern_f3.debug.ui;

final class OverlayEditorLayout {
    static final int ICON_BUTTON_SIZE = 16;
    static final int BUTTON_GAP = 4;
    static final int PANEL_PADDING = 6;
    static final int DOCK_HEIGHT = 46;
    static final int COLLAPSED_DOCK_HEIGHT = 20;
    static final int EXPANDED_PANEL_WIDTH = ICON_BUTTON_SIZE * 4 + BUTTON_GAP * 3 + PANEL_PADDING * 2;
    static final int COLLAPSED_PANEL_WIDTH = ICON_BUTTON_SIZE + PANEL_PADDING * 2;

    private OverlayEditorLayout() {}

    static PanelBounds panelBounds(int screenWidth, int screenHeight, boolean expanded) {
        int width = Math.min(screenWidth, expanded ? EXPANDED_PANEL_WIDTH : COLLAPSED_PANEL_WIDTH);
        int height = expanded ? DOCK_HEIGHT : COLLAPSED_DOCK_HEIGHT;
        return new PanelBounds((screenWidth - width) / 2, screenHeight - height, width, height);
    }

    static int clampDraggedY(int requestedY, int moduleHeight, int screenHeight) {
        return Math.max(0, Math.min(requestedY, screenHeight - moduleHeight));
    }

    record PanelBounds(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
