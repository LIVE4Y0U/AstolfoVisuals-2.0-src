package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.render.AspectRatioModule;
import xyz.angames.astolfoclient.client.module.modules.render.CameraUtilsModule;

@Environment(EnvType.CLIENT)
@Mixin(class_757.class)
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private class_310 field_4015;
   @Shadow
   private float field_4005;
   @Shadow
   private float field_3988;
   @Shadow
   private float field_4004;

   @Shadow
   public abstract float method_32796();

   @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
   private void onGetFov(class_4184 camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
      CameraUtilsModule camUtils = CameraUtilsModule.getInstance();
      if (camUtils != null && camUtils.isEnabled()) {
         float baseFov = (Float)cir.getReturnValue();
         cir.setReturnValue((float)camUtils.getAnimatedFov(baseFov));
      }
   }

   @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
   private void onGetBasicProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
      AspectRatioModule aspectRatioModule = AspectRatioModule.getInstance();
      if (aspectRatioModule != null && aspectRatioModule.isEnabled()) {
         float defaultAspect = (float)this.field_4015.method_22683().method_4489() / this.field_4015.method_22683().method_4506();
         float aspect = aspectRatioModule.getAspectRatio(defaultAspect);
         Matrix4f matrix4f = new Matrix4f();
         if (this.field_4005 != 1.0F) {
            matrix4f.translate(this.field_3988, -this.field_4004, 0.0F);
            matrix4f.scale(this.field_4005, this.field_4005, 1.0F);
         }

         matrix4f.perspective(fov * (float) (Math.PI / 180.0), aspect, 0.05F, this.method_32796());
         cir.setReturnValue(matrix4f);
      }
   }
}
