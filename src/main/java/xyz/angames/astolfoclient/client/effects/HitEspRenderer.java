package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.HitEspModule;

@Environment(EnvType.CLIENT)
public class HitEspRenderer {
   private static final class_2960 TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/hit_effect.png");
   private final HitEspManager manager;

   public HitEspRenderer(HitEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      HitEspModule hitEspModule = (HitEspModule)AstolfoclientClient.moduleManager.getModuleByName("HitESP");
      if (hitEspModule != null && hitEspModule.isEnabled()) {
         List<HitEspEffect> validEffects = this.manager.getEffects();
         if (!validEffects.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShader(class_10142.field_53880);
            class_289 tessellator = class_289.method_1348();
            class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
            long currentTime = System.currentTimeMillis();
            double tickDelta = context.tickCounter().method_60637(true);
            int rgb = ThemeManager.getThemedColor(0L);
            float r = (rgb >> 16 & 0xFF) / 255.0F;
            float g = (rgb >> 8 & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float a = 1.0F;
            float baseScale = hitEspModule.size.getFloat();
            float rotSpeed = hitEspModule.rotationSpeed.getFloat();
            float spawnAnimDuration = hitEspModule.spawnAnimDuration.getFloat();
            float leaveTime = hitEspModule.leaveTime.getFloat();
            float fadeTime = hitEspModule.fadeTime.getFloat();
            boolean doExplosion = hitEspModule.doExplosion.get();
            boolean drewAnything = false;

            for (HitEspEffect effect : validEffects) {
               class_4587 matrixStack = context.matrixStack();
               long age = currentTime - effect.creationTime;
               if (!effect.isShattered) {
                  float textureRotation = (float)age * rotSpeed * effect.rotationDirection;
                  float scale = baseScale;
                  float alpha = 1.0F;
                  if ((float)age < spawnAnimDuration) {
                     float p = (float)age / spawnAnimDuration;
                     float ease = (float)(Math.sin(-20.420352248333657 * (p + 1.0)) * Math.pow(2.0, -10.0 * p) + 1.0);
                     scale = baseScale * ease;
                     alpha = Math.min(1.0F, p * 2.5F);
                  }

                  if (!doExplosion && (float)age >= leaveTime) {
                     float fadeProgress = ((float)age - leaveTime) / fadeTime;
                     alpha = Math.max(0.0F, 1.0F - fadeProgress);
                  }

                  alpha *= a;
                  if (alpha > 0.01F) {
                     matrixStack.method_22903();
                     matrixStack.method_22904(
                        effect.position.field_1352 - context.camera().method_19326().field_1352,
                        effect.position.field_1351 - context.camera().method_19326().field_1351,
                        effect.position.field_1350 - context.camera().method_19326().field_1350
                     );
                     matrixStack.method_22907(effect.orientation);
                     matrixStack.method_22907(class_7833.field_40718.rotation(textureRotation));
                     matrixStack.method_22905(scale, scale, scale);
                     Matrix4f matrix = matrixStack.method_23760().method_23761();
                     buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, alpha);
                     buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, alpha);
                     buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, alpha);
                     buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, alpha);
                     matrixStack.method_22909();
                     drewAnything = true;
                  }
               } else {
                  float groundLifespan = hitEspModule.groundLifespan.getFloat();
                  boolean shrinkOnGround = hitEspModule.shrinkOnGround.get();
                  float gridSize = hitEspModule.gridSize.getFloat();
                  float baseShardScale = baseScale / gridSize * 0.93F;

                  for (HitEspEffect.Shard shard : effect.shards) {
                     float alpha = 1.0F;
                     float shardScale = baseShardScale;
                     if (shard.onGround) {
                        long timeOnGround = currentTime - shard.groundHitTime;
                        float deathProgress = (float)timeOnGround / groundLifespan;
                        alpha = 1.0F - deathProgress;
                        if (alpha < 0.0F) {
                           alpha = 0.0F;
                        }

                        if (shrinkOnGround) {
                           float shrinkEase = 1.0F - (float)Math.pow(deathProgress, 2.0);
                           shardScale *= Math.max(0.0F, shrinkEase);
                        }
                     }

                     alpha *= a;
                     if (alpha > 0.01F && shardScale > 0.01F) {
                        double renderX = class_3532.method_16436(tickDelta, shard.prevPos.field_1352, shard.pos.field_1352);
                        double renderY = class_3532.method_16436(tickDelta, shard.prevPos.field_1351, shard.pos.field_1351);
                        double renderZ = class_3532.method_16436(tickDelta, shard.prevPos.field_1350, shard.pos.field_1350);
                        float renderRotX = (float)class_3532.method_16436(tickDelta, shard.prevRotX, shard.rotX);
                        float renderRotY = (float)class_3532.method_16436(tickDelta, shard.prevRotY, shard.rotY);
                        float renderRotZ = (float)class_3532.method_16436(tickDelta, shard.prevRotZ, shard.rotZ);
                        matrixStack.method_22903();
                        matrixStack.method_22904(
                           renderX - context.camera().method_19326().field_1352,
                           renderY - context.camera().method_19326().field_1351,
                           renderZ - context.camera().method_19326().field_1350
                        );
                        matrixStack.method_22907(class_7833.field_40714.rotationDegrees(renderRotX));
                        matrixStack.method_22907(class_7833.field_40716.rotationDegrees(renderRotY));
                        matrixStack.method_22907(class_7833.field_40718.rotationDegrees(renderRotZ));
                        matrixStack.method_22905(shardScale, shardScale, shardScale);
                        Matrix4f matrix = matrixStack.method_23760().method_23761();
                        buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F).method_22913(shard.u1, shard.v2).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F).method_22913(shard.u2, shard.v2).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F).method_22913(shard.u2, shard.v1).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F).method_22913(shard.u1, shard.v1).method_22915(r, g, b, alpha);
                        matrixStack.method_22909();
                        drewAnything = true;
                     }
                  }
               }
            }

            if (!drewAnything) {
               Matrix4f dummy = context.matrixStack().method_23760().method_23761();
               buffer.method_22918(dummy, 0.0F, 0.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(0, 0, 0, 0);
               buffer.method_22918(dummy, 0.0F, 0.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(0, 0, 0, 0);
               buffer.method_22918(dummy, 0.0F, 0.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(0, 0, 0, 0);
               buffer.method_22918(dummy, 0.0F, 0.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(0, 0, 0, 0);
            }

            class_286.method_43433(buffer.method_60800());
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }
}
