package xyz.angames.astolfoclient.client.mixin;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.AmbientsModule;

@Environment(EnvType.CLIENT)
@Mixin(class_638.class)
public class ClientWorldMixin {
   @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
   private void onGetSkyColor(class_243 cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
      if (AstolfoclientClient.moduleManager != null) {
         AmbientsModule ambients = (AmbientsModule)AstolfoclientClient.moduleManager.getModuleByName("Ambients");
         if (ambients != null && ambients.isEnabled() && ambients.customSkybox.get()) {
            float strength = (float)ambients.skyboxStrength.get();
            if (strength > 0.0F) {
               Color themeColor = new Color(ThemeManager.getThemedColor(0L));
               int originalColor = (Integer)cir.getReturnValue();
               int origR = originalColor >> 16 & 0xFF;
               int origG = originalColor >> 8 & 0xFF;
               int origB = originalColor & 0xFF;
               int themeR = themeColor.getRed();
               int themeG = themeColor.getGreen();
               int themeB = themeColor.getBlue();
               int finalR = (int)(origR * (1.0F - strength) + themeR * strength);
               int finalG = (int)(origG * (1.0F - strength) + themeG * strength);
               int finalB = (int)(origB * (1.0F - strength) + themeB * strength);
               int finalColor = finalR << 16 | finalG << 8 | finalB;
               cir.setReturnValue(finalColor);
            }
         }
      }
   }
}
