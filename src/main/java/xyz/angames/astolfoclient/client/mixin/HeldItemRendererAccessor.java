package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Arm;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(render.item.HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
   @Invoker("renderArmHoldingItem")
   void invokeRenderArmHoldingItem(util.math.MatrixStack var1, client.render.VertexConsumerProvider var2, int var3, float var4, float var5, minecraft.util.Arm var6);
}
