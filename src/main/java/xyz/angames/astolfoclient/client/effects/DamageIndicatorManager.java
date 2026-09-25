package xyz.angames.astolfoclient.client.effects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.DamageIndicatorModule;

@Environment(EnvType.CLIENT)
public class DamageIndicatorManager {
   private final List<DamageIndicatorManager.DamageParticle> particles = new ArrayList<>();
   private final Map<Integer, Float> healthCache = new HashMap<>();
   private final DecimalFormat format = new DecimalFormat("#.#");

   public void tick() {
      class_310 client = class_310.method_1551();
      if (client.field_1687 != null && client.field_1724 != null) {
         DamageIndicatorModule module = (DamageIndicatorModule)AstolfoclientClient.moduleManager.getModuleByName("DamageIndicators");
         if (module != null && module.isEnabled()) {
            for (class_1297 entity : client.field_1687.method_18112()) {
               if (entity instanceof class_1309 living) {
                  int id = living.method_5628();
                  float currentHealth = living.method_6032() + living.method_6067();
                  if (this.healthCache.containsKey(id)) {
                     float previousHealth = this.healthCache.get(id);
                     float damageAmount = previousHealth - currentHealth;
                     if (damageAmount > 0.1F) {
                        this.spawnParticle(living, damageAmount);
                     }
                  }

                  this.healthCache.put(id, currentHealth);
               }
            }

            for (int i = this.particles.size() - 1; i >= 0; i--) {
               DamageIndicatorManager.DamageParticle p = this.particles.get(i);
               p.age++;
               p.x = p.x + p.vx;
               p.y = p.y + p.vy;
               p.z = p.z + p.vz;
               p.vx *= 0.8F;
               p.vy *= 0.8F;
               p.vz *= 0.8F;
               p.y += 0.015F;
               if (p.age >= p.maxAge) {
                  this.particles.remove(i);
               }
            }
         } else {
            this.healthCache.clear();
         }
      } else {
         this.particles.clear();
         this.healthCache.clear();
      }
   }

   private void spawnParticle(class_1309 target, float damage) {
      DamageIndicatorManager.DamageParticle p = new DamageIndicatorManager.DamageParticle();
      p.x = target.method_23317() + (Math.random() - 0.5) * 0.8;
      p.y = target.method_23318() + target.method_17682() * 0.5 + Math.random() * 0.5;
      p.z = target.method_23321() + (Math.random() - 0.5) * 0.8;
      double angle = Math.random() * Math.PI * 2.0;
      double speed = 0.15 + Math.random() * 0.1;
      p.vx = (float)(Math.cos(angle) * speed);
      p.vz = (float)(Math.sin(angle) * speed);
      p.vy = 0.15F + (float)(Math.random() * 0.1);
      p.text = "-" + this.format.format(damage);
      p.isCrit = damage > 6.0F;
      p.maxAge = p.isCrit ? 50 : 35;
      this.particles.add(p);
   }

   public List<DamageIndicatorManager.DamageParticle> getParticles() {
      return this.particles;
   }

   @Environment(EnvType.CLIENT)
   public static class DamageParticle {
      public String text;
      public double x;
      public double y;
      public double z;
      public float vx;
      public float vy;
      public float vz;
      public int age = 0;
      public int maxAge;
      public boolean isCrit;
   }
}
