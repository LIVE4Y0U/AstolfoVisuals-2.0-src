package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.util.TimerManager;

@Environment(EnvType.CLIENT)
@Mixin(RenderTickCounter.Dynamic.class)
public class RenderTickCounterMixin {
   @Shadow
   private float tickTime;
   @Shadow
   private long prevTimeMillis;
   @Shadow
   private float lastFrameDuration;
   @Shadow
   private float tickDelta;

   @Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
   private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
      if (AstolfoclientClient.getInstance() != null) {
         float multiplier = TimerManager.getTimer();
         float elapsed = (float)(timeMillis - this.prevTimeMillis) / this.tickTime;
         this.lastFrameDuration = elapsed * multiplier;
         this.prevTimeMillis = timeMillis;
         this.tickDelta = this.tickDelta + this.lastFrameDuration;
         int i = (int)this.tickDelta;
         this.tickDelta -= i;
         cir.setReturnValue(i);
      }
   }
}
