package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(class_1676.class)
public class ProjectileEntityMixin {
   @Inject(method = "onEntityHit", at = @At("HEAD"))
   private void onEntityHit(class_3966 entityHitResult, CallbackInfo ci) {
      class_1676 self = (class_1676)this;
      if (self.method_24921() == class_310.method_1551().field_1724) {
         class_1297 target = entityHitResult.method_17782();
         if (AstolfoclientClient.killEffectManager != null) {
            Module mod = AstolfoclientClient.moduleManager.getModuleByName("KillEffect");
            if (mod != null && mod.isEnabled()) {
               AstolfoclientClient.killEffectManager.onAttack(target);
            }
         }
      }
   }
}
