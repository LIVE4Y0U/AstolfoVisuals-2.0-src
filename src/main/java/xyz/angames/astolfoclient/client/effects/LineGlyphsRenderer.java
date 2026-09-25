package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.LineGlyphsModule;

@Environment(EnvType.CLIENT)
public class LineGlyphsRenderer {
   private final List<LineGlyphsRenderer.GlyphsVecGen> glyphs = new ArrayList<>();
   private final Random rand = new Random(93882L);
   private final class_310 client = class_310.method_1551();
   private long lastTickTime = 0L;

   public void render(WorldRenderContext context) {
      Module mod = AstolfoclientClient.moduleManager.getModuleByName("LineGlyphs");
      if (mod instanceof LineGlyphsModule && mod.isEnabled() && this.client.field_1724 != null && this.client.field_1687 != null) {
         LineGlyphsModule module = (LineGlyphsModule)mod;
         int maxCount = (int)module.glyphsCount.get();
         boolean slow = module.slowSpeed.get();
         boolean glowing = module.linesGlowing.get();
         long now = System.currentTimeMillis();
         if (now - this.lastTickTime > 50L) {
            this.glyphsUpdate(slow);
            this.addAllGlyphs(maxCount);
            this.lastTickTime = now;
         }

         this.glyphs.removeIf(genx -> genx.isToRemove());
         if (!this.glyphs.isEmpty()) {
            class_4587 stack = context.matrixStack();
            class_243 cam = context.camera().method_19326();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, glowing ? class_4534.ONE : class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ONE, class_4534.ZERO);
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(class_10142.field_53876);
            class_289 tessellator = class_289.method_1348();
            float pTicks = context.tickCounter().method_60637(true);
            int colorIndex = 0;

            for (LineGlyphsRenderer.GlyphsVecGen gen : this.glyphs) {
               this.clientColoredBegin(gen, ++colorIndex, pTicks, cam, stack, tessellator, 1.0F, 0.0F);
            }

            if (glowing) {
               colorIndex = 0;

               for (LineGlyphsRenderer.GlyphsVecGen gen : this.glyphs) {
                  this.clientColoredBegin(gen, ++colorIndex, pTicks, cam, stack, tessellator, 1.5F, 4.0F);
               }

               colorIndex = 0;

               for (LineGlyphsRenderer.GlyphsVecGen gen : this.glyphs) {
                  this.clientColoredBegin(gen, ++colorIndex, pTicks, cam, stack, tessellator, 1.9F, 9.0F);
               }
            }

            RenderSystem.lineWidth(1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      } else {
         this.glyphs.clear();
      }
   }

   private void clientColoredBegin(
      LineGlyphsRenderer.GlyphsVecGen gen, int colorIndex, float pTicks, class_243 cam, class_4587 stack, class_289 tessellator, float widthMul, float widthAdd
   ) {
      if (gen.vecGens.size() >= 2) {
         float lineWidth = this.calcLineWidth(gen, cam);
         RenderSystem.lineWidth(Math.min(lineWidth * widthMul + widthAdd, 15.0F));
         Matrix4f mat = stack.method_23760().method_23761();
         class_287 buffer = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
         List<class_243> vecs = gen.getPosVectors(pTicks);
         float alphaPC = gen.getAlphaPC();
         if (widthAdd == 4.0F) {
            alphaPC *= 0.1F;
         }

         if (widthAdd == 9.0F) {
            alphaPC *= 0.04F;
         }

         int index = 0;

         for (class_243 vec : vecs) {
            float pointAlpha = alphaPC * (0.25F + (float)index / gen.vecGens.size() / 1.75F);
            Color c = new Color(ThemeManager.getThemedColor(colorIndex * 15 + index * 5));
            float r = c.getRed() / 255.0F * 0.9F + 0.1F;
            float g = c.getGreen() / 255.0F * 0.9F + 0.1F;
            float b = c.getBlue() / 255.0F * 0.9F + 0.1F;
            buffer.method_22918(
                  mat, (float)(vec.field_1352 - cam.field_1352), (float)(vec.field_1351 - cam.field_1351), (float)(vec.field_1350 - cam.field_1350)
               )
               .method_22915(r, g, b, pointAlpha);
            index++;
         }

         class_286.method_43433(buffer.method_60800());
      }
   }

   private float calcLineWidth(LineGlyphsRenderer.GlyphsVecGen gen, class_243 cam) {
      class_2382 pos = gen.vecGens
         .stream()
         .min(Comparator.comparingDouble(v -> cam.method_1028(v.method_10263(), v.method_10264(), v.method_10260())))
         .orElse(gen.vecGens.get(0));
      double dst = Math.sqrt(cam.method_1028(pos.method_10263(), pos.method_10264(), pos.method_10260()));
      return 1.0E-4F + 3.0F * (float)class_3532.method_15350(1.0 - dst / 20.0, 0.0, 1.0);
   }

   private void glyphsUpdate(boolean slowSpeed) {
      for (LineGlyphsRenderer.GlyphsVecGen gen : this.glyphs) {
         gen.update(slowSpeed);
      }
   }

   private void addAllGlyphs(int countCap) {
      while (this.glyphs.size() < countCap) {
         class_2382 pos = this.randGlyphSpawnPos();
         this.glyphs.add(new LineGlyphsRenderer.GlyphsVecGen(pos, this.randInt(7, 12)));
      }
   }

   private class_2382 randGlyphSpawnPos() {
      class_243 cam = this.client.field_1724 != null ? this.client.field_1724.method_19538() : class_243.field_1353;
      double fov = ((Integer)this.client.field_1690.method_41808().method_41753()).intValue();
      float yaw = this.client.field_1724 != null ? this.client.field_1724.method_36454() : 0.0F;

      for (int attempt = 16; attempt > 0; attempt--) {
         double dst = this.randInt(6, 24);
         int yawMin = (int)(yaw - fov * 0.75);
         int yawMax = (int)(yaw + fov * 0.75);
         float radYaw = (float)Math.toRadians(this.randInt(yawMin, yawMax));
         int randXOff = (int)(-(class_3532.method_15374(radYaw) * dst));
         int randYOff = this.randInt(0, 12);
         int randZOff = (int)(class_3532.method_15362(radYaw) * dst);
         class_2382 pos = new class_2382((int)cam.field_1352 + randXOff, (int)cam.field_1351 + randYOff, (int)cam.field_1350 + randZOff);
         if (this.isSpawnPosFree(pos)) {
            return pos;
         }
      }

      return new class_2382((int)cam.field_1352, (int)cam.field_1351, (int)cam.field_1350);
   }

   private boolean isSpawnPosFree(class_2382 pos) {
      if (this.client.field_1687 == null) {
         return true;
      } else {
         class_2338 bp = new class_2338(pos.method_10263(), pos.method_10264(), pos.method_10260());
         class_2680 state = this.client.field_1687.method_8320(bp);
         if (state.method_26215()) {
            return true;
         } else {
            return !state.method_26227().method_15769() ? false : state.method_26220(this.client.field_1687, bp).method_1110();
         }
      }
   }

   private int randInt(int min, int max) {
      return max <= min ? min : this.rand.nextInt(max - min) + min;
   }

   private int[] getR360XY() {
      return new int[]{this.rand.nextInt(4) * 90, (this.rand.nextInt(2) - 1) * 90};
   }

   private int[] getA90R(int[] outdated) {
      int a = outdated[0];
      int b = outdated[1];

      for (int maxAttempt = 150; maxAttempt > 0 && Math.abs(b - outdated[1]) != 90; maxAttempt--) {
         b = (this.rand.nextInt(4) - 2) * 90;
      }

      for (int maxAttempt = 5; maxAttempt > 0 && (Math.abs(a - outdated[0]) != 90 || Math.abs(a - outdated[0]) != 270); maxAttempt--) {
         a = this.rand.nextInt(4) * 90;
      }

      return new int[]{a, b};
   }

   private class_2382 offsetFromRXYR(class_2382 vec3i, int[] rxy, int r) {
      float yawR = (float)Math.toRadians(rxy[0]);
      float pitchR = (float)Math.toRadians(rxy[1]);
      float r1 = r;
      int ry = (int)(class_3532.method_15374(pitchR) * r1);
      if (pitchR != 0.0F) {
         r1 = 0.0F;
      }

      int rx = (int)(-(class_3532.method_15374(yawR) * r1));
      int rz = (int)(class_3532.method_15362(yawR) * r1);
      return new class_2382(vec3i.method_10263() + rx, vec3i.method_10264() + ry, vec3i.method_10260() + rz);
   }

   @Environment(EnvType.CLIENT)
   private class GlyphsVecGen {
      private final List<class_2382> vecGens = new ArrayList<>();
      private int currentStepTicks;
      private int lastStepSet;
      private int stepsAmount;
      private int[] lastYawPitch;
      private long spawnTime;
      private boolean removing = false;
      private long removeTime;

      GlyphsVecGen(class_2382 spawnPos, int maxStepsAmount) {
         this.vecGens.add(spawnPos);
         this.lastYawPitch = LineGlyphsRenderer.this.getR360XY();
         this.stepsAmount = maxStepsAmount;
         this.spawnTime = System.currentTimeMillis();
      }

      private void update(boolean slowSpeed) {
         if (this.stepsAmount == 0 && !this.removing) {
            this.removing = true;
            this.removeTime = System.currentTimeMillis();
         }

         if (this.currentStepTicks > 0) {
            this.currentStepTicks -= slowSpeed ? 1 : 2;
            if (this.currentStepTicks < 0) {
               this.currentStepTicks = 0;
            }
         } else if (!this.removing) {
            class_2382 last = this.vecGens.get(this.vecGens.size() - 1);
            boolean added = false;

            for (int attempt = 6; attempt > 0; attempt--) {
               int[] nextR = LineGlyphsRenderer.this.getA90R(this.lastYawPitch);
               int step = LineGlyphsRenderer.this.randInt(0, 3);
               class_2382 next = LineGlyphsRenderer.this.offsetFromRXYR(last, nextR, step);
               if (LineGlyphsRenderer.this.isSpawnPosFree(next)) {
                  this.lastYawPitch = nextR;
                  this.lastStepSet = this.currentStepTicks = step;
                  this.vecGens.add(next);
                  this.stepsAmount--;
                  added = true;
                  break;
               }
            }

            if (!added) {
               this.stepsAmount = 0;
            }
         }
      }

      public List<class_243> getPosVectors(float pTicks) {
         List<class_243> smoothVecs = new ArrayList<>();
         float advance = Math.min(Math.max(1.0F - (this.currentStepTicks - pTicks) / Math.max(1, this.lastStepSet), 0.0F), 1.0F);

         for (int i = 0; i < this.vecGens.size(); i++) {
            class_2382 v = this.vecGens.get(i);
            double x = v.method_10263();
            double y = v.method_10264();
            double z = v.method_10260();
            if (this.vecGens.size() >= 2 && i == this.vecGens.size() - 1 && !this.removing) {
               class_2382 prev = this.vecGens.get(this.vecGens.size() - 2);
               x = prev.method_10263() + (x - prev.method_10263()) * advance;
               y = prev.method_10264() + (y - prev.method_10264()) * advance;
               z = prev.method_10260() + (z - prev.method_10260()) * advance;
            }

            smoothVecs.add(new class_243(x, y, z));
         }

         return smoothVecs;
      }

      public float getAlphaPC() {
         long now = System.currentTimeMillis();
         return this.removing ? Math.max(0.0F, 1.0F - (float)(now - this.removeTime) / 500.0F) : Math.min(1.0F, (float)(now - this.spawnTime) / 500.0F);
      }

      public boolean isToRemove() {
         return this.removing && System.currentTimeMillis() - this.removeTime > 500L;
      }
   }
}
