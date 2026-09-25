package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(class_355.class)
public class PlayerListHudMixin {
   @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
   public void onGetPlayerName(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
      class_2561 originalText = (class_2561)cir.getReturnValue();
      if (originalText != null) {
         cir.setReturnValue(NameProtectModule.getProtectedText(originalText));
      } else {
         String originalStr = entry.method_2966().getName();
         String protectedStr = NameProtectModule.getProtectedName(originalStr);
         if (!originalStr.equals(protectedStr)) {
            cir.setReturnValue(class_2561.method_43470(protectedStr));
         }
      }
   }
}
