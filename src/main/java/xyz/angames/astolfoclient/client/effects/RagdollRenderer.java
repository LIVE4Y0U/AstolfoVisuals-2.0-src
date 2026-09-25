package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_5944;
import net.minecraft.class_638;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class RagdollRenderer {
   private final List<RagdollRenderer.Ragdoll> ragdolls = new ArrayList<>();
   private class_638 lastWorld = null;

   public void addRagdoll(class_1309 entity) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && entity != null) {
         try {
            class_898 dispatcher = mc.method_1561();
            class_897<?, ?> renderer = dispatcher.method_3953(entity);
            if (renderer == null) {
               return;
            }

            class_10042 stateCopy = (class_10042)renderer.method_55269();

            try {
               renderer.method_62354(entity, stateCopy, 1.0F);
            } catch (Exception e) {
               e.printStackTrace();
            }

            RagdollRenderer.SnapshotVertexConsumer snapshotConsumer = new RagdollRenderer.SnapshotVertexConsumer();
            class_4597 captureProvider = layer -> snapshotConsumer;
            class_4587 matrices = new class_4587();

            try {
               renderer.method_3936(stateCopy, matrices, captureProvider, 15728880);
            } catch (Exception e) {
               e.printStackTrace();
            }

            snapshotConsumer.commitLast();
            if (!snapshotConsumer.vertices.isEmpty()) {
               double ex = entity.method_23317();
               double ey = entity.method_23318();
               double ez = entity.method_23321();
               float height = entity.method_17682();
               synchronized (this.ragdolls) {
                  this.ragdolls.add(new RagdollRenderer.Ragdoll(snapshotConsumer.vertices, ex, ey, ez, height));
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public static class_243 rotateAroundAxis(class_243 point, class_243 axis, double angle) {
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double dot = point.field_1352 * axis.field_1352 + point.field_1351 * axis.field_1351 + point.field_1350 * axis.field_1350;
      double crossX = axis.field_1351 * point.field_1350 - axis.field_1350 * point.field_1351;
      double crossY = axis.field_1350 * point.field_1352 - axis.field_1352 * point.field_1350;
      double crossZ = axis.field_1352 * point.field_1351 - axis.field_1351 * point.field_1352;
      double rx = point.field_1352 * cos + crossX * sin + axis.field_1352 * dot * (1.0 - cos);
      double ry = point.field_1351 * cos + crossY * sin + axis.field_1351 * dot * (1.0 - cos);
      double rz = point.field_1350 * cos + crossZ * sin + axis.field_1350 * dot * (1.0 - cos);
      return new class_243(rx, ry, rz);
   }

   public void render(WorldRenderContext context) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null) {
         synchronized (this.ragdolls) {
            this.ragdolls.clear();
         }

         this.lastWorld = null;
      } else {
         if (mc.field_1687 != this.lastWorld) {
            synchronized (this.ragdolls) {
               this.ragdolls.clear();
            }

            this.lastWorld = mc.field_1687;
         }

         Module ragdollModule = AstolfoclientClient.moduleManager.getModuleByName("Ragdoll");
         if (ragdollModule != null && ragdollModule.isEnabled()) {
            long now = System.currentTimeMillis();
            List<RagdollRenderer.Ragdoll> active = new ArrayList<>();
            synchronized (this.ragdolls) {
               Iterator<RagdollRenderer.Ragdoll> it = this.ragdolls.iterator();

               while (it.hasNext()) {
                  RagdollRenderer.Ragdoll r = it.next();
                  if (now - r.spawnTime > 1500L) {
                     it.remove();
                  } else {
                     active.add(r);
                  }
               }
            }

            if (!active.isEmpty()) {
               class_243 cameraPos = context.camera().method_19326();
               class_4587 matrices = context.matrixStack();
               class_289 tessellator = class_289.method_1348();
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.enableCull();
               matrices.method_22903();
               matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
               class_287[] bufferHolder = new class_287[]{tessellator.method_60827(class_5596.field_27382, class_290.field_1575)};
               Matrix4f m = matrices.method_23760().method_23761();

               for (RagdollRenderer.Ragdoll ragdoll : active) {
                  long age = now - ragdoll.spawnTime;
                  float explosionProgress = 0.0F;
                  float explosionAgeSecs = 0.0F;
                  if (age >= 800L) {
                     explosionProgress = (float)(age - 800L) / 700.0F;
                     explosionAgeSecs = (float)(age - 800L) / 1000.0F;
                  }

                  if (explosionProgress > 1.0F) {
                     explosionProgress = 1.0F;
                  }

                  float progress = (float)age / 1500.0F;
                  float alphaVal = 0.5F * (1.0F - progress);

                  for (RagdollRenderer.Ragdoll.Shard shard : ragdoll.shards) {
                     for (int j = 0; j < 4; j++) {
                        RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v = shard.vertices[j];
                        class_243 distortionOffset = shard.vertexDistortions[j].method_1021(explosionProgress);
                        double vxLocal = v.x + distortionOffset.field_1352;
                        double vyLocal = v.y + distortionOffset.field_1351;
                        double vzLocal = v.z + distortionOffset.field_1350;
                        class_243 localToCenter = new class_243(
                           vxLocal - shard.center.field_1352, vyLocal - shard.center.field_1351, vzLocal - shard.center.field_1350
                        );
                        class_243 rotated = rotateAroundAxis(localToCenter, shard.rotationAxis, shard.rotationSpeed * explosionAgeSecs);
                        class_243 rotatedScaled = rotated.method_1021(1.0 - explosionProgress);
                        class_243 finalLocalPos = shard.center.method_1019(rotatedScaled).method_1019(shard.velocity.method_1021(explosionAgeSecs));
                        double vx = ragdoll.x + finalLocalPos.field_1352;
                        double vy = ragdoll.y + finalLocalPos.field_1351;
                        double vz = ragdoll.z + finalLocalPos.field_1350;
                        bufferHolder[0].method_22918(m, (float)vx, (float)vy, (float)vz).method_22913(v.u, v.v).method_22915(v.r, v.g, v.b, alphaVal);
                     }
                  }

                  class_5944 shader = RenderSystem.setShader(AstolfoclientClient.COSMOS_FILL_SHADER);
                  if (shader != null) {
                     float timeSecs = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
                     if (shader.method_34582("uTime") != null) {
                        shader.method_34582("uTime").method_1251(timeSecs);
                     }

                     if (shader.method_34582("uResolution") != null) {
                        shader.method_34582("uResolution").method_1255(1.0F, 1.0F);
                     }

                     int color1 = ThemeManager.getThemedColor(0L);
                     float r1 = (color1 >> 16 & 0xFF) / 255.0F;
                     float g1 = (color1 >> 8 & 0xFF) / 255.0F;
                     float b1 = (color1 & 0xFF) / 255.0F;
                     if (shader.method_34582("uColor1") != null) {
                        shader.method_34582("uColor1").method_1249(r1, g1, b1);
                     }

                     if (shader.method_34582("uColor2") != null) {
                        shader.method_34582("uColor2").method_1249(0.0F, 0.0F, 0.0F);
                     }

                     if (shader.method_34582("uBlockCenter") != null) {
                        shader.method_34582("uBlockCenter").method_1249((float)ragdoll.x, (float)ragdoll.y, (float)ragdoll.z);
                     }

                     if (shader.method_34582("ColorModulator") != null) {
                        shader.method_34582("ColorModulator").method_35657(1.0F, 1.0F, 1.0F, 1.0F);
                     }

                     if (shader.method_34582("uAlphaMode") != null) {
                        shader.method_34582("uAlphaMode").method_1251(1.0F);
                     }
                  }

                  try {
                     class_286.method_43433(bufferHolder[0].method_60800());
                  } catch (Exception var42) {
                  }

                  if (ragdoll != active.get(active.size() - 1)) {
                     bufferHolder[0] = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
                  }
               }

               matrices.method_22909();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
            }
         } else {
            synchronized (this.ragdolls) {
               this.ragdolls.clear();
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Ragdoll {
      public final List<RagdollRenderer.Ragdoll.Shard> shards = new ArrayList<>();
      public final double x;
      public final double y;
      public final double z;
      public final long spawnTime;
      public static final long LIFESPAN = 1500L;

      public Ragdoll(List<RagdollRenderer.SnapshotVertexConsumer.CapturedVertex> capturedVertices, double x, double y, double z, float entityHeight) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.spawnTime = System.currentTimeMillis();
         class_243 entityCenter = new class_243(0.0, entityHeight / 2.0, 0.0);

         for (int i = 0; i + 3 < capturedVertices.size(); i += 4) {
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v0 = capturedVertices.get(i);
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v1 = capturedVertices.get(i + 1);
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v2 = capturedVertices.get(i + 2);
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v3 = capturedVertices.get(i + 3);
            this.shards.add(new RagdollRenderer.Ragdoll.Shard(v0, v1, v2, v2, entityCenter));
            this.shards.add(new RagdollRenderer.Ragdoll.Shard(v0, v2, v3, v3, entityCenter));
         }
      }

      @Environment(EnvType.CLIENT)
      public static class Shard {
         public final RagdollRenderer.SnapshotVertexConsumer.CapturedVertex[] vertices = new RagdollRenderer.SnapshotVertexConsumer.CapturedVertex[4];
         public final class_243[] vertexDistortions = new class_243[4];
         public final class_243 center;
         public final class_243 velocity;
         public final class_243 rotationAxis;
         public final float rotationSpeed;

         public Shard(
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v0,
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v1,
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v2,
            RagdollRenderer.SnapshotVertexConsumer.CapturedVertex v3,
            class_243 entityCenter
         ) {
            this.vertices[0] = v0;
            this.vertices[1] = v1;
            this.vertices[2] = v2;
            this.vertices[3] = v3;
            double cx = (v0.x + v1.x + v2.x + v3.x) / 4.0;
            double cy = (v0.y + v1.y + v2.y + v3.y) / 4.0;
            double cz = (v0.z + v1.z + v2.z + v3.z) / 4.0;
            this.center = new class_243(cx, cy, cz);

            for (int i = 0; i < 4; i++) {
               this.vertexDistortions[i] = new class_243((Math.random() - 0.5) * 0.25, (Math.random() - 0.5) * 0.25, (Math.random() - 0.5) * 0.25);
            }

            double dx = cx - entityCenter.field_1352;
            double dy = cy - entityCenter.field_1351;
            double dz = cz - entityCenter.field_1350;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.01) {
               dx = Math.random() - 0.5;
               dy = Math.random() - 0.5;
               dz = Math.random() - 0.5;
               len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }

            double speed = 0.4 + Math.random() * 0.8;
            this.velocity = new class_243(
               dx / len * speed + (Math.random() - 0.5) * 0.2, dy / len * speed + Math.random() * 0.5 + 0.3, dz / len * speed + (Math.random() - 0.5) * 0.2
            );
            this.rotationAxis = new class_243(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).method_1029();
            this.rotationSpeed = (float)(Math.random() * 8.0 + 3.0);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SnapshotVertexConsumer implements class_4588 {
      public final List<RagdollRenderer.SnapshotVertexConsumer.CapturedVertex> vertices = new ArrayList<>();
      private RagdollRenderer.SnapshotVertexConsumer.CapturedVertex currentVertex = new RagdollRenderer.SnapshotVertexConsumer.CapturedVertex();
      private boolean hasVertex = false;

      public class_4588 method_22912(float x, float y, float z) {
         if (this.hasVertex) {
            this.vertices.add(this.currentVertex);
         }

         this.currentVertex = new RagdollRenderer.SnapshotVertexConsumer.CapturedVertex();
         this.currentVertex.x = x;
         this.currentVertex.y = y;
         this.currentVertex.z = z;
         this.hasVertex = true;
         return this;
      }

      public class_4588 method_1336(int r, int g, int b, int a) {
         this.currentVertex.r = r / 255.0F;
         this.currentVertex.g = g / 255.0F;
         this.currentVertex.b = b / 255.0F;
         this.currentVertex.a = a / 255.0F;
         return this;
      }

      public class_4588 method_22915(float r, float g, float b, float a) {
         this.currentVertex.r = r;
         this.currentVertex.g = g;
         this.currentVertex.b = b;
         this.currentVertex.a = a;
         return this;
      }

      public class_4588 method_22913(float u, float v) {
         this.currentVertex.u = u;
         this.currentVertex.v = v;
         return this;
      }

      public class_4588 method_60796(int u, int v) {
         return this;
      }

      public class_4588 method_22921(int u, int v) {
         return this;
      }

      public class_4588 method_22914(float x, float y, float z) {
         return this;
      }

      public void commitLast() {
         if (this.hasVertex) {
            this.vertices.add(this.currentVertex);
            this.hasVertex = false;
         }
      }

      @Environment(EnvType.CLIENT)
      public static class CapturedVertex {
         public float x;
         public float y;
         public float z;
         public float u;
         public float v;
         public float r = 1.0F;
         public float g = 1.0F;
         public float b = 1.0F;
         public float a = 1.0F;
      }
   }
}
