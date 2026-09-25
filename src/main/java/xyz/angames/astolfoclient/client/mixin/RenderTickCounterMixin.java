package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9779.class_9781;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.util.TimerManager;

@Environment(EnvType.CLIENT)
@Mixin(class_9781.class)
public class RenderTickCounterMixin {
   @Shadow
   private float field_51964;
   @Shadow
   private long field_51962;
   @Shadow
   private float field_51958;
   @Shadow
   private float field_51959;

   @Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
   private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
      if (AstolfoclientClient.getInstance() != null) {
         float multiplier = TimerManager.getTimer();
         float elapsed = (float)(timeMillis - this.field_51962) / this.field_51964;
         this.field_51958 = elapsed * multiplier;
         this.field_51962 = timeMillis;
         this.field_51959 = this.field_51959 + this.field_51958;
         int i = (int)this.field_51959;
         this.field_51959 -= i;
         cir.setReturnValue(i);
      }
   }
}
