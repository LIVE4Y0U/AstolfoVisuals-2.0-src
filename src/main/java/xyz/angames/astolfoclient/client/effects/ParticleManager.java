package xyz.angames.astolfoclient.client.effects;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1665;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.ParticlesModule;

@Environment(EnvType.CLIENT)
public class ParticleManager {
   private final List<Particle> particles = new CopyOnWriteArrayList<>();
   private final Random random = new Random();
   private static final double GRAVITY = 0.015;
   private static final double EXPLOSION_STRENGTH = 0.3;
   private static final double BOUNCE_FACTOR = 0.6;
   private static final double FRICTION = 0.98;
   private static final Color[] VANILLA_TOTEM_COLORS = new Color[]{
      new Color(130, 255, 60), new Color(60, 230, 80), new Color(40, 180, 70), new Color(255, 255, 140), new Color(255, 180, 30)
   };

   public void addEffects(class_243 origin) {
      ParticlesModule mod = (ParticlesModule)AstolfoclientClient.moduleManager.getModuleByName("Particles");
      if (mod != null && mod.isEnabled() && mod.hits.get()) {
         int amount = mod.amount.getInt();
         long lifespan = mod.lifespan.getInt();

         for (int i = 0; i < amount; i++) {
            double velX = (this.random.nextDouble() - 0.5) * 0.3;
            double velY = this.random.nextDouble() * 0.5 * 0.3;
            double velZ = (this.random.nextDouble() - 0.5) * 0.3;
            this.particles.add(new Particle(origin, new class_243(velX, velY, velZ), mod.type.getValue(), true, lifespan));
         }
      }
   }

   public void addTotemPop(class_243 origin) {
      ParticlesModule mod = (ParticlesModule)AstolfoclientClient.moduleManager.getModuleByName("Particles");
      if (mod != null && mod.isEnabled() && mod.totemPop.get()) {
         int amount = mod.totemAmount.getInt();
         long lifespan = mod.totemLifespan.getInt();
         ParticlesModule.ParticleType pType = mod.totemType.getValue();
         String animMode = mod.totemAnimation.get();
         String colorMode = mod.totemColor.get();
         boolean physics = mod.totemPhysics.get();

         for (int i = 0; i < amount; i++) {
            Color color = this.getTotemParticleColor(colorMode);
            double extra1 = 0.0;
            double extra2 = 0.0;
            Particle.ParticleAnimation anim;
            class_243 vel;
            switch (animMode) {
               case "Sphere": {
                  anim = Particle.ParticleAnimation.SPHERE;
                  double phi = Math.acos(1.0 - 2.0 * (i + 0.5) / amount);
                  double theta = Math.PI * (1.0 + Math.sqrt(5.0)) * i;
                  double speed = 0.22 + this.random.nextDouble() * 0.12;
                  double vx = Math.sin(phi) * Math.cos(theta) * speed;
                  double vy = Math.cos(phi) * speed;
                  double vz = Math.sin(phi) * Math.sin(theta) * speed;
                  vel = new class_243(vx, vy, vz);
                  break;
               }
               case "Spiral": {
                  anim = Particle.ParticleAnimation.SPIRAL;
                  double angle = (Math.PI * 2) / Math.max(1, amount) * i * 2.5 + this.random.nextDouble() * 0.4;
                  double radius = 0.15 + this.random.nextDouble() * 0.25;
                  double upward = 0.08 + this.random.nextDouble() * 0.18;
                  vel = new class_243(Math.cos(angle) * 0.1, upward, Math.sin(angle) * 0.1);
                  extra1 = angle;
                  extra2 = radius;
                  break;
               }
               case "Fountain": {
                  anim = Particle.ParticleAnimation.FOUNTAIN;
                  double angle = this.random.nextDouble() * Math.PI * 2.0;
                  double spread = 0.08 + this.random.nextDouble() * 0.16;
                  double vx = Math.cos(angle) * spread;
                  double vy = 0.32 + this.random.nextDouble() * 0.26;
                  double vz = Math.sin(angle) * spread;
                  vel = new class_243(vx, vy, vz);
                  break;
               }
               case "Shockwave": {
                  anim = Particle.ParticleAnimation.SHOCKWAVE;
                  double angle = (Math.PI * 2) / Math.max(1, amount) * i + (this.random.nextDouble() - 0.5) * 0.1;
                  double speed = 0.28 + this.random.nextDouble() * 0.12;
                  double vx = Math.cos(angle) * speed;
                  double vy = (this.random.nextDouble() - 0.5) * 0.05;
                  double vz = Math.sin(angle) * speed;
                  vel = new class_243(vx, vy, vz);
                  extra1 = angle;
                  break;
               }
               case "Explosion": {
                  anim = Particle.ParticleAnimation.EXPLOSION;
                  double velX = (this.random.nextDouble() - 0.5) * 0.45;
                  double velY = this.random.nextDouble() * 0.45 + 0.05;
                  double velZ = (this.random.nextDouble() - 0.5) * 0.45;
                  vel = new class_243(velX, velY, velZ);
                  break;
               }
               default: {
                  anim = Particle.ParticleAnimation.EXPLOSION;
                  double velX = (this.random.nextDouble() - 0.5) * 0.45;
                  double velY = this.random.nextDouble() * 0.45 + 0.05;
                  double velZ = (this.random.nextDouble() - 0.5) * 0.45;
                  vel = new class_243(velX, velY, velZ);
               }
            }

            this.particles.add(new Particle(origin, vel, pType, physics, lifespan, color, anim, origin, extra1, extra2));
         }
      }
   }

   private Color getTotemParticleColor(String colorMode) {
      return switch (colorMode) {
         case "Vanilla" -> VANILLA_TOTEM_COLORS[this.random.nextInt(VANILLA_TOTEM_COLORS.length)];
         case "Lime" -> new Color(130, 255, 60);
         case "Green" -> new Color(60, 230, 80);
         case "Dark Green" -> new Color(40, 180, 70);
         case "Light Yellow" -> new Color(255, 255, 140);
         case "Dark Yellow" -> new Color(255, 180, 30);
         case "Theme" -> null;
         default -> VANILLA_TOTEM_COLORS[this.random.nextInt(VANILLA_TOTEM_COLORS.length)];
      };
   }

   public void addTrail(class_243 origin, ParticlesModule.ParticleType type, int amount, long lifespan) {
      for (int i = 0; i < amount; i++) {
         double velX = (this.random.nextDouble() - 0.5) * 0.02;
         double velY = (this.random.nextDouble() - 0.5) * 0.02;
         double velZ = (this.random.nextDouble() - 0.5) * 0.02;
         this.particles.add(new Particle(origin, new class_243(velX, velY, velZ), type, false, lifespan));
      }
   }

   public void tick() {
      ParticlesModule mod = (ParticlesModule)AstolfoclientClient.moduleManager.getModuleByName("Particles");
      if (mod != null) {
         class_310 client = class_310.method_1551();
         if (client.field_1687 != null && client.field_1724 != null) {
            if (mod.isEnabled()) {
               if (mod.walk.get() && client.field_1724.method_24828() && client.field_1724.method_18798().method_1027() > 0.01 && this.random.nextInt(3) == 0) {
                  class_243 footPos = client.field_1724
                     .method_19538()
                     .method_1031((this.random.nextDouble() - 0.5) * 0.5, this.random.nextDouble() * 0.2, (this.random.nextDouble() - 0.5) * 0.5);
                  this.addTrail(footPos, mod.walkType.getValue(), mod.walkAmount.getInt(), mod.walkLifespan.getInt());
               }

               for (class_1297 entity : client.field_1687.method_18112()) {
                  if (entity.method_18798().method_1027() > 0.01) {
                     if (mod.arrows.get() && entity instanceof class_1665 && !(entity instanceof class_1685)) {
                        this.addTrail(
                           entity.method_19538().method_1031(0.0, entity.method_17682() / 2.0F, 0.0),
                           mod.arrowType.getValue(),
                           mod.arrowAmount.getInt(),
                           mod.arrowLifespan.getInt()
                        );
                     } else if (mod.pearls.get() && entity instanceof class_1684) {
                        this.addTrail(
                           entity.method_19538().method_1031(0.0, entity.method_17682() / 2.0F, 0.0),
                           mod.pearlType.getValue(),
                           mod.pearlAmount.getInt(),
                           mod.pearlLifespan.getInt()
                        );
                     } else if (mod.tridents.get() && entity instanceof class_1685) {
                        this.addTrail(
                           entity.method_19538().method_1031(0.0, entity.method_17682() / 2.0F, 0.0),
                           mod.tridentType.getValue(),
                           mod.tridentAmount.getInt(),
                           mod.tridentLifespan.getInt()
                        );
                     } else if (mod.items.get() && entity instanceof class_1542 && this.random.nextInt(2) == 0) {
                        this.addTrail(
                           entity.method_19538().method_1031(0.0, entity.method_17682() / 2.0F, 0.0),
                           mod.itemType.getValue(),
                           mod.itemAmount.getInt(),
                           mod.itemLifespan.getInt()
                        );
                     }
                  }
               }
            }

            long currentTime = System.currentTimeMillis();
            this.particles.removeIf(p -> {
               if (currentTime - p.creationTime > p.lifespan) {
                  return true;
               }

               p.prevPosition = p.position;
               switch (p.animation) {
                  case SPHERE:
                     p.velocity = p.velocity.method_1021(0.95);
                     if (p.hasPhysics) {
                        this.handlePhysics(client, p);
                     } else {
                        p.position = p.position.method_1019(p.velocity);
                     }
                     break;
                  case SPIRAL:
                     p.extraData1 += 0.18;
                     p.extraData2 += 0.015;
                     double sx = p.origin.field_1352 + Math.cos(p.extraData1) * p.extraData2;
                     double sz = p.origin.field_1350 + Math.sin(p.extraData1) * p.extraData2;
                     p.velocity = p.velocity.method_1021(0.97);
                     p.position = new class_243(sx, p.position.field_1351 + p.velocity.field_1351, sz);
                     break;
                  case SHOCKWAVE:
                     p.velocity = p.velocity.method_1021(0.95);
                     if (p.hasPhysics) {
                        this.handlePhysics(client, p);
                     } else {
                        p.position = p.position.method_1019(p.velocity);
                     }
                     break;
                  case FOUNTAIN:
                     p.velocity = p.velocity.method_1031(0.0, -0.015, 0.0);
                     p.velocity = p.velocity.method_18805(0.98, 1.0, 0.98);
                     if (p.hasPhysics) {
                        this.handlePhysics(client, p);
                     } else {
                        p.position = p.position.method_1019(p.velocity);
                     }
                     break;
                  case EXPLOSION:
                  case NONE:
                     if (p.hasPhysics) {
                        p.velocity = p.velocity.method_1031(0.0, -0.015, 0.0);
                        p.velocity = p.velocity.method_18805(0.98, 1.0, 0.98);
                        this.handlePhysics(client, p);
                     } else {
                        p.position = p.position.method_1019(p.velocity);
                     }
                     break;
                  default:
                     if (p.hasPhysics) {
                        p.velocity = p.velocity.method_1031(0.0, -0.015, 0.0);
                        p.velocity = p.velocity.method_18805(0.98, 1.0, 0.98);
                        this.handlePhysics(client, p);
                     } else {
                        p.position = p.position.method_1019(p.velocity);
                     }
               }

               return false;
            });
         } else {
            this.particles.clear();
         }
      }
   }

   private boolean shouldCollide(class_310 client, class_2338 pos) {
      if (client.field_1687 == null) {
         return false;
      } else {
         class_2680 state = client.field_1687.method_8320(pos);
         if (state.method_26215()) {
            return false;
         } else if (state.method_51176()) {
            return false;
         } else if (!state.method_27852(class_2246.field_31037) && !state.method_27852(class_2246.field_10369)) {
            class_265 collision = state.method_26220(client.field_1687, pos);
            return !collision.method_1110();
         } else {
            return false;
         }
      }
   }

   private void handlePhysics(class_310 client, Particle p) {
      if (client.field_1687 == null) {
         p.position = p.position.method_1019(p.velocity);
      } else {
         double dx = p.velocity.field_1352;
         double dy = p.velocity.field_1351;
         double dz = p.velocity.field_1350;
         double currX = p.position.field_1352;
         double currY = p.position.field_1351;
         double currZ = p.position.field_1350;
         if (this.shouldCollide(client, class_2338.method_49637(currX, currY + dy, currZ))) {
            p.velocity = new class_243(p.velocity.field_1352, -dy * 0.6, p.velocity.field_1350);
         }

         if (this.shouldCollide(client, class_2338.method_49637(currX + dx, currY, currZ))) {
            p.velocity = new class_243(-dx * 0.6, p.velocity.field_1351, p.velocity.field_1350);
         }

         if (this.shouldCollide(client, class_2338.method_49637(currX, currY, currZ + dz))) {
            p.velocity = new class_243(p.velocity.field_1352, p.velocity.field_1351, -dz * 0.6);
         }

         p.position = p.position.method_1019(p.velocity);
      }
   }

   public List<Particle> getParticles() {
      return this.particles;
   }
}
