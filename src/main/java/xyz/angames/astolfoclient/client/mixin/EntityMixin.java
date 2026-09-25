package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(class_1297.class)
public abstract class EntityMixin {
   @Inject(method = "getCustomName", at = @At("RETURN"), cancellable = true)
   private void onGetCustomName(CallbackInfoReturnable<class_2561> cir) {
      class_2561 original = (class_2561)cir.getReturnValue();
      if (original != null) {
         class_2561 protectedText = NameProtectModule.getProtectedText(original);
         if (protectedText != null && protectedText != original) {
            cir.setReturnValue(protectedText);
         }
      }
   }
}
