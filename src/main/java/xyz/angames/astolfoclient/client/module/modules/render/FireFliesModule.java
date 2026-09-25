package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_10142;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class FireFliesModule extends Module {
   private final class_310 mc = class_310.method_1551();
   public final BooleanSetting darkImprint = new BooleanSetting("DarkImprint", false);
   public final BooleanSetting lighting = new BooleanSetting("Lighting", false);
   public final NumberSetting spawnDelay = new NumberSetting("SpawnDelay", 3.0, 1.0, 10.0, 0.5);
   private static final int MAX_FIREFLIES = 20;
   private static final long MAX_PART_ALIVE_TIME = 6000L;
   private final List<FireFliesModule.FirePart> partList = new ArrayList<>();
   private static final class_2960 ICON_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");

   public FireFliesModule() {
      super("FireFlies", "Renders beautiful glowing fireflies around you", Module.Category.RENDER);
      this.addSettings(this.darkImprint, this.lighting, this.spawnDelay);
      WorldRenderEvents.LAST.register(this::render3D);
   }

   @Override
   public void onDisable() {
      this.partList.clear();
   }

   @Override
   public void onTick() {
      if (this.isEnabled() && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.mc.field_1724.field_6012 == 1) {
            for (FireFliesModule.FirePart part : this.partList) {
               part.setToRemove();
            }
         }

         long currentTime = System.currentTimeMillis();

         for (FireFliesModule.FirePart part : this.partList) {
            part.updatePart(this.mc);
         }

         this.partList.removeIf(partx -> partx.toRemove || currentTime - partx.startTime >= 6000L);

         while (this.partList.size() > 20) {
            this.partList.remove(0);
         }

         if (this.partList.size() < 20 && this.mc.field_1724.field_6012 % ((int)this.spawnDelay.get() + 1) == 0) {
            this.partList.add(new FireFliesModule.FirePart(this.generateVecForPart(this.mc, 10.0, 4.0), 6000.0F));
            this.partList.add(new FireFliesModule.FirePart(this.generateVecForPart(this.mc, 6.0, 5.0), 6000.0F));
         }
      } else {
         this.partList.clear();
      }
   }

   private class_243 generateVecForPart(class_310 mc, double rangeXZ, double rangeY) {
      class_243 pos = mc.field_1724.method_19538().method_1031(getRandom(-rangeXZ, rangeXZ), getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ));

      for (int i = 0; i < 30; i++) {
         pos = mc.field_1724.method_19538().method_1031(getRandom(-rangeXZ, rangeXZ), getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ));
      }

      return pos;
   }

   private static float getRandom(double min, double max) {
      return (float)(min + Math.random() * (max - min));
   }

   private static float lerp(float from, float to, float pct) {
      return from + (to - from) * pct;
   }

   private void render3D(WorldRenderContext context) {
      if (this.isEnabled() && !this.partList.isEmpty() && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         class_4587 matrixStack = context.matrixStack();
         float tickDelta = context.tickCounter().method_60637(true);
         class_243 cameraPos = context.camera().method_19326();
         Quaternionf cameraRot = context.camera().method_23767();
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         int baseColor = ThemeManager.getThemedColor(0L);
         float r = (baseColor >> 16 & 0xFF) / 255.0F;
         float g = (baseColor >> 8 & 0xFF) / 255.0F;
         float b = (baseColor & 0xFF) / 255.0F;
         if (this.darkImprint.get()) {
            RenderSystem.defaultBlendFunc();
         } else {
            RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         }

         class_289 tessellator = class_289.method_1348();
         RenderSystem.setShader(class_10142.field_53876);
         class_287 sparkBuffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
         boolean hasSparks = false;

         for (FireFliesModule.FirePart part : this.partList) {
            float partAlpha = part.getAlphaPC();
            if (!part.sparkParts.isEmpty()) {
               for (FireFliesModule.SparkPart spark : part.sparkParts) {
                  double sparkX = spark.prevPosX + (spark.posX - spark.prevPosX) * tickDelta - cameraPos.field_1352;
                  double sparkY = spark.prevPosY + (spark.posY - spark.prevPosY) * tickDelta - cameraPos.field_1351;
                  double sparkZ = spark.prevPosZ + (spark.posZ - spark.prevPosZ) * tickDelta - cameraPos.field_1350;
                  matrixStack.method_22903();
                  matrixStack.method_22904(sparkX, sparkY, sparkZ);
                  matrixStack.method_22907(cameraRot);
                  float sparkSize = 0.02F;
                  matrixStack.method_22905(sparkSize, sparkSize, sparkSize);
                  Matrix4f sm = matrixStack.method_23760().method_23761();
                  float sparkAlpha = partAlpha * (1.0F - (float)spark.timePC());
                  sparkBuffer.method_22918(sm, -0.5F, -0.5F, 0.0F).method_22915(r, g, b, sparkAlpha);
                  sparkBuffer.method_22918(sm, 0.5F, -0.5F, 0.0F).method_22915(r, g, b, sparkAlpha);
                  sparkBuffer.method_22918(sm, 0.5F, 0.5F, 0.0F).method_22915(r, g, b, sparkAlpha);
                  sparkBuffer.method_22918(sm, -0.5F, 0.5F, 0.0F).method_22915(r, g, b, sparkAlpha);
                  matrixStack.method_22909();
                  hasSparks = true;
               }
            }
         }

         if (hasSparks) {
            class_286.method_43433(sparkBuffer.method_60800());
         }

         for (FireFliesModule.FirePart part : this.partList) {
            if (part.trailParts.size() >= 2) {
               float partAlpha = part.getAlphaPC();
               double dist = cameraPos.method_1022(part.posVec);
               float width = 1.0E-5F + 8.0F * class_3532.method_15363(1.0F - ((float)dist - 3.0F) / 20.0F, 0.0F, 1.0F);
               RenderSystem.lineWidth(width);
               class_287 lineBuffer = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
               matrixStack.method_22903();
               matrixStack.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
               Matrix4f lm = matrixStack.method_23760().method_23761();

               for (int i = 0; i < part.trailParts.size(); i++) {
                  FireFliesModule.TrailPart trail = part.trailParts.get(i);
                  float sizePC = (float)i / part.trailParts.size();
                  if (sizePC > 0.5F) {
                     sizePC = 1.0F - sizePC;
                  }

                  sizePC *= 2.0F;
                  float trailAlpha = partAlpha * sizePC;
                  lineBuffer.method_22918(lm, (float)trail.x, (float)trail.y, (float)trail.z).method_22915(r, g, b, trailAlpha);
               }

               class_286.method_43433(lineBuffer.method_60800());
               matrixStack.method_22909();
            }
         }

         RenderSystem.lineWidth(1.0F);
         RenderSystem.setShaderTexture(0, ICON_TEXTURE);
         RenderSystem.setShader(class_10142.field_53880);
         class_287 textureBuffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
         boolean hasTexture = false;

         for (FireFliesModule.FirePart part : this.partList) {
            float partAlpha = part.getAlphaPC();
            double x = part.prevPos.field_1352 + (part.posVec.field_1352 - part.prevPos.field_1352) * tickDelta - cameraPos.field_1352;
            double y = part.prevPos.field_1351 + (part.posVec.field_1351 - part.prevPos.field_1351) * tickDelta - cameraPos.field_1351;
            double z = part.prevPos.field_1350 + (part.posVec.field_1350 - part.prevPos.field_1350) * tickDelta - cameraPos.field_1350;
            matrixStack.method_22903();
            matrixStack.method_22904(x, y, z);
            matrixStack.method_22907(cameraRot);
            float scale = 0.08F;
            matrixStack.method_22905(scale, scale, scale);
            Matrix4f m = matrixStack.method_23760().method_23761();
            textureBuffer.method_22918(m, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, partAlpha);
            textureBuffer.method_22918(m, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, partAlpha);
            textureBuffer.method_22918(m, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, partAlpha);
            textureBuffer.method_22918(m, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, partAlpha);
            if (this.lighting.get()) {
               matrixStack.method_22905(3.0F, 3.0F, 3.0F);
               Matrix4f mGlow = matrixStack.method_23760().method_23761();
               float glowR = r * 0.4F;
               float glowG = g * 0.4F;
               float glowB = b * 0.4F;
               float glowAlpha = partAlpha / 5.0F;
               textureBuffer.method_22918(mGlow, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 1.0F).method_22915(glowR, glowG, glowB, glowAlpha);
               textureBuffer.method_22918(mGlow, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 1.0F).method_22915(glowR, glowG, glowB, glowAlpha);
               textureBuffer.method_22918(mGlow, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 0.0F).method_22915(glowR, glowG, glowB, glowAlpha);
               textureBuffer.method_22918(mGlow, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 0.0F).method_22915(glowR, glowG, glowB, glowAlpha);
            }

            matrixStack.method_22909();
            hasTexture = true;
         }

         if (hasTexture) {
            class_286.method_43433(textureBuffer.method_60800());
         }

         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class FirePart {
      class_243 posVec;
      class_243 prevPos;
      final List<FireFliesModule.TrailPart> trailParts = new ArrayList<>();
      final List<FireFliesModule.SparkPart> sparkParts = new ArrayList<>();
      float anim = 0.0F;
      float animTo = 1.0F;
      float animSpeed = 0.02F;
      int msChangeSideRate;
      float moveYawSet;
      float speed;
      float yMotion;
      float moveYaw;
      float maxAlive;
      long startTime;
      long rateTimer;
      boolean toRemove = false;

      public FirePart(class_243 posVec, float maxAlive) {
         this.posVec = posVec;
         this.prevPos = posVec;
         this.maxAlive = maxAlive;
         this.moveYawSet = FireFliesModule.getRandom(0.0, 360.0);
         this.speed = FireFliesModule.getRandom(0.1, 0.25);
         this.yMotion = FireFliesModule.getRandom(-0.075, 0.1);
         this.moveYaw = this.moveYawSet;
         this.msChangeSideRate = this.calculateMsChangeSideRate();
         this.startTime = System.currentTimeMillis();
         this.rateTimer = System.currentTimeMillis();
      }

      public float getTimePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0F, 1.0F);
      }

      public void setAlphaPCTo(float to) {
         this.animTo = to;
      }

      public float getAlphaPC() {
         return this.anim;
      }

      public void updatePart(class_310 mc) {
         this.anim = this.anim + (this.animTo - this.anim) * this.animSpeed;
         this.anim = class_3532.method_15363(this.anim, 0.0F, 1.0F);
         if (System.currentTimeMillis() - this.rateTimer >= this.msChangeSideRate) {
            this.msChangeSideRate = this.calculateMsChangeSideRate();
            this.rateTimer = System.currentTimeMillis();
            this.moveYawSet = FireFliesModule.getRandom(0.0, 360.0);
         }

         this.moveYaw = FireFliesModule.lerp(this.moveYaw, this.moveYawSet, 0.065F);
         this.speed /= 1.005F;
         float motionX = -((float)Math.sin(Math.toRadians(this.moveYaw))) * this.speed;
         float motionZ = (float)Math.cos(Math.toRadians(this.moveYaw)) * this.speed;
         this.prevPos = this.posVec;
         double scaleBox = 0.1;
         boolean collides = false;
         if (mc.field_1687 != null) {
            class_238 box = new class_238(
               this.posVec.field_1352 - scaleBox / 2.0,
               this.posVec.field_1351,
               this.posVec.field_1350 - scaleBox / 2.0,
               this.posVec.field_1352 + scaleBox / 2.0,
               this.posVec.field_1351 + scaleBox,
               this.posVec.field_1350 + scaleBox / 2.0
            );
            Iterable<class_265> collisions = mc.field_1687.method_20812(null, box);
            if (collisions.iterator().hasNext()) {
               collides = true;
            }
         }

         float delente = collides ? 0.3F : 1.0F;
         this.yMotion /= 1.02F;
         this.posVec = this.posVec.method_1031(motionX / delente, this.yMotion / delente, motionZ / delente);
         if (this.getTimePC() >= 1.0F) {
            this.setAlphaPCTo(0.0F);
            if (this.getAlphaPC() < 0.003921569F) {
               this.setToRemove();
            }
         }

         this.trailParts.add(new FireFliesModule.TrailPart(this, 400));
         this.trailParts.removeIf(FireFliesModule.TrailPart::toRemove);

         for (int i = 0; i < 2; i++) {
            this.sparkParts.add(new FireFliesModule.SparkPart(this, 300));
         }

         for (FireFliesModule.SparkPart spark : this.sparkParts) {
            spark.motionSparkProcess();
         }

         this.sparkParts.removeIf(FireFliesModule.SparkPart::toRemove);
      }

      public void setToRemove() {
         this.toRemove = true;
      }

      private int calculateMsChangeSideRate() {
         return (int)FireFliesModule.getRandom(300.5, 900.5);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class SparkPart {
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      double speed;
      double radianYaw;
      double radianPitch;
      long startTime;
      int maxTime;

      public SparkPart(FireFliesModule.FirePart part, int maxTime) {
         this.posX = part.posVec.field_1352;
         this.posY = part.posVec.field_1351;
         this.posZ = part.posVec.field_1350;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.speed = Math.random() / 30.0;
         this.radianYaw = Math.random() * 360.0;
         this.radianPitch = -90.0 + Math.random() * 180.0;
         this.startTime = System.currentTimeMillis();
         this.maxTime = maxTime;
      }

      public double timePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public boolean toRemove() {
         return this.timePC() == 1.0;
      }

      public void motionSparkProcess() {
         double radYaw = Math.toRadians(this.radianYaw);
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.posX + Math.sin(radYaw) * this.speed;
         this.posY = this.posY + Math.cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed;
         this.posZ = this.posZ + Math.cos(radYaw) * this.speed;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class TrailPart {
      double x;
      double y;
      double z;
      long startTime;
      int maxTime;

      public TrailPart(FireFliesModule.FirePart part, int maxTime) {
         this.x = part.posVec.field_1352;
         this.y = part.posVec.field_1351;
         this.z = part.posVec.field_1350;
         this.startTime = System.currentTimeMillis();
         this.maxTime = maxTime;
      }

      public float getTimePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public boolean toRemove() {
         return this.getTimePC() == 1.0F;
      }
   }
}
