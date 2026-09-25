package xyz.angames.astolfoclient.client.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.QuadRadiusState;
import dev.sxmurxy.mre.builders.states.SizeState;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1684;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1803;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.mixin.DrawContextAccessor;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.TrajectoriesModule;

@Environment(EnvType.CLIENT)
public class TrajectoriesRenderer {
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");
   private static final class_2960 HIT_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/hit.png");
   private final class_310 mc = class_310.method_1551();
   private final Map<Integer, TrajectoriesRenderer.CachedPearl> cachedPearls = new HashMap<>();
   private final List<TrajectoriesRenderer.TimerTagInfo> timersToRender = new ArrayList<>();
   private final Matrix4f lastModelViewMatrix = new Matrix4f();
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private double lastCamX;
   private double lastCamY;
   private double lastCamZ;
   private double lastScaledWidth;
   private double lastScaledHeight;
   private boolean hasMatrices = false;

   public void render(WorldRenderContext context) {
      this.timersToRender.clear();
      this.hasMatrices = false;
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         Module rawMod = AstolfoclientClient.moduleManager.getModuleByName("Trajectories");
         if (rawMod != null && rawMod.isEnabled()) {
            TrajectoriesModule mod = (TrajectoriesModule)rawMod;
            class_4587 matrices = context.matrixStack();
            class_243 camPos = context.camera().method_19326();
            Quaternionf cameraRot = context.camera().method_23767();
            float tickDelta = context.tickCounter().method_60637(true);
            Color themeColor = new Color(ThemeManager.getThemedColor(0L));
            this.lastModelViewMatrix.set(RenderSystem.getModelViewMatrix());
            this.lastProjectionMatrix.set(RenderSystem.getProjectionMatrix());
            this.lastCamX = camPos.field_1352;
            this.lastCamY = camPos.field_1351;
            this.lastCamZ = camPos.field_1350;
            this.lastScaledWidth = this.mc.method_22683().method_4486();
            this.lastScaledHeight = this.mc.method_22683().method_4502();
            this.hasMatrices = true;
            if (mod.thrownPearls.get()) {
               long currentTime = System.currentTimeMillis();

               for (class_1297 entity : this.mc.field_1687.method_18112()) {
                  if (entity instanceof class_1684 pearl) {
                     TrajectoriesRenderer.CachedPearl cache = this.calculatePearlPath(pearl, tickDelta);
                     cache.lastUpdateTime = currentTime;
                     this.cachedPearls.put(pearl.method_5628(), cache);
                  }
               }

               Iterator<Entry<Integer, TrajectoriesRenderer.CachedPearl>> it = this.cachedPearls.entrySet().iterator();

               while (it.hasNext()) {
                  TrajectoriesRenderer.CachedPearl cache = it.next().getValue();
                  int elapsedTicks = (int)((currentTime - cache.lastUpdateTime) / 50L);
                  int currentTicksToLand = cache.ticksToLand - elapsedTicks;
                  if (currentTicksToLand > -5 && elapsedTicks <= 300) {
                     this.drawCachedPearl(matrices, cache, camPos, cameraRot, themeColor, mod, Math.max(0, currentTicksToLand));
                  } else {
                     it.remove();
                  }
               }
            }

            class_1799 stack = this.mc.field_1724.method_6047();
            if (stack.method_7960() || !this.isThrowable(stack.method_7909())) {
               stack = this.mc.field_1724.method_6079();
               if (stack.method_7960() || !this.isThrowable(stack.method_7909())) {
                  return;
               }
            }

            class_1792 item = stack.method_7909();
            boolean isBow = item instanceof class_1753;
            boolean isCrossbow = item instanceof class_1764;
            boolean isTrident = item instanceof class_1835;
            float velocity = 1.5F;
            float gravity = 0.03F;
            float drag = 0.99F;
            float pitchOffset = 0.0F;
            if (isBow) {
               float charge = (72000 - this.mc.field_1724.method_6014()) / 20.0F;
               charge = (charge * charge + charge * 2.0F) / 3.0F;
               if (charge > 1.0F) {
                  charge = 1.0F;
               }

               if (this.mc.field_1724.method_6014() == 0) {
                  charge = 1.0F;
               }

               velocity = charge * 3.0F;
               gravity = 0.05F;
            } else if (isCrossbow) {
               velocity = 3.15F;
               gravity = 0.05F;
            } else if (isTrident) {
               velocity = 2.5F;
               gravity = 0.05F;
            } else if (item instanceof class_1828 || item instanceof class_1803) {
               velocity = 0.5F;
               gravity = 0.05F;
               pitchOffset = -20.0F;
            } else if (item instanceof class_1779) {
               velocity = 0.7F;
               gravity = 0.07F;
               pitchOffset = -20.0F;
            }

            boolean multishot = isCrossbow && stack.method_58657().toString().contains("multishot");
            float waterDrag = !isBow && !isCrossbow && !isTrident ? 0.8F : 0.6F;
            if (multishot) {
               this.simulateAndDraw(matrices, camPos, cameraRot, tickDelta, velocity, gravity, drag, waterDrag, themeColor, mod, -10.0F, pitchOffset);
               this.simulateAndDraw(matrices, camPos, cameraRot, tickDelta, velocity, gravity, drag, waterDrag, themeColor, mod, 10.0F, pitchOffset);
            }

            this.simulateAndDraw(matrices, camPos, cameraRot, tickDelta, velocity, gravity, drag, waterDrag, themeColor, mod, 0.0F, pitchOffset);
         }
      }
   }

   private void simulateAndDraw(
      class_4587 matrices,
      class_243 camPos,
      Quaternionf cameraRot,
      float tickDelta,
      float velocity,
      float gravity,
      float drag,
      float waterDrag,
      Color color,
      TrajectoriesModule mod,
      float yawOffset,
      float pitchOffset
   ) {
      double yaw = class_3532.method_16439(tickDelta, this.mc.field_1724.field_5982, this.mc.field_1724.method_36454()) + yawOffset;
      double pitch = class_3532.method_16439(tickDelta, this.mc.field_1724.field_6004, this.mc.field_1724.method_36455()) + pitchOffset;
      double lerpX = class_3532.method_16436(tickDelta, this.mc.field_1724.field_6038, this.mc.field_1724.method_23317());
      double lerpY = class_3532.method_16436(tickDelta, this.mc.field_1724.field_5971, this.mc.field_1724.method_23318());
      double lerpZ = class_3532.method_16436(tickDelta, this.mc.field_1724.field_5989, this.mc.field_1724.method_23321());
      double yawRad = Math.toRadians(yaw);
      double pitchRad = Math.toRadians(pitch);
      double posX = lerpX - Math.cos(yawRad) * 0.16;
      double posY = lerpY + this.mc.field_1724.method_5751() - 0.1;
      double posZ = lerpZ - Math.sin(yawRad) * 0.16;
      double motionX = -Math.sin(yawRad) * Math.cos(pitchRad);
      double motionY = -Math.sin(pitchRad);
      double motionZ = Math.cos(yawRad) * Math.cos(pitchRad);
      double distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
      motionX = motionX / distance * velocity;
      motionY = motionY / distance * velocity;
      motionZ = motionZ / distance * velocity;
      List<class_243> path = new ArrayList<>();
      class_243 currentPos = new class_243(posX, posY, posZ);
      class_239 hitResult = null;
      int ticksToLand = 0;

      for (int i = 0; i < 300; i++) {
         path.add(new class_243(posX, posY, posZ));
         class_243 nextPos = new class_243(posX + motionX, posY + motionY, posZ + motionZ);
         hitResult = this.mc.field_1687.method_17742(new class_3959(currentPos, nextPos, class_3960.field_17558, class_242.field_1348, this.mc.field_1724));
         if (hitResult != null && hitResult.method_17783() == class_240.field_1332) {
            nextPos = hitResult.method_17784();
         }

         class_238 boundingBox = new class_238(posX, posY, posZ, posX, posY, posZ).method_1012(motionX, motionY, motionZ).method_1014(1.0);

         for (class_1297 entity : this.mc.field_1687.method_8333(this.mc.field_1724, boundingBox, e -> e.method_5863() && e.method_5805())) {
            class_238 entBox = entity.method_5829().method_1014(0.3F);
            if (entBox.method_1006(currentPos)) {
               hitResult = new class_3966(entity);
               nextPos = currentPos;
               break;
            }
         }

         posX = nextPos.field_1352;
         posY = nextPos.field_1351;
         posZ = nextPos.field_1350;
         ticksToLand++;
         if (hitResult != null && hitResult.method_17783() != class_240.field_1333) {
            path.add(new class_243(posX, posY, posZ));
            break;
         }

         class_2338 blockPos = class_2338.method_49637(posX, posY, posZ);
         float currentDrag = this.mc.field_1687.method_8316(blockPos).method_15769() ? drag : waterDrag;
         motionX *= currentDrag;
         motionY *= currentDrag;
         motionZ *= currentDrag;
         motionY -= gravity;
         currentPos = nextPos;
      }

      this.drawZapLine(matrices, path, camPos, cameraRot, color, mod.drawThroughWalls.get(), 0.25F, true);
      if (hitResult != null && hitResult.method_17783() != class_240.field_1333) {
         if (mod.showHitbox.get()) {
            this.drawLandingBox(
               matrices, hitResult, camPos, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, mod.drawThroughWalls.get()
            );
         }

         class_1799 held = this.mc.field_1724.method_6047();
         if (held.method_7960() || !(held.method_7909() instanceof class_1776)) {
            held = this.mc.field_1724.method_6079();
         }

         if (!held.method_7960() && held.method_7909() instanceof class_1776) {
            this.collectCircularTimer(hitResult.method_17784(), ticksToLand, camPos);
         }
      }
   }

   private void drawZapLine(
      class_4587 matrices,
      List<class_243> path,
      class_243 camPos,
      Quaternionf cameraRot,
      Color color,
      boolean drawThroughWalls,
      float scaleMultiplier,
      boolean drawAsLine
   ) {
      if (path != null && !path.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         RenderSystem.disableCull();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         }

         class_289 tessellator = class_289.method_1348();
         float r = color.getRed() / 255.0F;
         float g = color.getGreen() / 255.0F;
         float b = color.getBlue() / 255.0F;
         if (drawAsLine) {
            RenderSystem.setShader(class_10142.field_53876);
            RenderSystem.lineWidth(3.0F);
            class_287 buffer = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
            Matrix4f mx = matrices.method_23760().method_23761();

            for (class_243 point : path) {
               float lx = (float)(point.field_1352 - camPos.field_1352);
               float ly = (float)(point.field_1351 - camPos.field_1351);
               float lz = (float)(point.field_1350 - camPos.field_1350);
               buffer.method_22918(mx, lx, ly, lz).method_22915(r, g, b, 1.0F);
            }

            class_286.method_43433(buffer.method_60800());
            RenderSystem.lineWidth(1.0F);
         } else {
            RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
            RenderSystem.setShader(class_10142.field_53880);
            class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
            class_243 playerPos = this.mc.field_1724 != null ? this.mc.field_1724.method_19538() : class_243.field_1353;

            for (int i = 0; i < path.size() - 1; i++) {
               class_243 p1 = path.get(i);
               class_243 p2 = path.get(i + 1);
               double segmentDist = p1.method_1022(p2);
               if (!(segmentDist < 0.001)) {
                  for (int k = 0; k < 10; k++) {
                     float t = k / 10.0F;
                     class_243 interpolatedPos = p1.method_1019(p2.method_1020(p1).method_1021(t));
                     if (!(interpolatedPos.method_1022(playerPos) <= 2.0)) {
                        float size1 = (float)segmentDist / 3.0F * scaleMultiplier;
                        this.drawBloomGlow(matrices, buffer, interpolatedPos, camPos, cameraRot, size1, r, g, b, 1.0F);
                        float size2 = (float)segmentDist * 2.0F * scaleMultiplier;
                        this.drawBloomGlow(matrices, buffer, interpolatedPos, camPos, cameraRot, size2, r, g, b, 0.05F);
                     }
                  }
               }
            }

            class_286.method_43433(buffer.method_60800());
         }

         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
      }
   }

   private void drawBloomGlow(
      class_4587 matrices, class_287 buffer, class_243 pos, class_243 camPos, Quaternionf cameraRot, float size, float r, float g, float b, float alpha
   ) {
      matrices.method_22903();
      matrices.method_22904(pos.field_1352 - camPos.field_1352, pos.field_1351 - camPos.field_1351, pos.field_1350 - camPos.field_1350);
      matrices.method_22907(cameraRot);
      Matrix4f mx = matrices.method_23760().method_23761();
      buffer.method_22918(mx, -size / 2.0F, -size / 2.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, alpha);
      buffer.method_22918(mx, size / 2.0F, -size / 2.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, alpha);
      buffer.method_22918(mx, size / 2.0F, size / 2.0F, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, alpha);
      buffer.method_22918(mx, -size / 2.0F, size / 2.0F, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, alpha);
      matrices.method_22909();
   }

   private TrajectoriesRenderer.CachedPearl calculatePearlPath(class_1684 pearl, float tickDelta) {
      TrajectoriesRenderer.CachedPearl cache = new TrajectoriesRenderer.CachedPearl();
      cache.path = new ArrayList<>();
      double posX = class_3532.method_16436(tickDelta, pearl.field_6038, pearl.method_23317());
      double posY = class_3532.method_16436(tickDelta, pearl.field_5971, pearl.method_23318());
      double posZ = class_3532.method_16436(tickDelta, pearl.field_5989, pearl.method_23321());
      double motionX = pearl.method_18798().field_1352;
      double motionY = pearl.method_18798().field_1351;
      double motionZ = pearl.method_18798().field_1350;
      class_243 currentPos = new class_243(posX, posY, posZ);
      int ticksToLand = 0;
      class_239 hitResult = null;

      for (int i = 0; i < 300; i++) {
         cache.path.add(new class_243(posX, posY, posZ));
         class_243 nextPos = new class_243(posX + motionX, posY + motionY, posZ + motionZ);
         hitResult = this.mc.field_1687.method_17742(new class_3959(currentPos, nextPos, class_3960.field_17558, class_242.field_1348, pearl));
         if (hitResult != null && hitResult.method_17783() == class_240.field_1332) {
            nextPos = hitResult.method_17784();
         }

         class_238 boundingBox = new class_238(posX, posY, posZ, posX, posY, posZ).method_1012(motionX, motionY, motionZ).method_1014(1.0);

         for (class_1297 entity : this.mc.field_1687.method_8333(pearl, boundingBox, e -> e.method_5863() && e.method_5805())) {
            class_238 entBox = entity.method_5829().method_1014(0.3F);
            if (entBox.method_1006(currentPos)) {
               hitResult = new class_3966(entity);
               nextPos = currentPos;
               break;
            }
         }

         posX = nextPos.field_1352;
         posY = nextPos.field_1351;
         posZ = nextPos.field_1350;
         ticksToLand++;
         if (hitResult != null && hitResult.method_17783() != class_240.field_1333) {
            cache.path.add(new class_243(posX, posY, posZ));
            break;
         }

         class_2338 blockPos = class_2338.method_49637(posX, posY, posZ);
         double currentDrag = this.mc.field_1687.method_8316(blockPos).method_15769() ? 0.99 : 0.8;
         motionX *= currentDrag;
         motionY *= currentDrag;
         motionZ *= currentDrag;
         motionY -= 0.03;
         currentPos = nextPos;
      }

      cache.hitResult = hitResult;
      cache.ticksToLand = ticksToLand;
      return cache;
   }

   private void drawCachedPearl(
      class_4587 matrices,
      TrajectoriesRenderer.CachedPearl cache,
      class_243 camPos,
      Quaternionf cameraRot,
      Color color,
      TrajectoriesModule mod,
      int ticksToLand
   ) {
      this.drawZapLine(matrices, cache.path, camPos, cameraRot, color, mod.drawThroughWalls.get(), 1.0F, false);
      if (cache.hitResult != null && cache.hitResult.method_17783() != class_240.field_1333) {
         if (mod.showHitbox.get()) {
            this.drawLandingBox(
               matrices, cache.hitResult, camPos, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, mod.drawThroughWalls.get()
            );
         }

         this.collectCircularTimer(cache.hitResult.method_17784(), ticksToLand, camPos);
      }
   }

   private void collectCircularTimer(class_243 hitPos, int ticksToLand, class_243 camPos) {
      double dx = hitPos.field_1352 - camPos.field_1352;
      double dy = hitPos.field_1351 + 0.75 - camPos.field_1351;
      double dz = hitPos.field_1350 - camPos.field_1350;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      Vector3f screenPos = this.project3DTo2D(hitPos.field_1352, hitPos.field_1351 + 0.75, hitPos.field_1350);
      if (screenPos != null) {
         this.timersToRender.add(new TrajectoriesRenderer.TimerTagInfo(hitPos, ticksToLand, dist, screenPos.x, screenPos.y));
      }
   }

   private Vector3f project3DTo2D(double x, double y, double z) {
      float rx = (float)(x - this.lastCamX);
      float ry = (float)(y - this.lastCamY);
      float rz = (float)(z - this.lastCamZ);
      Vector4f pos = new Vector4f(rx, ry, rz, 1.0F);
      this.lastModelViewMatrix.transform(pos);
      this.lastProjectionMatrix.transform(pos);
      if (pos.w() <= 0.0F) {
         return null;
      }

      float ndcX = pos.x() / pos.w();
      float ndcY = pos.y() / pos.w();
      float screenX = (ndcX + 1.0F) * 0.5F * (float)this.lastScaledWidth;
      float screenY = (1.0F - ndcY) * 0.5F * (float)this.lastScaledHeight;
      return new Vector3f(screenX, screenY, pos.w());
   }

   public void renderHUD(class_332 drawContext) {
      Module rawMod = AstolfoclientClient.moduleManager.getModuleByName("Trajectories");
      if (rawMod != null && rawMod.isEnabled()) {
         TrajectoriesModule mod = (TrajectoriesModule)rawMod;
         if (this.hasMatrices && !this.timersToRender.isEmpty()) {
            class_4587 matrices = drawContext.method_51448();
            class_4598 imm = ((DrawContextAccessor)drawContext).getVertexConsumers();
            Color themeColor = new Color(ThemeManager.getThemedColor(0L));
            this.timersToRender.sort((t1, t2) -> Double.compare(t2.distance, t1.distance));

            for (TrajectoriesRenderer.TimerTagInfo tag : this.timersToRender) {
               matrices.method_22903();
               matrices.method_22904(tag.x, tag.y, 0.0);
               float scale = (float)(mod.scale.get() * (10.0 / Math.min(10.0, Math.max(2.0, tag.distance))));
               scale = (float)Math.min(scale, mod.scale.get() * 2.0);
               scale *= 0.8F;
               matrices.method_22905(scale, scale, 1.0F);
               this.drawCircularTimer2D(matrices, tag.ticksToLand, themeColor, mod, imm);
               matrices.method_22909();
            }
         }
      }
   }

   private void drawCircularTimer2D(class_4587 matrices, int ticksToLand, Color themeColor, TrajectoriesModule mod, class_4598 imm) {
      float seconds = ticksToLand / 20.0F;
      String text = String.format("%.1fs", seconds);
      class_327 tr = this.mc.field_1772;
      float textWidth = tr.method_1727(text);
      float circleSize = Math.max(20.0F, textWidth + 8.0F);
      matrices.method_22903();
      Matrix4f mx = matrices.method_23760().method_23761();
      float cx = -circleSize / 2.0F;
      float cy = -circleSize / 2.0F;
      if (mod.background.get()) {
         int alphaInt = (int)((float)mod.bgOpacity.get() * 255.0F);
         if (mod.glow.get()) {
            int layers = 8;
            float maxSpread = 6.0F;

            for (int i = layers; i > 0; i--) {
               float progress = (float)i / layers;
               float fade = 1.0F - progress;
               float alpha = fade * fade * 0.25F * (float)mod.bgOpacity.get();
               int currentAlpha = class_3532.method_15340((int)(255.0F * alpha), 0, 255);
               if (currentAlpha > 0) {
                  float expand = progress * maxSpread;
                  Color layerColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), currentAlpha);
                  Builder.rectangle()
                     .size(new SizeState(circleSize + expand * 2.0F, circleSize + expand * 2.0F))
                     .radius(new QuadRadiusState((circleSize + expand * 2.0F) / 2.0F))
                     .color(new QuadColorState(layerColor))
                     .build()
                     .render(mx, cx - expand, cy - expand);
               }
            }
         }

         Builder.rectangle()
            .size(new SizeState(circleSize, circleSize))
            .radius(new QuadRadiusState(circleSize / 2.0F))
            .color(new QuadColorState(new Color(5, 5, 8, Math.min(245, alphaInt))))
            .build()
            .render(mx, cx, cy);
      }

      float textX = -(textWidth / 2.0F);
      float textY = -3.5F;
      tr.method_27521(text, textX, textY, -1, true, mx, imm, class_6415.field_33994, 0, 15728880);
      imm.method_22993();
      matrices.method_22909();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void drawLandingBox(class_4587 matrices, class_239 hit, class_243 camPos, float r, float g, float b, boolean drawThroughWalls) {
      class_289 tessellator = class_289.method_1348();
      if (hit.method_17783() == class_240.field_1332) {
         class_3965 blockHit = (class_3965)hit;
         class_243 hitPos = blockHit.method_17784();
         float size = 1.0F;
         double hX = hitPos.field_1352 - camPos.field_1352;
         double hY = hitPos.field_1351 - camPos.field_1351;
         double hZ = hitPos.field_1350 - camPos.field_1350;
         switch (blockHit.method_17780()) {
            case field_11036:
               hY += 0.005;
               break;
            case field_11033:
               hY -= 0.005;
               break;
            case field_11043:
               hZ -= 0.005;
               break;
            case field_11035:
               hZ += 0.005;
               break;
            case field_11039:
               hX -= 0.005;
               break;
            case field_11034:
               hX += 0.005;
         }

         matrices.method_22903();
         matrices.method_22904(hX, hY, hZ);
         matrices.method_22907(blockHit.method_17780().method_23224());
         matrices.method_22907(class_7833.field_40713.rotationDegrees(-90.0F));
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         RenderSystem.disableCull();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
         }

         RenderSystem.setShaderTexture(0, HIT_TEXTURE);
         RenderSystem.setShader(class_10142.field_53880);
         class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
         Matrix4f mx = matrices.method_23760().method_23761();
         buffer.method_22918(mx, -size / 3.0F, size / 3.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, 1.0F);
         buffer.method_22918(mx, size / 3.0F, size / 3.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, 1.0F);
         buffer.method_22918(mx, size / 3.0F, -size / 3.0F, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, 1.0F);
         buffer.method_22918(mx, -size / 3.0F, -size / 3.0F, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, 1.0F);
         class_286.method_43433(buffer.method_60800());
         matrices.method_22909();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
      } else if (hit.method_17783() == class_240.field_1331) {
         class_1297 entity = ((class_3966)hit).method_17782();
         class_238 bBox = entity.method_5829().method_989(-camPos.field_1352, -camPos.field_1351, -camPos.field_1350);
         Matrix4f mx = matrices.method_23760().method_23761();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
         }

         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);
         this.drawBoxLines(buffer, mx, bBox, r, g, b, 1.0F);
         class_286.method_43433(buffer.method_60800());
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
      }
   }

   private void drawBoxLines(class_287 buffer, Matrix4f mx, class_238 box, float r, float g, float b, float a) {
      float x1 = (float)box.field_1323;
      float y1 = (float)box.field_1322;
      float z1 = (float)box.field_1321;
      float x2 = (float)box.field_1320;
      float y2 = (float)box.field_1325;
      float z2 = (float)box.field_1324;
      buffer.method_22918(mx, x1, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z1).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x2, y2, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y1, z2).method_22915(r, g, b, a);
      buffer.method_22918(mx, x1, y2, z2).method_22915(r, g, b, a);
   }

   private boolean isThrowable(class_1792 item) {
      return item instanceof class_1753
         || item instanceof class_1764
         || item instanceof class_1835
         || item instanceof class_1776
         || item instanceof class_1823
         || item instanceof class_1771
         || item instanceof class_1828
         || item instanceof class_1803
         || item instanceof class_1779;
   }

   @Environment(EnvType.CLIENT)
   private static class CachedPearl {
      List<class_243> path;
      class_239 hitResult;
      int ticksToLand;
      long lastUpdateTime;
   }

   @Environment(EnvType.CLIENT)
   private static class TimerTagInfo {
      class_243 hitPos;
      int ticksToLand;
      double distance;
      double x;
      double y;

      public TimerTagInfo(class_243 hitPos, int ticksToLand, double distance, double x, double y) {
         this.hitPos = hitPos;
         this.ticksToLand = ticksToLand;
         this.distance = distance;
         this.x = x;
         this.y = y;
      }
   }
}
