package xyz.angames.astolfoclient.client.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(client.network.ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends client.network.AbstractClientPlayerEntity {
   public ClientPlayerEntityMixin(client.world.ClientWorld world, GameProfile profile) {
      super(world, profile);
   }

   @Inject(method = "tickMovement", at = @At("HEAD"))
   public void onTickMovement(CallbackInfo ci) {
      client.network.ClientPlayerEntity player = (client.network.ClientPlayerEntity)this;
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.blindness.get()) {
         boolean actuallyBlind = false;

         for (entity.effect.StatusEffectInstance effect : player.getStatusEffects()) {
            if (effect.getEffectType().equals(entity.effect.StatusEffects.BLINDNESS)) {
               actuallyBlind = true;
               break;
            }
         }

         if (actuallyBlind) {
            player.setSprinting(false);
            minecraft.client.MinecraftClient.getInstance().options.sprintKey.setPressed(false);
         }
      }
   }
}
