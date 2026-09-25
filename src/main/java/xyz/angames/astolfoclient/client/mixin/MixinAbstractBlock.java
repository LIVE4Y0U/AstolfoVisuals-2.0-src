package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3726;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(class_4970.class)
public class MixinAbstractBlock {
   @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
   private void onGetCollisionShape(class_2680 state, class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
      if (AstolfoclientClient.moduleManager != null) {
         Module noClip = AstolfoclientClient.moduleManager.getModuleByName("NoClip");
         if (noClip != null && noClip.isEnabled()) {
            cir.setReturnValue(class_259.method_1073());
         }
      }
   }
}
