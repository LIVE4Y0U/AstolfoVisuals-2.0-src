package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_310;
import net.minecraft.class_591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(class_591.class)
public abstract class PlayerEntityModelMixin {
   @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
   private void swellBabyHead(class_10055 state, CallbackInfo ci) {
      if (class_310.method_1551().field_1724 != null && state.field_53528 == class_310.method_1551().field_1724.method_5628()) {
         Module babyMod = AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer");
         class_591 model = (class_591)this;
         if (babyMod != null && babyMod.isEnabled()) {
            model.field_3398.field_37938 = 1.75F;
            model.field_3398.field_37939 = 1.75F;
            model.field_3398.field_37940 = 1.75F;
         } else {
            model.field_3398.field_37938 = 1.0F;
            model.field_3398.field_37939 = 1.0F;
            model.field_3398.field_37940 = 1.0F;
         }
      }
   }
}
