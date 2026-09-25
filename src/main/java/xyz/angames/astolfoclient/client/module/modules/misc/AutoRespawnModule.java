package xyz.angames.astolfoclient.client.module.modules.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class AutoRespawnModule extends Module {
   public final NumberSetting delay = new NumberSetting("Delay (ms)", 0.0, 0.0, 1000.0, 50.0);
   private long deathTime = 0L;

   public AutoRespawnModule() {
      super("AutoRespawn", "Automatically respawns instantly upon death.", Module.Category.MISC);
      this.addSetting(this.delay);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (this.isEnabled()) {
            this.onTick(client);
         }
      });
   }

   private void onTick(MinecraftClient mc) {
      if (mc.player == null) {
         this.deathTime = 0L;
      } else {
         boolean isDead = mc.currentScreen instanceof DeathScreen || mc.player.isDead() || mc.player.getHealth() <= 0.0F;
         if (isDead) {
            long now = System.currentTimeMillis();
            if (this.deathTime == 0L) {
               this.deathTime = now;
            }

            if (now - this.deathTime >= (long)this.delay.get()) {
               mc.player.requestRespawn();
               if (mc.currentScreen instanceof DeathScreen) {
                  mc.setScreen(null);
               }

               this.deathTime = 0L;
            }
         } else {
            this.deathTime = 0L;
         }
      }
   }
}
