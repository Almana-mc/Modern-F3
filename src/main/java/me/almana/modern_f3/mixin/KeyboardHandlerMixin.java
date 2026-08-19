package me.almana.modern_f3.mixin;

import me.almana.modern_f3.client.ClientEvents;
import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
//? if >=26.1
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    //? if >=26.1 {
    private void onKeyPress(long windowPointer, int action, KeyEvent event, CallbackInfo ci) {
    //?} else {
    /*private void onKeyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
    *///?}
        Minecraft mc = Minecraft.getInstance();
        if (Compat.screen(mc) != null) return;
        //? if >=26.1 {
        if (!DebugOverlay.TOGGLE_KEY.matches(event)) return;
        //?} else {
        /*if (!DebugOverlay.TOGGLE_KEY.matches(key, scanCode)) return;
        *///?}

        if (action == GLFW.GLFW_PRESS) {
            DebugOverlay.get().toggle();
            ClientEvents.showKeybindHintOnce(mc);
        }
        ci.cancel();
    }
}
