package xyz.angames.astolfoclient.client.module.modules.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.class_310;
import net.minecraft.class_418;
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

   private void onTick(class_310 mc) {
      if (mc.field_1724 == null) {
         this.deathTime = 0L;
      } else {
         boolean isDead = mc.field_1755 instanceof class_418 || mc.field_1724.method_29504() || mc.field_1724.method_6032() <= 0.0F;
         if (isDead) {
            long now = System.currentTimeMillis();
            if (this.deathTime == 0L) {
               this.deathTime = now;
            }

            if (now - this.deathTime >= (long)this.delay.get()) {
               mc.field_1724.method_7331();
               if (mc.field_1755 instanceof class_418) {
                  mc.method_1507(null);
               }

               this.deathTime = 0L;
            }
         } else {
            this.deathTime = 0L;
         }
      }
   }
}
