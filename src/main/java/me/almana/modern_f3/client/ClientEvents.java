package me.almana.modern_f3.client;

import me.almana.modern_f3.ModernF3;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.ui.OverlayEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
//?}

//? if neoforge
@EventBusSubscriber(modid = ModernF3.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static boolean keybindHintShown;

    public static void handleTick(Minecraft mc) {
        while (DebugOverlay.TOGGLE_KEY.consumeClick()) {
            DebugOverlay.get().toggle();
            showKeybindHintOnce(mc);
        }

        while (DebugOverlay.EDIT_KEY.consumeClick()) {
            if (Compat.screen(mc) == null) {
                Compat.setScreen(mc, new OverlayEditScreen());
            }
        }

        DebugOverlay.get().tick();
    }

    public static void showKeybindHintOnce(Minecraft mc) {
        if (keybindHintShown || !DebugOverlay.TOGGLE_KEY.isDefault()) {
            return;
        }

        keybindHintShown = true;
        Compat.overlayMessage(mc,
            Component.translatable("message.modern_f3.keybind_hint", Component.translatable("key.modern_f3.toggle_overlay")), false
        );
    }

    //? if neoforge {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        handleTick(Minecraft.getInstance());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        DebugOverlay.get().renderLayer(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        DebugOverlay.get().renderOnScreen(event.getGuiGraphics(), event.getPartialTick());
    }
    //?}
}
