package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(class_268.class)
public class ScoreboardTeamMixin {
   @Inject(method = "getPrefix", at = @At("RETURN"), cancellable = true)
   public void onGetPrefix(CallbackInfoReturnable<class_2561> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((class_2561)cir.getReturnValue()));
   }

   @Inject(method = "getSuffix", at = @At("RETURN"), cancellable = true)
   public void onGetSuffix(CallbackInfoReturnable<class_2561> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((class_2561)cir.getReturnValue()));
   }

   @Inject(method = "decorateName", at = @At("RETURN"), cancellable = true)
   public void onDecorateName(CallbackInfoReturnable<class_5250> cir) {
      class_2561 protectedText = NameProtectModule.getProtectedText((class_2561)cir.getReturnValue());
      if (protectedText != null) {
         cir.setReturnValue(protectedText.method_27661());
      }
   }
}
