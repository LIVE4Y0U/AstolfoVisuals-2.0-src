package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.entity.Entity.class)
public abstract class EntityMixin {
   @Inject(method = "getCustomName", at = @At("RETURN"), cancellable = true)
   private void onGetCustomName(CallbackInfoReturnable<minecraft.text.Text> cir) {
      minecraft.text.Text original = (minecraft.text.Text)cir.getReturnValue();
      if (original != null) {
         minecraft.text.Text protectedText = NameProtectModule.getProtectedText(original);
         if (protectedText != null && protectedText != original) {
            cir.setReturnValue(protectedText);
         }
      }
   }
}
