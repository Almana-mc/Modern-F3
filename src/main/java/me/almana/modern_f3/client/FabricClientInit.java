package me.almana.modern_f3.client;

//? if fabric && >=26.1 {
/*import me.almana.modern_f3.ModernF3;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.resources.Identifier;

public class FabricClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(DebugOverlay.TOGGLE_KEY);
        KeyMappingHelper.registerKeyMapping(DebugOverlay.EDIT_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::handleTick);

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(ModernF3.MODID, "overlay"),
            (graphics, deltaTracker) -> DebugOverlay.get().renderLayer(graphics, deltaTracker)
        );

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                DebugOverlay.get().renderOnScreen(graphics, tickDelta)));
    }
}
*///?} else if fabric {
/*import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class FabricClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(DebugOverlay.TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(DebugOverlay.EDIT_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::handleTick);

        HudRenderCallback.EVENT.register((graphics, deltaTracker) ->
            DebugOverlay.get().renderLayer(graphics, deltaTracker));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                DebugOverlay.get().renderOnScreen(graphics, tickDelta)));
    }
}
*///?}
