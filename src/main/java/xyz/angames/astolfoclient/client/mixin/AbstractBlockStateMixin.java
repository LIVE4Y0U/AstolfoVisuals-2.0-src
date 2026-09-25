package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2464;
import net.minecraft.class_2680;
import net.minecraft.class_4970.class_4971;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(class_4971.class)
public abstract class AbstractBlockStateMixin {
   @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
   private void onGetRenderType(CallbackInfoReturnable<class_2464> cir) {
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.grass.get() && NoRenderModule.isGrass((class_2680)this)) {
         cir.setReturnValue(class_2464.field_11455);
      }
   }
}
