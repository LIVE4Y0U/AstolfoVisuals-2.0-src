package xyz.angames.astolfoclient.client.util;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@Environment(EnvType.CLIENT)
public class TargetUtils {
   public static minecraft.entity.LivingEntity getLookedAtTarget(minecraft.client.MinecraftClient client, double maxDistance) {
      if (client != null && client.player != null && client.world != null) {
         minecraft.entity.Entity cameraEntity = client.getCameraEntity();
         if (cameraEntity == null) {
            cameraEntity = client.player;
         }

         util.math.Vec3d start = cameraEntity.getCameraPosVec(1.0F);
         util.math.Vec3d rot = cameraEntity.getRotationVec(1.0F);
         util.math.Vec3d end = start.add(rot.multiply(maxDistance));
         util.hit.BlockHitResult blockHit = client.world.raycast(new minecraft.world.RaycastContext(start, end, world.RaycastContext.ShapeType.COLLIDER, world.RaycastContext.FluidHandling.NONE, cameraEntity));
         double effectiveDist = blockHit != null && blockHit.getType() != hit.HitResult.Type.MISS ? start.distanceTo(blockHit.getPos()) : maxDistance;
         util.math.Box searchBox = cameraEntity.getBoundingBox().stretch(rot.multiply(effectiveDist)).expand(1.0, 1.0, 1.0);
         double closestDist = effectiveDist;
         minecraft.entity.LivingEntity closestEntity = null;

         for (minecraft.entity.Entity entity : client.world
            .getOtherEntities(cameraEntity, searchBox, e -> e instanceof minecraft.entity.LivingEntity && !(e instanceof entity.decoration.ArmorStandEntity) && e.isAlive() && !e.isSpectator())) {
            if (!isInvisible(entity)) {
               float margin = entity.getTargetingMargin();
               util.math.Box entityBox = entity.getBoundingBox().expand(margin > 0.0F ? margin : 0.1);
               Optional<util.math.Vec3d> hit = entityBox.raycast(start, end);
               if (hit.isPresent()) {
                  double dist = start.distanceTo(hit.get());
                  if (dist < closestDist) {
                     closestDist = dist;
                     closestEntity = (minecraft.entity.LivingEntity)entity;
                  }
               }
            }
         }

         return closestEntity;
      } else {
         return null;
      }
   }

   public static boolean isInvisible(minecraft.entity.Entity entity) {
      if (entity == null) {
         return false;
      } else if (entity.isInvisible()) {
         return true;
      } else {
         return entity instanceof minecraft.entity.LivingEntity living ? living.hasStatusEffect(entity.effect.StatusEffects.INVISIBILITY) : false;
      }
   }
}
