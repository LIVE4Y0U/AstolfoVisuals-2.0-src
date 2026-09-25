package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import net.minecraft.class_315;
import net.minecraft.class_3419;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1140.class)
public class SoundSystemMixin {
   @Shadow
   @Final
   private class_315 field_5555;

   @Inject(method = "getAdjustedVolume", at = @At("HEAD"), cancellable = true)
   private void onGetAdjustedVolume(class_1113 sound, CallbackInfoReturnable<Float> cir) {
      if (sound != null && sound.method_4775() != null && "astolfoclient".equals(sound.method_4775().method_12836())) {
         float mcMaster = this.field_5555 != null ? this.field_5555.method_1630(class_3419.field_15250) : 1.0F;
         float clientVol = sound.method_4781();
         if (mcMaster > 0.001F) {
            cir.setReturnValue(clientVol / mcMaster);
         } else {
            cir.setReturnValue(clientVol);
         }
      }
   }
}
