package xyz.angames.astolfoclient.client.mixin;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.render.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.ShaderHand;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.client.MinecraftClient.class)
public class MinecraftClientMixin {
   @Inject(method = "getResourcePackDir", at = @At("HEAD"), cancellable = true)
   private void onGetResourcePackDir(CallbackInfoReturnable<Path> cir) {
   }

   @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
   private void onGetFramebuffer(CallbackInfoReturnable<client.gl.Framebuffer> cir) {
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
         minecraft.client.MinecraftClient client = (minecraft.client.MinecraftClient)this;
         if (client.crosshairTarget instanceof util.hit.EntityHitResult hitResult) {
            minecraft.entity.Entity target = hitResult.getEntity();
            Module hitEspModule = AstolfoclientClient.moduleManager.getModuleByName("HitESP");
            if (hitEspModule != null && hitEspModule.isEnabled() && client.player != null) {
               client.render.Camera camera = client.gameRenderer.getCamera();
               Quaternionf orientation = new Quaternionf(camera.getRotation());
               util.math.Vec3d viewVec = client.player.getRotationVec(0.0F);
               util.math.Vec3d targetVec = target.getPos().subtract(client.player.getPos());
               float rotationDirection = (float)Math.signum(viewVec.crossProduct(targetVec).y);
               if (rotationDirection == 0.0F) {
                  rotationDirection = 1.0F;
               }

               util.math.Vec3d playerEyePos = client.player.getEyePos();
               util.math.Vec3d hitPos = hitResult.getPos();
               util.math.Vec3d direction = hitPos.subtract(playerEyePos).normalize();
               util.math.Vec3d offset = direction.multiply(0.3);
               util.math.Vec3d spawnPos = hitPos.subtract(offset);
               if (AstolfoclientClient.hitEspManager != null) {
                  AstolfoclientClient.hitEspManager.addEffect(spawnPos, rotationDirection, orientation);
               }
            }
         }
      }
   }
}
