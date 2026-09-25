package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_6379;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.util.IASAccountHelper;

@Environment(EnvType.CLIENT)
@Mixin(class_437.class)
public abstract class ScreenMixin {
   @Shadow
   protected abstract <T extends class_364 & class_4068 & class_6379> T method_37063(T var1);

   @Inject(method = "init", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      class_437 screen = (class_437)this;
      IASAccountHelper.onScreenInit(screen, this::method_37063);
   }
}
