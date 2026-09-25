package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_10142;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class DashTrailModule extends Module {
   public static DashTrailModule INSTANCE;
   private static final class_2960 DASH_BLOOM = class_2960.method_60655("astolfoclient", "textures/effects/dashtrail/dashbloom.png");
   private static final int MAX_CUBICS = 1000;
   private final class_310 mc = class_310.method_1551();
   public final BooleanSetting firstPerson = new BooleanSetting("First Person", false);
   public final ModeSetting colorMode = new ModeSetting("Color", "Client", "Client", "Rainbow");
   public final BooleanSetting motionsSmoothing = new BooleanSetting("Motion Smoothing", false);
   public final BooleanSetting dashDots = new BooleanSetting("Sparks", true);
   public final BooleanSetting lighting = new BooleanSetting("Lighting", true);
   public final NumberSetting dashLength = new NumberSetting("Length", 0.75, 0.5, 2.0, 0.05);
   private final List<class_2960> dashCubicTextures = new ArrayList<>();
   private final List<List<class_2960>> dashCubicAnimatedTextures = new ArrayList<>();
   private final List<DashTrailModule.DashCubic> dashCubics = new ArrayList<>();
   private final Random random = new Random(1234567891L);
   private class_243 prevPlayerPos = null;

   public DashTrailModule() {
      super("DashTrail", "Dash trail behind player", Module.Category.RENDER);
      INSTANCE = this;
      this.addSettings(this.firstPerson, this.colorMode, this.motionsSmoothing, this.dashDots, this.lighting, this.dashLength);
      this.loadTextures();
      WorldRenderEvents.LAST.register(this::onRender3D);
   }

   private void loadTextures() {
      for (int i = 1; i <= 21; i++) {
         this.dashCubicTextures.add(class_2960.method_60655("astolfoclient", "textures/effects/dashtrail/dashcubics/dashcubic" + i + ".png"));
      }

      int[] groupCounts = new int[]{11, 23, 32, 16, 32};

      for (int g = 0; g < groupCounts.length; g++) {
         List<class_2960> group = new ArrayList<>();

         for (int f = 1; f <= groupCounts[g]; f++) {
            group.add(class_2960.method_60655("astolfoclient", "textures/effects/dashtrail/dashcubics/group_dashs/group" + (g + 1) + "/dashcubic" + f + ".png"));
         }

         this.dashCubicAnimatedTextures.add(group);
      }
   }

   private int getColorDashCubic() {
      return switch (this.colorMode.get()) {
         case "Rainbow" -> Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0F, 0.8F, 1.0F).getRGB();
         case "Client" -> ThemeManager.getThemedColor(0L);
         default -> ThemeManager.getThemedColor(0L);
      };
   }

   private static int swapAlpha(int color, float alpha) {
      return color & 16777215 | class_3532.method_15340((int)alpha, 0, 255) << 24;
   }

   private static int toDark(int color, float factor) {
      int a = color >> 24 & 0xFF;
      return (int)((color >> 16 & 0xFF) / 255.0F * factor * 255.0F) << 16
         | (int)((color >> 8 & 0xFF) / 255.0F * factor * 255.0F) << 8
         | (int)((color & 0xFF) / 255.0F * factor * 255.0F)
         | a << 24;
   }

   private static int getOverallColorFrom(int c1, int c2, float f) {
      f = class_3532.method_15363(f, 0.0F, 1.0F);
      int r = (int)((c1 >> 16 & 0xFF) + ((c2 >> 16 & 0xFF) - (c1 >> 16 & 0xFF)) * f);
      int g = (int)((c1 >> 8 & 0xFF) + ((c2 >> 8 & 0xFF) - (c1 >> 8 & 0xFF)) * f);
      int b = (int)((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * f);
      int a = (int)((c1 >> 24 & 0xFF) + ((c2 >> 24 & 0xFF) - (c1 >> 24 & 0xFF)) * f);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static float easeInOutQuadWave(float t) {
      return t < 0.5F ? 2.0F * t * t : 1.0F - (-2.0F * t + 2.0F) * (-2.0F * t + 2.0F) / 2.0F;
   }

   private static void addVertex(class_287 bb, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
      bb.method_22918(matrix, x, y, z).method_22913(u, v).method_1336(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
   }

   private static void bufferEnd(class_287 bb) {
      class_286.method_43433(bb.method_60800());
   }

   @Override
   public void onTick() {
      if (this.isEnabled() && this.mc.field_1687 != null && this.mc.field_1724 != null) {
         for (int i = this.dashCubics.size() - 1; i >= 0; i--) {
            DashTrailModule.DashCubic c = this.dashCubics.get(i);
            if (c.getTimePC() >= 1.0F && c.alphaTarget != 0.0F) {
               c.alphaTarget = 0.0F;
            }

            if (c.getTimePC() >= 1.0F && c.alphaTarget == 0.0F && c.alphaValue < 0.02F) {
               this.dashCubics.remove(i);
            }
         }

         int size = this.dashCubics.size();

         for (int i = 0; i < size; i++) {
            DashTrailModule.DashCubic current = this.dashCubics.get(i);
            DashTrailModule.DashCubic next = this.motionsSmoothing.get() && i + 1 < size ? this.dashCubics.get(i + 1) : null;
            current.motionCubicProcess(next);
         }

         class_1657 player = this.mc.field_1724;
         class_243 currentPos = player.method_19538();
         if (this.prevPlayerPos != null) {
            double dx = currentPos.field_1352 - this.prevPlayerPos.field_1352;
            double dy = currentPos.field_1351 - this.prevPlayerPos.field_1351;
            double dz = currentPos.field_1350 - this.prevPlayerPos.field_1350;
            double entitySpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double entitySpeedXZ = Math.sqrt(dx * dx + dz * dz);
            if (entitySpeedXZ >= 0.05) {
               int countMax = entitySpeed > 1.5 ? 4 : 2;
               boolean[] dashPops = this.getDashPops();

               for (int count = 0; count < countMax; count++) {
                  this.dashCubics
                     .add(
                        new DashTrailModule.DashCubic(
                           new DashTrailModule.DashBase(
                              player, 0.04F, new DashTrailModule.DashTexture(true), (float)count / countMax, this.getRandomTimeAnimationPerTime()
                           ),
                           dashPops[0] || dashPops[1]
                        )
                     );
                  if (this.dashCubics.size() > 1000) {
                     this.dashCubics.remove(0);
                  }
               }
            }
         }

         this.prevPlayerPos = currentPos;
      }
   }

   private boolean[] getDashPops() {
      return new boolean[]{false, this.dashDots.get()};
   }

   private int getRandomTimeAnimationPerTime() {
      return (int)((550 + this.random.nextInt(300)) * this.dashLength.getFloat());
   }

   private boolean hasChancedAnimatedTextureSet() {
      return this.random.nextInt(100) > 40;
   }

   public void onRender3D(WorldRenderContext event) {
      if (this.isEnabled() && !this.dashCubics.isEmpty()) {
         if (this.firstPerson.get() || !this.mc.field_1690.method_31044().method_31034()) {
            float tickDelta = event.tickCounter().method_60637(true);
            float lightingPC = this.lighting.get() ? 1.0F : 0.0F;
            class_4184 camera = event.camera();
            class_243 cam = camera.method_19326();
            double camX = cam.field_1352;
            double camY = cam.field_1351;
            double camZ = cam.field_1350;
            class_4587 matrices = event.matrixStack();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE, class_4535.ONE, class_4534.ZERO);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(class_10142.field_53880);

            for (DashTrailModule.DashCubic cubic : this.dashCubics) {
               if (!(cubic.alphaValue <= 0.05F)) {
                  double dx = cubic.getRenderPosX(tickDelta) - camX;
                  double dy = cubic.getRenderPosY(tickDelta) - camY;
                  double dz = cubic.getRenderPosZ(tickDelta) - camZ;
                  if (!(dx * dx + dy * dy + dz * dz > 2500.0)) {
                     cubic.drawDash(matrices, tickDelta, false, 1.0F, lightingPC, camX, camY, camZ, camera);
                  }
               }
            }

            for (DashTrailModule.DashCubic cubic : this.dashCubics) {
               if (!(cubic.alphaValue <= 0.05F)) {
                  double dx = cubic.getRenderPosX(tickDelta) - camX;
                  double dy = cubic.getRenderPosY(tickDelta) - camY;
                  double dz = cubic.getRenderPosZ(tickDelta) - camZ;
                  if (!(dx * dx + dy * dy + dz * dz > 2500.0)) {
                     cubic.drawDash(matrices, tickDelta, true, 1.0F, lightingPC, camX, camY, camZ, camera);
                  }
               }
            }

            if (this.dashDots.get()) {
               RenderSystem.setShaderTexture(0, DASH_BLOOM);
               class_287 bb = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               boolean drew = false;

               for (DashTrailModule.DashCubic cubic : this.dashCubics) {
                  if (!(cubic.alphaValue <= 0.05F) && !cubic.sparks.isEmpty()) {
                     float aPC = cubic.alphaValue;

                     for (DashTrailModule.DashSpark spark : cubic.sparks) {
                        float sparkAPC = easeInOutQuadWave(class_3532.method_15363((float)spark.alphaPC() * aPC, 0.0F, 1.0F));
                        if (!(sparkAPC <= 0.01F)) {
                           int c = getOverallColorFrom(cubic.color, swapAlpha(-1, cubic.color >> 24 & 0xFF), 1.0F - sparkAPC);
                           c = swapAlpha(c, (c >> 24 & 0xFF) * sparkAPC / 3.0F);
                           double rx = spark.getRenderPosX(tickDelta) + cubic.getRenderPosX(tickDelta) - camX;
                           double ry = spark.getRenderPosY(tickDelta) + cubic.getRenderPosY(tickDelta) - camY;
                           double rz = spark.getRenderPosZ(tickDelta) + cubic.getRenderPosZ(tickDelta) - camZ;
                           matrices.method_22903();
                           matrices.method_22904(rx, ry, rz);
                           matrices.method_22907(camera.method_23767());
                           float sz = 0.06F * sparkAPC;
                           Matrix4f matrix = matrices.method_23760().method_23761();
                           addVertex(bb, matrix, -sz, -sz, 0.0F, 0.0F, 1.0F, c);
                           addVertex(bb, matrix, sz, -sz, 0.0F, 1.0F, 1.0F, c);
                           addVertex(bb, matrix, sz, sz, 0.0F, 1.0F, 0.0F, c);
                           addVertex(bb, matrix, -sz, sz, 0.0F, 0.0F, 0.0F, c);
                           drew = true;
                           matrices.method_22909();
                        }
                     }
                  }
               }

               if (drew) {
                  bufferEnd(bb);
               }
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }

   @Override
   public void onDisable() {
      this.dashCubics.clear();
      this.prevPlayerPos = null;
      super.onDisable();
   }

   @Environment(EnvType.CLIENT)
   private class DashBase {
      private final class_1309 entity;
      private double motionX;
      private double motionY;
      private double motionZ;
      private double posX;
      private double posY;
      private double posZ;
      private double prevPosX;
      private double prevPosY;
      private double prevPosZ;
      private final int rMTime;
      private final DashTrailModule.DashTexture dashTexture;

      private DashBase(class_1309 entity, float speedDash, DashTrailModule.DashTexture dashTexture, float offsetTickPC, int rmTime) {
         this.rMTime = rmTime;
         this.entity = entity;
         this.motionX = entity.method_23317() - entity.field_6014;
         this.motionY = entity.method_23318() - entity.field_6036;
         this.motionZ = entity.method_23321() - entity.field_5969;
         this.posX = entity.field_6014 - this.motionX * offsetTickPC + -0.0875 + 0.175 * Math.random();
         this.posY = entity.field_6036 - this.motionY * offsetTickPC + entity.method_17682() / 3.0F + entity.method_17682() / 4.0F * Math.random() * 0.7;
         this.posZ = entity.field_5969 - this.motionZ * offsetTickPC + -0.0875 + 0.175 * Math.random();
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.motionX *= speedDash;
         this.motionY *= speedDash;
         this.motionZ *= speedDash;
         this.dashTexture = dashTexture;
      }
   }

   @Environment(EnvType.CLIENT)
   private class DashCubic {
      private float alphaValue = 1.0F;
      private float alphaTarget = 1.0F;
      private final long startTime = System.currentTimeMillis();
      private final DashTrailModule.DashBase base;
      private final int color = DashTrailModule.this.getColorDashCubic();
      private final List<DashTrailModule.DashSpark> sparks = new ArrayList<>();
      private final boolean addDops;

      private DashCubic(DashTrailModule.DashBase base, boolean addDops) {
         this.base = base;
         this.addDops = addDops;
      }

      private float getTimePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.base.rMTime, 0.0F, 1.0F);
      }

      private double getRenderPosX(float tickDelta) {
         return this.base.prevPosX + (this.base.posX - this.base.prevPosX) * tickDelta;
      }

      private double getRenderPosY(float tickDelta) {
         return this.base.prevPosY + (this.base.posY - this.base.prevPosY) * tickDelta;
      }

      private double getRenderPosZ(float tickDelta) {
         return this.base.prevPosZ + (this.base.posZ - this.base.prevPosZ) * tickDelta;
      }

      private void motionCubicProcess(DashTrailModule.DashCubic nextCubic) {
         float speed = 0.035F;
         this.alphaValue = this.alphaValue + (this.alphaTarget - this.alphaValue) * speed * 10.0F;
         if (Math.abs(this.alphaValue - this.alphaTarget) < 0.01F) {
            this.alphaValue = this.alphaTarget;
         }

         this.base.prevPosX = this.base.posX;
         this.base.prevPosY = this.base.posY;
         this.base.prevPosZ = this.base.posZ;
         this.base.motionX = (nextCubic != null ? nextCubic.base.motionX : this.base.motionX) / 1.05;
         this.base.posX = this.base.posX + 5.0 * this.base.motionX;
         this.base.motionY = (nextCubic != null ? nextCubic.base.motionY : this.base.motionY) / 1.05;
         this.base.posY = this.base.posY + 5.0 * this.base.motionY / (this.base.motionY < 0.0 ? 1.0F : 3.5F);
         this.base.motionZ = (nextCubic != null ? nextCubic.base.motionZ : this.base.motionZ) / 1.05;
         this.base.posZ = this.base.posZ + 5.0 * this.base.motionZ;
         if (this.addDops) {
            if (this.getTimePC() < 0.3F && DashTrailModule.this.random.nextInt(12) > 5) {
               this.sparks.add(DashTrailModule.this.new DashSpark());
            }

            this.sparks.forEach(DashTrailModule.DashSpark::motionSparkProcess);
         }

         this.sparks.removeIf(DashTrailModule.DashSpark::toRemove);
      }

      private void drawDash(
         class_4587 stack, float tickDelta, boolean isBloom, float alphaPC, float lightingPC, double camX, double camY, double camZ, class_4184 camera
      ) {
         class_2960 texId = isBloom ? DashTrailModule.DASH_BLOOM : this.base.dashTexture.getResource();
         if (texId != null) {
            float aPC = this.alphaValue * alphaPC;
            if (!(aPC < 0.01F)) {
               double rx = this.getRenderPosX(tickDelta) - camX;
               double ry = this.getRenderPosY(tickDelta) - camY;
               double rz = this.getRenderPosZ(tickDelta) - camZ;
               if (isBloom) {
                  float scale = 0.033F * aPC;
                  float extXY = 64.0F * scale;
                  float timePcOf = Math.max(0.0F, 1.0F - this.getTimePC());
                  stack.method_22903();
                  stack.method_22904(rx, ry, rz);
                  stack.method_22907(camera.method_23767());
                  int color1 = DashTrailModule.getOverallColorFrom(this.color, -1, 0.15F);
                  int bloomColor = DashTrailModule.swapAlpha(color1, 55.0F * aPC);
                  RenderSystem.setShaderTexture(0, DashTrailModule.DASH_BLOOM);
                  class_287 bb = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
                  Matrix4f matrix = stack.method_23760().method_23761();
                  float s = extXY / 1.75F * 0.1F;
                  DashTrailModule.addVertex(bb, matrix, -s, -s, 0.0F, 0.0F, 1.0F, bloomColor);
                  DashTrailModule.addVertex(bb, matrix, s, -s, 0.0F, 1.0F, 1.0F, bloomColor);
                  DashTrailModule.addVertex(bb, matrix, s, s, 0.0F, 1.0F, 0.0F, bloomColor);
                  DashTrailModule.addVertex(bb, matrix, -s, s, 0.0F, 0.0F, 0.0F, bloomColor);
                  DashTrailModule.bufferEnd(bb);
                  if (lightingPC != 0.0F) {
                     float aMul = aPC * lightingPC;
                     extXY *= 1.0F + 6.0F * timePcOf * aMul;
                     float s2 = extXY / 2.0F * 0.1F;
                     int glowColor = DashTrailModule.swapAlpha(DashTrailModule.toDark(color1, aMul / 4.0F), 90.0F * aMul);
                     bb = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
                     DashTrailModule.addVertex(bb, matrix, -s2, -s2, 0.0F, 0.0F, 1.0F, glowColor);
                     DashTrailModule.addVertex(bb, matrix, s2, -s2, 0.0F, 1.0F, 1.0F, glowColor);
                     DashTrailModule.addVertex(bb, matrix, s2, s2, 0.0F, 1.0F, 0.0F, glowColor);
                     DashTrailModule.addVertex(bb, matrix, -s2, s2, 0.0F, 0.0F, 0.0F, glowColor);
                     DashTrailModule.bufferEnd(bb);
                  }

                  stack.method_22909();
               } else {
                  float scale = 0.033F * aPC;
                  float extX = 64.0F * scale * 0.1F;
                  float extY = 64.0F * scale * 0.1F;
                  float halfX = extX / 2.0F;
                  float halfY = extY / 2.0F;
                  stack.method_22903();
                  stack.method_22904(rx, ry, rz);
                  stack.method_22907(camera.method_23767());
                  int mainColor = DashTrailModule.toDark(DashTrailModule.getOverallColorFrom(this.color, -1, 0.4F), aPC);
                  RenderSystem.setShaderTexture(0, texId);
                  class_287 bb = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
                  Matrix4f matrix = stack.method_23760().method_23761();
                  DashTrailModule.addVertex(bb, matrix, -halfX, -halfY, 0.0F, 0.0F, 1.0F, mainColor);
                  DashTrailModule.addVertex(bb, matrix, halfX, -halfY, 0.0F, 1.0F, 1.0F, mainColor);
                  DashTrailModule.addVertex(bb, matrix, halfX, halfY, 0.0F, 1.0F, 0.0F, mainColor);
                  DashTrailModule.addVertex(bb, matrix, -halfX, halfY, 0.0F, 0.0F, 0.0F, mainColor);
                  DashTrailModule.bufferEnd(bb);
                  stack.method_22909();
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private class DashSpark {
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      double speed = Math.random() / 50.0;
      double radianYaw = Math.random() * 360.0;
      double radianPitch = -90.0 + Math.random() * 180.0;
      long startTime = System.currentTimeMillis();

      DashSpark() {
      }

      double timePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / 1000.0F, 0.0F, 1.0F);
      }

      double alphaPC() {
         return 1.0 - this.timePC();
      }

      boolean toRemove() {
         return this.timePC() >= 1.0;
      }

      void motionSparkProcess() {
         double radYaw = Math.toRadians(this.radianYaw);
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.posX + Math.sin(radYaw) * this.speed;
         this.posY = this.posY + Math.cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed;
         this.posZ = this.posZ + Math.cos(radYaw) * this.speed;
      }

      double getRenderPosX(float tickDelta) {
         return this.prevPosX + (this.posX - this.prevPosX) * tickDelta;
      }

      double getRenderPosY(float tickDelta) {
         return this.prevPosY + (this.posY - this.prevPosY) * tickDelta;
      }

      double getRenderPosZ(float tickDelta) {
         return this.prevPosZ + (this.posZ - this.prevPosZ) * tickDelta;
      }
   }

   @Environment(EnvType.CLIENT)
   private class DashTexture {
      private final List<class_2960> textures;
      private final boolean animated;
      private final long timeAfterSpawn;
      private final long animationPerTime;

      private DashTexture(boolean animated) {
         boolean isAnimated = animated && DashTrailModule.this.hasChancedAnimatedTextureSet();
         this.animated = isAnimated;
         if (isAnimated) {
            this.timeAfterSpawn = System.currentTimeMillis();
            this.textures = new ArrayList<>(
               DashTrailModule.this.dashCubicAnimatedTextures.get(DashTrailModule.this.random.nextInt(DashTrailModule.this.dashCubicAnimatedTextures.size()))
            );
            this.animationPerTime = DashTrailModule.this.getRandomTimeAnimationPerTime();
         } else {
            this.textures = new ArrayList<>();
            this.textures.add(DashTrailModule.this.dashCubicTextures.get(DashTrailModule.this.random.nextInt(DashTrailModule.this.dashCubicTextures.size())));
            this.timeAfterSpawn = 0L;
            this.animationPerTime = 0L;
         }
      }

      private class_2960 getResource() {
         if (this.animated && !this.textures.isEmpty()) {
            float timePC = (float)((System.currentTimeMillis() - this.timeAfterSpawn) % this.animationPerTime) / (float)this.animationPerTime;
            int fragNumber = class_3532.method_15340((int)(timePC * this.textures.size()), 0, this.textures.size() - 1);
            return this.textures.get(fragNumber);
         } else {
            return this.textures.isEmpty() ? null : this.textures.get(0);
         }
      }
   }
}
