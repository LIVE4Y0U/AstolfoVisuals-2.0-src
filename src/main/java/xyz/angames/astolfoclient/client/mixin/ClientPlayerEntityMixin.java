package xyz.angames.astolfoclient.client.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(class_746.class)
public abstract class ClientPlayerEntityMixin extends class_742 {
   public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
      super(world, profile);
   }

   @Inject(method = "tickMovement", at = @At("HEAD"))
   public void onTickMovement(CallbackInfo ci) {
      class_746 player = (class_746)this;
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.blindness.get()) {
         boolean actuallyBlind = false;

         for (class_1293 effect : player.method_6026()) {
            if (effect.method_5579().equals(class_1294.field_5919)) {
               actuallyBlind = true;
               break;
            }
         }

         if (actuallyBlind) {
            player.method_5728(false);
            class_310.method_1551().field_1690.field_1867.method_23481(false);
         }
      }
   }
}
