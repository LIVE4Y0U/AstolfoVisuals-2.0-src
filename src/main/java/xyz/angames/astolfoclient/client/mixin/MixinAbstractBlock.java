package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.block.AbstractBlock.class)
public class MixinAbstractBlock {
   @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
   private void onGetCollisionShape(minecraft.block.BlockState state, minecraft.world.BlockView world, util.math.BlockPos pos, minecraft.block.ShapeContext context, CallbackInfoReturnable<util.shape.VoxelShape> cir) {
      if (AstolfoclientClient.moduleManager != null) {
         Module noClip = AstolfoclientClient.moduleManager.getModuleByName("NoClip");
         if (noClip != null && noClip.isEnabled()) {
            cir.setReturnValue(util.shape.VoxelShapes.empty());
         }
      }
   }
}
