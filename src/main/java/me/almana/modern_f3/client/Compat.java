package me.almana.modern_f3.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Compat {
    private Compat() {}

    public static Screen screen(Minecraft mc) {
        //? if >=26.2 {
        /*return mc.gui.screen();
        *///?} else {
        return mc.screen;
        //?}
    }

    public static void setScreen(Minecraft mc, Screen screen) {
        //? if >=26.2 {
        /*mc.gui.setScreen(screen);
        *///?} else {
        mc.setScreen(screen);
        //?}
    }

    public static int guiTicks(Minecraft mc) {
        //? if >=26.2 {
        /*return mc.gui.hud.getGuiTicks();
        *///?} else {
        return mc.gui.getGuiTicks();
        //?}
    }

    public static void overlayMessage(Minecraft mc, Component message, boolean animate) {
        //? if >=26.2 {
        /*mc.gui.hud.setOverlayMessage(message, animate);
        *///?} else {
        mc.gui.setOverlayMessage(message, animate);
        //?}
    }

    public static boolean hudHidden(Minecraft mc) {
        //? if >=26.2 {
        /*return mc.gui.hud.isHidden();
        *///?} else {
        return mc.options.hideGui;
        //?}
    }

    public static void pushPose(GuiGraphicsExtractor graphics) {
        //? if >=26.1 {
        graphics.pose().pushMatrix();
        //?} else {
        /*graphics.pose().pushPose();
        *///?}
    }

    public static void translate(GuiGraphicsExtractor graphics, float x, float y) {
        //? if >=26.1 {
        graphics.pose().translate(x, y);
        //?} else {
        /*graphics.pose().translate(x, y, 0);
        *///?}
    }

    public static void scale(GuiGraphicsExtractor graphics, float scale) {
        //? if >=26.1 {
        graphics.pose().scale(scale, scale);
        //?} else {
        /*graphics.pose().scale(scale, scale, 1);
        *///?}
    }

    public static void popPose(GuiGraphicsExtractor graphics) {
        //? if >=26.1 {
        graphics.pose().popMatrix();
        //?} else {
        /*graphics.pose().popPose();
        *///?}
    }
}
