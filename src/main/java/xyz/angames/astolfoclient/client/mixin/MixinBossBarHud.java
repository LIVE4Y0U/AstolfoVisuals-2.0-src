package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(gui.hud.BossBarHud.class)
public class MixinBossBarHud {
   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void onRenderBossBar(client.gui.DrawContext context, CallbackInfo ci) {
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.bossbar.get()) {
         ci.cancel();
      }
   }
}
