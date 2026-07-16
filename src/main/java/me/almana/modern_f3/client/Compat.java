package me.almana.modern_f3.client;

import net.minecraft.client.Minecraft;
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
}
