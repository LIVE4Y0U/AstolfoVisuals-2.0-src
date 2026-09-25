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
@Mixin(client.network.AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
   private static final minecraft.util.Identifier CUSTOM_CAPE = minecraft.util.Identifier.of("astolfoclient", "textures/cape.png");

   @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
   private void onGetSkinTextures(CallbackInfoReturnable<client.util.SkinTextures> cir) {
      client.network.AbstractClientPlayerEntity player = (client.network.AbstractClientPlayerEntity)this;
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      boolean isSelf = mc.player != null && player.getUuid().equals(mc.player.getUuid());
      boolean isFriend = player.getName() != null && FriendManager.isFriend(player.getName().getString())
         || player.getGameProfile() != null && FriendManager.isFriend(player.getGameProfile().getName());
      if (isSelf || isFriend) {
         client.util.SkinTextures original = (client.util.SkinTextures)cir.getReturnValue();
         if (original == null) {
            return;
         }

         client.util.SkinTextures customTextures = new client.util.SkinTextures(
            original.comp_1626(), original.comp_1911(), CUSTOM_CAPE, CUSTOM_CAPE, original.comp_1629(), original.comp_1630()
         );
         cir.setReturnValue(customTextures);
      }
   }
}
