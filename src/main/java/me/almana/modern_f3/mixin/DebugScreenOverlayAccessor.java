package me.almana.modern_f3.mixin;

//? if <26.1 {
/*import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public interface DebugScreenOverlayAccessor {
    @Invoker("getGameInformation")
    List<String> modernF3$getGameInformation();

    @Invoker("getSystemInformation")
    List<String> modernF3$getSystemInformation();

    @Accessor("block")
    void modernF3$setBlock(HitResult block);

    @Accessor("liquid")
    void modernF3$setLiquid(HitResult liquid);
}
*///?}
