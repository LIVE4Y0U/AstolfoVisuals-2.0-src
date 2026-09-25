package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(client.render.GameRenderer.class)
public interface GameRendererAccessor {
   @Invoker("updateCrosshairTarget")
   void invokeUpdateCrosshairTarget(float var1);
}
