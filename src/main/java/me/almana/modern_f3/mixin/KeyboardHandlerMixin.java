package me.almana.modern_f3.mixin;

import me.almana.modern_f3.client.ClientEvents;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long windowPointer, int action, KeyEvent event, CallbackInfo ci) {
        if (!DebugOverlay.TOGGLE_KEY.isDefault()) return;
        if (event.key() != GLFW.GLFW_KEY_F3 || action != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        DebugOverlay.get().toggle();
        ClientEvents.showKeybindHintOnce(mc);
        ci.cancel();
    }
}
