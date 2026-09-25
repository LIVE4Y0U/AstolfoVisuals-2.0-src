package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(client.sound.SoundSystem.class)
public class SoundSystemMixin {
   @Shadow
   @Final
   private client.option.GameOptions settings;

   @Inject(method = "getAdjustedVolume", at = @At("HEAD"), cancellable = true)
   private void onGetAdjustedVolume(client.sound.SoundInstance sound, CallbackInfoReturnable<Float> cir) {
      if (sound != null && sound.getId() != null && "astolfoclient".equals(sound.getId().getNamespace())) {
         float mcMaster = this.settings != null ? this.settings.getSoundVolume(minecraft.sound.SoundCategory.MASTER) : 1.0F;
         float clientVol = sound.getVolume();
         if (mcMaster > 0.001F) {
            cir.setReturnValue(clientVol / mcMaster);
         } else {
            cir.setReturnValue(clientVol);
         }
      }
   }
}
