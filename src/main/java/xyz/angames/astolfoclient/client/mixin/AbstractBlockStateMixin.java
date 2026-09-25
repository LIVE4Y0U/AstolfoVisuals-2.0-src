package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(block.AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
   @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
   private void onGetRenderType(CallbackInfoReturnable<minecraft.block.BlockRenderType> cir) {
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.grass.get() && NoRenderModule.isGrass((minecraft.block.BlockState)this)) {
         cir.setReturnValue(minecraft.block.BlockRenderType.INVISIBLE);
      }
   }
}
