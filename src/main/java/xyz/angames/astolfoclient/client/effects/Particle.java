package xyz.angames.astolfoclient.client.effects;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import xyz.angames.astolfoclient.client.module.modules.render.ParticlesModule;

@Environment(EnvType.CLIENT)
public class Particle {
   public class_243 position;
   public class_243 prevPosition;
   public class_243 velocity;
   public final long creationTime = System.currentTimeMillis();
   public final float rotation;
   public final float scale;
   public final ParticlesModule.ParticleType type;
   public final boolean hasPhysics;
   public final long lifespan;
   public final Color color;
   public final Particle.ParticleAnimation animation;
   public final class_243 origin;
   public double extraData1;
   public double extraData2;

   public Particle(class_243 position, class_243 velocity, ParticlesModule.ParticleType type, boolean hasPhysics, long lifespan) {
      this(
         position,
         velocity,
         type,
         hasPhysics,
         lifespan,
         null,
         hasPhysics ? Particle.ParticleAnimation.EXPLOSION : Particle.ParticleAnimation.NONE,
         position,
         0.0,
         0.0
      );
   }

   public Particle(class_243 position, class_243 velocity, ParticlesModule.ParticleType type, boolean hasPhysics, long lifespan, Color color) {
      this(
         position,
         velocity,
         type,
         hasPhysics,
         lifespan,
         color,
         hasPhysics ? Particle.ParticleAnimation.EXPLOSION : Particle.ParticleAnimation.NONE,
         position,
         0.0,
         0.0
      );
   }

   public Particle(
      class_243 position,
      class_243 velocity,
      ParticlesModule.ParticleType type,
      boolean hasPhysics,
      long lifespan,
      Color color,
      Particle.ParticleAnimation animation,
      class_243 origin,
      double extraData1,
      double extraData2
   ) {
      this.position = position;
      this.prevPosition = position;
      this.velocity = velocity;
      this.type = type;
      this.hasPhysics = hasPhysics;
      this.lifespan = lifespan;
      this.color = color;
      this.animation = animation != null ? animation : Particle.ParticleAnimation.EXPLOSION;
      this.origin = origin != null ? origin : position;
      this.extraData1 = extraData1;
      this.extraData2 = extraData2;
      this.rotation = (float)(Math.random() * 360.0);
      this.scale = (float)(0.5 + Math.random() * 0.5);
   }

   @Environment(EnvType.CLIENT)
   public enum ParticleAnimation {
      EXPLOSION,
      SPHERE,
      SPIRAL,
      FOUNTAIN,
      SHOCKWAVE,
      NONE;
   }
}
