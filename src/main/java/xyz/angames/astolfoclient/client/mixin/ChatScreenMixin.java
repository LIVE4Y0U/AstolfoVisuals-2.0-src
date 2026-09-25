package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.modules.misc.PasswordHiderModule;

@Environment(EnvType.CLIENT)
@Mixin(class_408.class)
public class ChatScreenMixin {
   @Shadow
   protected class_342 field_2382;

   @Inject(method = "init", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      if (this.field_2382 != null) {
         PasswordHiderModule.setupChatField(this.field_2382);
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.field_2382 != null) {
         PasswordHiderModule.renderChatFieldOverlay(context, this.field_2382);
      }
   }
}
