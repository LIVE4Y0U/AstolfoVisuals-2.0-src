package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(class_757.class)
public interface GameRendererAccessor {
   @Invoker("updateCrosshairTarget")
   void invokeUpdateCrosshairTarget(float var1);
}
