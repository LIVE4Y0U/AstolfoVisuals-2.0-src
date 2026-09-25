package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;
import xyz.angames.astolfoclient.client.protection.NameLimiter;

@Environment(EnvType.CLIENT)
@Mixin(render.entity.EntityRenderer.class)
public abstract class EntityRendererMixin {
   @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
   private minecraft.text.Text onModifyLabel(minecraft.text.Text text) {
      return NameProtectModule.getProtectedText(NameLimiter.truncate(text));
   }
}
