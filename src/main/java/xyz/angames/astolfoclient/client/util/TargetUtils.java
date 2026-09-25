package xyz.angames.astolfoclient.client.util;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public class TargetUtils {
   public static class_1309 getLookedAtTarget(class_310 client, double maxDistance) {
      if (client != null && client.field_1724 != null && client.field_1687 != null) {
         class_1297 cameraEntity = client.method_1560();
         if (cameraEntity == null) {
            cameraEntity = client.field_1724;
         }

         class_243 start = cameraEntity.method_5836(1.0F);
         class_243 rot = cameraEntity.method_5828(1.0F);
         class_243 end = start.method_1019(rot.method_1021(maxDistance));
         class_3965 blockHit = client.field_1687.method_17742(new class_3959(start, end, class_3960.field_17558, class_242.field_1348, cameraEntity));
         double effectiveDist = blockHit != null && blockHit.method_17783() != class_240.field_1333 ? start.method_1022(blockHit.method_17784()) : maxDistance;
         class_238 searchBox = cameraEntity.method_5829().method_18804(rot.method_1021(effectiveDist)).method_1009(1.0, 1.0, 1.0);
         double closestDist = effectiveDist;
         class_1309 closestEntity = null;

         for (class_1297 entity : client.field_1687
            .method_8333(cameraEntity, searchBox, e -> e instanceof class_1309 && !(e instanceof class_1531) && e.method_5805() && !e.method_7325())) {
            if (!isInvisible(entity)) {
               float margin = entity.method_5871();
               class_238 entityBox = entity.method_5829().method_1014(margin > 0.0F ? margin : 0.1);
               Optional<class_243> hit = entityBox.method_992(start, end);
               if (hit.isPresent()) {
                  double dist = start.method_1022(hit.get());
                  if (dist < closestDist) {
                     closestDist = dist;
                     closestEntity = (class_1309)entity;
                  }
               }
            }
         }

         return closestEntity;
      } else {
         return null;
      }
   }

   public static boolean isInvisible(class_1297 entity) {
      if (entity == null) {
         return false;
      } else if (entity.method_5767()) {
         return true;
      } else {
         return entity instanceof class_1309 living ? living.method_6059(class_1294.field_5905) : false;
      }
   }
}
