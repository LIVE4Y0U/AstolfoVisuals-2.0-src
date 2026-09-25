package xyz.angames.astolfoclient.client.mixin;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.ShaderHand;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public class MinecraftClientMixin {
   @Inject(method = "getResourcePackDir", at = @At("HEAD"), cancellable = true)
   private void onGetResourcePackDir(CallbackInfoReturnable<Path> cir) {
   }

   @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
   private void onGetFramebuffer(CallbackInfoReturnable<class_276> cir) {
      if (ShaderHand.rendering) {
         ShaderHand mod = ShaderHand.getInstance();
         if (mod != null && mod.getHandsBuffer() != null) {
            cir.setReturnValue(mod.getHandsBuffer());
         }
      }
   }

   @Inject(method = "doAttack", at = @At("HEAD"))
   private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
      if (AstolfoclientClient.moduleManager != null) {
         class_310 client = (class_310)this;
         if (client.field_1765 instanceof class_3966 hitResult) {
            class_1297 target = hitResult.method_17782();
            Module hitEspModule = AstolfoclientClient.moduleManager.getModuleByName("HitESP");
            if (hitEspModule != null && hitEspModule.isEnabled() && client.field_1724 != null) {
               class_4184 camera = client.field_1773.method_19418();
               Quaternionf orientation = new Quaternionf(camera.method_23767());
               class_243 viewVec = client.field_1724.method_5828(0.0F);
               class_243 targetVec = target.method_19538().method_1020(client.field_1724.method_19538());
               float rotationDirection = (float)Math.signum(viewVec.method_1036(targetVec).field_1351);
               if (rotationDirection == 0.0F) {
                  rotationDirection = 1.0F;
               }

               class_243 playerEyePos = client.field_1724.method_33571();
               class_243 hitPos = hitResult.method_17784();
               class_243 direction = hitPos.method_1020(playerEyePos).method_1029();
               class_243 offset = direction.method_1021(0.3);
               class_243 spawnPos = hitPos.method_1020(offset);
               if (AstolfoclientClient.hitEspManager != null) {
                  AstolfoclientClient.hitEspManager.addEffect(spawnPos, rotationDirection, orientation);
               }
            }
         }
      }
   }
}
