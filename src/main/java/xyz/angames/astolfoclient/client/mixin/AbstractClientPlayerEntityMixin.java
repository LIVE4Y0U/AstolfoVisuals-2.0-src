package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
   private static final Identifier CUSTOM_CAPE = Identifier.of("astolfoclient", "textures/cape.png");

   @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
   private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
      AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)this;
      MinecraftClient mc = MinecraftClient.getInstance();
      boolean isSelf = mc.player != null && player.getUuid().equals(mc.player.getUuid());
      boolean isFriend = player.getName() != null && FriendManager.isFriend(player.getName().getString())
         || player.getGameProfile() != null && FriendManager.isFriend(player.getGameProfile().getName());
      if (isSelf || isFriend) {
         SkinTextures original = (SkinTextures)cir.getReturnValue();
         if (original == null) {
            return;
         }

         SkinTextures customTextures = new SkinTextures(
            original.comp_1626(), original.comp_1911(), CUSTOM_CAPE, CUSTOM_CAPE, original.comp_1629(), original.comp_1630()
         );
         cir.setReturnValue(customTextures);
      }
   }
}
