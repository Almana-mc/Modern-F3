package me.almana.modern_f3;

import me.almana.modern_f3.debug.overlay.DebugOverlay;
//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
//?}

//? if neoforge
@Mod(ModernF3.MODID)
public class ModernF3 {
    public static final String MODID = "modern_f3";

    //? if neoforge {
    public ModernF3(IEventBus modEventBus, ModContainer modContainer) {
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.registerCategory(DebugOverlay.CATEGORY);
            event.register(DebugOverlay.TOGGLE_KEY);
            event.register(DebugOverlay.EDIT_KEY);
        }
    }
    //?}
}
