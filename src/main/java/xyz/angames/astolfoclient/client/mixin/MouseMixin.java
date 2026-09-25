package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
   @Inject(method = "onMouseButton", at = @At("HEAD"))
   private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (action == 1 && client.currentScreen == null && AstolfoclientClient.moduleManager != null) {
         int mappedKey = -(button + 100);

         for (Module module : AstolfoclientClient.moduleManager.getModules()) {
            if (module.getKeyCode() == mappedKey) {
               module.toggle();
            }
         }
      }
   }
}
