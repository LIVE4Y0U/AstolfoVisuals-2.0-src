package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(class_266.class)
public class ScoreboardObjectiveMixin {
   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   public void onGetDisplayName(CallbackInfoReturnable<class_2561> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((class_2561)cir.getReturnValue()));
   }
}
