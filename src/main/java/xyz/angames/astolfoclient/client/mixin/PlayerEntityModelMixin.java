package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(entity.model.PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {
   @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
   private void swellBabyHead(entity.state.PlayerEntityRenderState state, CallbackInfo ci) {
      if (minecraft.client.MinecraftClient.getInstance().player != null && state.id == minecraft.client.MinecraftClient.getInstance().player.getId()) {
         Module babyMod = AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer");
         entity.model.PlayerEntityModel model = (entity.model.PlayerEntityModel)this;
         if (babyMod != null && babyMod.isEnabled()) {
            model.head.xScale = 1.75F;
            model.head.yScale = 1.75F;
            model.head.zScale = 1.75F;
         } else {
            model.head.xScale = 1.0F;
            model.head.yScale = 1.0F;
            model.head.zScale = 1.0F;
         }
      }
   }
}
