package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class FireFliesModule extends Module {
   private final MinecraftClient mc = MinecraftClient.getInstance();
   public final BooleanSetting darkImprint = new BooleanSetting("DarkImprint", false);
   public final BooleanSetting lighting = new BooleanSetting("Lighting", false);
   public final NumberSetting spawnDelay = new NumberSetting("SpawnDelay", 3.0, 1.0, 10.0, 0.5);
   private static final int MAX_FIREFLIES = 20;
   private static final long MAX_PART_ALIVE_TIME = 6000L;
   private final List<FireFliesModule.FirePart> partList = new ArrayList<>();
   private static final Identifier ICON_TEXTURE = Identifier.of("astolfoclient", "textures/effects/bloom.png");

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
      if (this.isEnabled() && this.mc.player != null && this.mc.world != null) {
         if (this.mc.player.age == 1) {
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

         if (this.partList.size() < 20 && this.mc.player.age % ((int)this.spawnDelay.get() + 1) == 0) {
            this.partList.add(new FireFliesModule.FirePart(this.generateVecForPart(this.mc, 10.0, 4.0), 6000.0F));
            this.partList.add(new FireFliesModule.FirePart(this.generateVecForPart(this.mc, 6.0, 5.0), 6000.0F));
         }
      } else {
         this.partList.clear();
      }
   }

   private Vec3d generateVecForPart(MinecraftClient mc, double rangeXZ, double rangeY) {
      Vec3d pos = mc.player.getPos().add(getRandom(-rangeXZ, rangeXZ), getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ));

      for (int i = 0; i < 30; i++) {
         pos = mc.player.getPos().add(getRandom(-rangeXZ, rangeXZ), getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ));
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
      if (this.isEnabled() && !this.partList.isEmpty() && this.mc.player != null && this.mc.world != null) {
         MatrixStack matrixStack = context.matrixStack();
         float tickDelta = context.tickCounter().getTickDelta(true);
         Vec3d cameraPos = context.camera().getPos();
         Quaternionf cameraRot = context.camera().getRotation();
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
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
         }

         Tessellator tessellator = Tessellator.getInstance();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder sparkBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         boolean hasSparks = false;

         for (FireFliesModule.FirePart part : this.partList) {
            float partAlpha = part.getAlphaPC();
            if (!part.sparkParts.isEmpty()) {
               for (FireFliesModule.SparkPart spark : part.sparkParts) {
                  double sparkX = spark.prevPosX + (spark.posX - spark.prevPosX) * tickDelta - cameraPos.x;
                  double sparkY = spark.prevPosY + (spark.posY - spark.prevPosY) * tickDelta - cameraPos.y;
                  double sparkZ = spark.prevPosZ + (spark.posZ - spark.prevPosZ) * tickDelta - cameraPos.z;
                  matrixStack.push();
                  matrixStack.translate(sparkX, sparkY, sparkZ);
                  matrixStack.multiply(cameraRot);
                  float sparkSize = 0.02F;
                  matrixStack.scale(sparkSize, sparkSize, sparkSize);
                  Matrix4f sm = matrixStack.peek().getPositionMatrix();
                  float sparkAlpha = partAlpha * (1.0F - (float)spark.timePC());
                  sparkBuffer.vertex(sm, -0.5F, -0.5F, 0.0F).color(r, g, b, sparkAlpha);
                  sparkBuffer.vertex(sm, 0.5F, -0.5F, 0.0F).color(r, g, b, sparkAlpha);
                  sparkBuffer.vertex(sm, 0.5F, 0.5F, 0.0F).color(r, g, b, sparkAlpha);
                  sparkBuffer.vertex(sm, -0.5F, 0.5F, 0.0F).color(r, g, b, sparkAlpha);
                  matrixStack.pop();
                  hasSparks = true;
               }
            }
         }

         if (hasSparks) {
            BufferRenderer.drawWithGlobalProgram(sparkBuffer.end());
         }

         for (FireFliesModule.FirePart part : this.partList) {
            if (part.trailParts.size() >= 2) {
               float partAlpha = part.getAlphaPC();
               double dist = cameraPos.distanceTo(part.posVec);
               float width = 1.0E-5F + 8.0F * MathHelper.clamp(1.0F - ((float)dist - 3.0F) / 20.0F, 0.0F, 1.0F);
               RenderSystem.lineWidth(width);
               BufferBuilder lineBuffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
               matrixStack.push();
               matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
               Matrix4f lm = matrixStack.peek().getPositionMatrix();

               for (int i = 0; i < part.trailParts.size(); i++) {
                  FireFliesModule.TrailPart trail = part.trailParts.get(i);
                  float sizePC = (float)i / part.trailParts.size();
                  if (sizePC > 0.5F) {
                     sizePC = 1.0F - sizePC;
                  }

                  sizePC *= 2.0F;
                  float trailAlpha = partAlpha * sizePC;
                  lineBuffer.vertex(lm, (float)trail.x, (float)trail.y, (float)trail.z).color(r, g, b, trailAlpha);
               }

               BufferRenderer.drawWithGlobalProgram(lineBuffer.end());
               matrixStack.pop();
            }
         }

         RenderSystem.lineWidth(1.0F);
         RenderSystem.setShaderTexture(0, ICON_TEXTURE);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         BufferBuilder textureBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         boolean hasTexture = false;

         for (FireFliesModule.FirePart part : this.partList) {
            float partAlpha = part.getAlphaPC();
            double x = part.prevPos.x + (part.posVec.x - part.prevPos.x) * tickDelta - cameraPos.x;
            double y = part.prevPos.y + (part.posVec.y - part.prevPos.y) * tickDelta - cameraPos.y;
            double z = part.prevPos.z + (part.posVec.z - part.prevPos.z) * tickDelta - cameraPos.z;
            matrixStack.push();
            matrixStack.translate(x, y, z);
            matrixStack.multiply(cameraRot);
            float scale = 0.08F;
            matrixStack.scale(scale, scale, scale);
            Matrix4f m = matrixStack.peek().getPositionMatrix();
            textureBuffer.vertex(m, -0.5F, -0.5F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, partAlpha);
            textureBuffer.vertex(m, 0.5F, -0.5F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, partAlpha);
            textureBuffer.vertex(m, 0.5F, 0.5F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, partAlpha);
            textureBuffer.vertex(m, -0.5F, 0.5F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, partAlpha);
            if (this.lighting.get()) {
               matrixStack.scale(3.0F, 3.0F, 3.0F);
               Matrix4f mGlow = matrixStack.peek().getPositionMatrix();
               float glowR = r * 0.4F;
               float glowG = g * 0.4F;
               float glowB = b * 0.4F;
               float glowAlpha = partAlpha / 5.0F;
               textureBuffer.vertex(mGlow, -0.5F, -0.5F, 0.0F).texture(0.0F, 1.0F).color(glowR, glowG, glowB, glowAlpha);
               textureBuffer.vertex(mGlow, 0.5F, -0.5F, 0.0F).texture(1.0F, 1.0F).color(glowR, glowG, glowB, glowAlpha);
               textureBuffer.vertex(mGlow, 0.5F, 0.5F, 0.0F).texture(1.0F, 0.0F).color(glowR, glowG, glowB, glowAlpha);
               textureBuffer.vertex(mGlow, -0.5F, 0.5F, 0.0F).texture(0.0F, 0.0F).color(glowR, glowG, glowB, glowAlpha);
            }

            matrixStack.pop();
            hasTexture = true;
         }

         if (hasTexture) {
            BufferRenderer.drawWithGlobalProgram(textureBuffer.end());
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
      Vec3d posVec;
      Vec3d prevPos;
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

      public FirePart(Vec3d posVec, float maxAlive) {
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
         return MathHelper.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0F, 1.0F);
      }

      public void setAlphaPCTo(float to) {
         this.animTo = to;
      }

      public float getAlphaPC() {
         return this.anim;
      }

      public void updatePart(MinecraftClient mc) {
         this.anim = this.anim + (this.animTo - this.anim) * this.animSpeed;
         this.anim = MathHelper.clamp(this.anim, 0.0F, 1.0F);
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
         if (mc.world != null) {
            Box box = new Box(
               this.posVec.x - scaleBox / 2.0,
               this.posVec.y,
               this.posVec.z - scaleBox / 2.0,
               this.posVec.x + scaleBox / 2.0,
               this.posVec.y + scaleBox,
               this.posVec.z + scaleBox / 2.0
            );
            Iterable<VoxelShape> collisions = mc.world.getBlockCollisions(null, box);
            if (collisions.iterator().hasNext()) {
               collides = true;
            }
         }

         float delente = collides ? 0.3F : 1.0F;
         this.yMotion /= 1.02F;
         this.posVec = this.posVec.add(motionX / delente, this.yMotion / delente, motionZ / delente);
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
         this.posX = part.posVec.x;
         this.posY = part.posVec.y;
         this.posZ = part.posVec.z;
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
         return MathHelper.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
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
         this.x = part.posVec.x;
         this.y = part.posVec.y;
         this.z = part.posVec.z;
         this.startTime = System.currentTimeMillis();
         this.maxTime = maxTime;
      }

      public float getTimePC() {
         return MathHelper.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public boolean toRemove() {
         return this.getTimePC() == 1.0F;
      }
   }
}
