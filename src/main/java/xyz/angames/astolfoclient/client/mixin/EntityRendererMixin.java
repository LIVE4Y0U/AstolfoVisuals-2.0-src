package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;
import xyz.angames.astolfoclient.client.protection.NameLimiter;

@Environment(EnvType.CLIENT)
@Mixin(class_897.class)
public abstract class EntityRendererMixin {
   @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
   private class_2561 onModifyLabel(class_2561 text) {
      return NameProtectModule.getProtectedText(NameLimiter.truncate(text));
   }
}
