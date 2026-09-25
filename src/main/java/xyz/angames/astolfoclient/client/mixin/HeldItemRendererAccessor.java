package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1306;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(class_759.class)
public interface HeldItemRendererAccessor {
   @Invoker("renderArmHoldingItem")
   void invokeRenderArmHoldingItem(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);
}
