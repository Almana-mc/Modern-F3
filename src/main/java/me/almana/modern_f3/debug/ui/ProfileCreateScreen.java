package me.almana.modern_f3.debug.ui;

import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.config.OverlayProfiles;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProfileCreateScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.modern_f3.profile_create.title");
    private static final Component CREATE = Component.translatable("button.modern_f3.create_profile");
    private static final int CARD_WIDTH = 220;
    private static final int CARD_HEIGHT = 90;

    private final Screen parent;
    private final DebugOverlay overlay = DebugOverlay.get();
    private EditBox nameInput;
    private Button createButton;

    public ProfileCreateScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cardX = width / 2 - CARD_WIDTH / 2;
        int cardY = height / 2 - CARD_HEIGHT / 2;
        int innerX = cardX + 10;
        int inputY = cardY + 28;

        nameInput = addRenderableWidget(new EditBox(
            font,
            innerX,
            inputY,
            CARD_WIDTH - 20,
            20,
            Component.translatable("screen.modern_f3.profile_create.name")
        ));
        nameInput.setMaxLength(40);
        nameInput.setHint(Component.translatable("screen.modern_f3.profile_create.hint"));
        nameInput.setResponder(value -> updateCreateButton());
        setInitialFocus(nameInput);

        int buttonY = inputY + 26;
        int buttonW = (CARD_WIDTH - 24) / 2;
        createButton = addRenderableWidget(Button.builder(CREATE, btn -> createProfile())
            .bounds(innerX, buttonY, buttonW, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, btn -> onClose())
            .bounds(innerX + buttonW + 4, buttonY, buttonW, 20).build());
        updateCreateButton();
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
        int cardX = width / 2 - CARD_WIDTH / 2;
        int cardY = height / 2 - CARD_HEIGHT / 2;

        graphics.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + CARD_HEIGHT, 0xD0181818);
        drawBorder(graphics, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0x60FFFFFF);

        graphics.text(font, TITLE, width / 2 - font.width(TITLE.getString()) / 2, cardY + 8, 0xFFE0E0E0, false);
        //? if >=26.1 {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        //?} else {
        /*super.render(graphics, mouseX, mouseY, a);
        *///?}
    }

    private void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    //? if >=26.1 {
    public boolean keyPressed(KeyEvent event) {
        if (submitOnEnter(event.key())) return true;
        return super.keyPressed(event);
    }
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (submitOnEnter(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    private boolean submitOnEnter(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (createButton.active) {
                createProfile();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        Compat.setScreen(minecraft, parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void createProfile() {
        OverlayProfiles.createProfile(overlay, nameInput.getValue());
        Compat.setScreen(minecraft, parent);
    }

    private void updateCreateButton() {
        if (createButton != null) {
            createButton.active = !nameInput.getValue().trim().isEmpty();
        }
    }
}
