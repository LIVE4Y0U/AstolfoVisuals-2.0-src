package xyz.angames.astolfoclient.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.client.render.VertexConsumerProvider;
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
   private static final Identifier BLOOM_TEXTURE = Identifier.of("astolfoclient", "textures/effects/bloom.png");
   private static final Identifier HIT_TEXTURE = Identifier.of("astolfoclient", "textures/effects/hit.png");
   private final MinecraftClient mc = MinecraftClient.getInstance();
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
      if (this.mc.world != null && this.mc.player != null) {
         Module rawMod = AstolfoclientClient.moduleManager.getModuleByName("Trajectories");
         if (rawMod != null && rawMod.isEnabled()) {
            TrajectoriesModule mod = (TrajectoriesModule)rawMod;
            MatrixStack matrices = context.matrixStack();
            Vec3d camPos = context.camera().getPos();
            Quaternionf cameraRot = context.camera().getRotation();
            float tickDelta = context.tickCounter().getTickDelta(true);
            Color themeColor = new Color(ThemeManager.getThemedColor(0L));
            this.lastModelViewMatrix.set(RenderSystem.getModelViewMatrix());
            this.lastProjectionMatrix.set(RenderSystem.getProjectionMatrix());
            this.lastCamX = camPos.x;
            this.lastCamY = camPos.y;
            this.lastCamZ = camPos.z;
            this.lastScaledWidth = this.mc.getWindow().getScaledWidth();
            this.lastScaledHeight = this.mc.getWindow().getScaledHeight();
            this.hasMatrices = true;
            if (mod.thrownPearls.get()) {
               long currentTime = System.currentTimeMillis();

               for (Entity entity : this.mc.world.getEntities()) {
                  if (entity instanceof EnderPearlEntity pearl) {
                     TrajectoriesRenderer.CachedPearl cache = this.calculatePearlPath(pearl, tickDelta);
                     cache.lastUpdateTime = currentTime;
                     this.cachedPearls.put(pearl.getId(), cache);
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

            ItemStack stack = this.mc.player.getMainHandStack();
            if (stack.isEmpty() || !this.isThrowable(stack.getItem())) {
               stack = this.mc.player.getOffHandStack();
               if (stack.isEmpty() || !this.isThrowable(stack.getItem())) {
                  return;
               }
            }

            Item item = stack.getItem();
            boolean isBow = item instanceof BowItem;
            boolean isCrossbow = item instanceof CrossbowItem;
            boolean isTrident = item instanceof TridentItem;
            float velocity = 1.5F;
            float gravity = 0.03F;
            float drag = 0.99F;
            float pitchOffset = 0.0F;
            if (isBow) {
               float charge = (72000 - this.mc.player.getItemUseTimeLeft()) / 20.0F;
               charge = (charge * charge + charge * 2.0F) / 3.0F;
               if (charge > 1.0F) {
                  charge = 1.0F;
               }

               if (this.mc.player.getItemUseTimeLeft() == 0) {
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
            } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
               velocity = 0.5F;
               gravity = 0.05F;
               pitchOffset = -20.0F;
            } else if (item instanceof ExperienceBottleItem) {
               velocity = 0.7F;
               gravity = 0.07F;
               pitchOffset = -20.0F;
            }

            boolean multishot = isCrossbow && stack.getEnchantments().toString().contains("multishot");
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
      MatrixStack matrices,
      Vec3d camPos,
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
      double yaw = MathHelper.lerp(tickDelta, this.mc.player.prevYaw, this.mc.player.getYaw()) + yawOffset;
      double pitch = MathHelper.lerp(tickDelta, this.mc.player.prevPitch, this.mc.player.getPitch()) + pitchOffset;
      double lerpX = MathHelper.lerp(tickDelta, this.mc.player.lastRenderX, this.mc.player.getX());
      double lerpY = MathHelper.lerp(tickDelta, this.mc.player.lastRenderY, this.mc.player.getY());
      double lerpZ = MathHelper.lerp(tickDelta, this.mc.player.lastRenderZ, this.mc.player.getZ());
      double yawRad = Math.toRadians(yaw);
      double pitchRad = Math.toRadians(pitch);
      double posX = lerpX - Math.cos(yawRad) * 0.16;
      double posY = lerpY + this.mc.player.getStandingEyeHeight() - 0.1;
      double posZ = lerpZ - Math.sin(yawRad) * 0.16;
      double motionX = -Math.sin(yawRad) * Math.cos(pitchRad);
      double motionY = -Math.sin(pitchRad);
      double motionZ = Math.cos(yawRad) * Math.cos(pitchRad);
      double distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
      motionX = motionX / distance * velocity;
      motionY = motionY / distance * velocity;
      motionZ = motionZ / distance * velocity;
      List<Vec3d> path = new ArrayList<>();
      Vec3d currentPos = new Vec3d(posX, posY, posZ);
      HitResult hitResult = null;
      int ticksToLand = 0;

      for (int i = 0; i < 300; i++) {
         path.add(new Vec3d(posX, posY, posZ));
         Vec3d nextPos = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
         hitResult = this.mc.world.raycast(new RaycastContext(currentPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this.mc.player));
         if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            nextPos = hitResult.getPos();
         }

         Box boundingBox = new Box(posX, posY, posZ, posX, posY, posZ).stretch(motionX, motionY, motionZ).expand(1.0);

         for (Entity entity : this.mc.world.getOtherEntities(this.mc.player, boundingBox, e -> e.canHit() && e.isAlive())) {
            Box entBox = entity.getBoundingBox().expand(0.3F);
            if (entBox.contains(currentPos)) {
               hitResult = new EntityHitResult(entity);
               nextPos = currentPos;
               break;
            }
         }

         posX = nextPos.x;
         posY = nextPos.y;
         posZ = nextPos.z;
         ticksToLand++;
         if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            path.add(new Vec3d(posX, posY, posZ));
            break;
         }

         BlockPos blockPos = BlockPos.ofFloored(posX, posY, posZ);
         float currentDrag = this.mc.world.getFluidState(blockPos).isEmpty() ? drag : waterDrag;
         motionX *= currentDrag;
         motionY *= currentDrag;
         motionZ *= currentDrag;
         motionY -= gravity;
         currentPos = nextPos;
      }

      this.drawZapLine(matrices, path, camPos, cameraRot, color, mod.drawThroughWalls.get(), 0.25F, true);
      if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
         if (mod.showHitbox.get()) {
            this.drawLandingBox(
               matrices, hitResult, camPos, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, mod.drawThroughWalls.get()
            );
         }

         ItemStack held = this.mc.player.getMainHandStack();
         if (held.isEmpty() || !(held.getItem() instanceof EnderPearlItem)) {
            held = this.mc.player.getOffHandStack();
         }

         if (!held.isEmpty() && held.getItem() instanceof EnderPearlItem) {
            this.collectCircularTimer(hitResult.getPos(), ticksToLand, camPos);
         }
      }
   }

   private void drawZapLine(
      MatrixStack matrices,
      List<Vec3d> path,
      Vec3d camPos,
      Quaternionf cameraRot,
      Color color,
      boolean drawThroughWalls,
      float scaleMultiplier,
      boolean drawAsLine
   ) {
      if (path != null && !path.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
         RenderSystem.disableCull();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         }

         Tessellator tessellator = Tessellator.getInstance();
         float r = color.getRed() / 255.0F;
         float g = color.getGreen() / 255.0F;
         float b = color.getBlue() / 255.0F;
         if (drawAsLine) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth(3.0F);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            Matrix4f mx = matrices.peek().getPositionMatrix();

            for (Vec3d point : path) {
               float lx = (float)(point.x - camPos.x);
               float ly = (float)(point.y - camPos.y);
               float lz = (float)(point.z - camPos.z);
               buffer.vertex(mx, lx, ly, lz).color(r, g, b, 1.0F);
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.lineWidth(1.0F);
         } else {
            RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Vec3d playerPos = this.mc.player != null ? this.mc.player.getPos() : Vec3d.ZERO;

            for (int i = 0; i < path.size() - 1; i++) {
               Vec3d p1 = path.get(i);
               Vec3d p2 = path.get(i + 1);
               double segmentDist = p1.distanceTo(p2);
               if (!(segmentDist < 0.001)) {
                  for (int k = 0; k < 10; k++) {
                     float t = k / 10.0F;
                     Vec3d interpolatedPos = p1.add(p2.subtract(p1).multiply(t));
                     if (!(interpolatedPos.distanceTo(playerPos) <= 2.0)) {
                        float size1 = (float)segmentDist / 3.0F * scaleMultiplier;
                        this.drawBloomGlow(matrices, buffer, interpolatedPos, camPos, cameraRot, size1, r, g, b, 1.0F);
                        float size2 = (float)segmentDist * 2.0F * scaleMultiplier;
                        this.drawBloomGlow(matrices, buffer, interpolatedPos, camPos, cameraRot, size2, r, g, b, 0.05F);
                     }
                  }
               }
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
         }

         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
      }
   }

   private void drawBloomGlow(
      MatrixStack matrices, BufferBuilder buffer, Vec3d pos, Vec3d camPos, Quaternionf cameraRot, float size, float r, float g, float b, float alpha
   ) {
      matrices.push();
      matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
      matrices.multiply(cameraRot);
      Matrix4f mx = matrices.peek().getPositionMatrix();
      buffer.vertex(mx, -size / 2.0F, -size / 2.0F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, alpha);
      buffer.vertex(mx, size / 2.0F, -size / 2.0F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, alpha);
      buffer.vertex(mx, size / 2.0F, size / 2.0F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, alpha);
      buffer.vertex(mx, -size / 2.0F, size / 2.0F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, alpha);
      matrices.pop();
   }

   private TrajectoriesRenderer.CachedPearl calculatePearlPath(EnderPearlEntity pearl, float tickDelta) {
      TrajectoriesRenderer.CachedPearl cache = new TrajectoriesRenderer.CachedPearl();
      cache.path = new ArrayList<>();
      double posX = MathHelper.lerp(tickDelta, pearl.lastRenderX, pearl.getX());
      double posY = MathHelper.lerp(tickDelta, pearl.lastRenderY, pearl.getY());
      double posZ = MathHelper.lerp(tickDelta, pearl.lastRenderZ, pearl.getZ());
      double motionX = pearl.getVelocity().x;
      double motionY = pearl.getVelocity().y;
      double motionZ = pearl.getVelocity().z;
      Vec3d currentPos = new Vec3d(posX, posY, posZ);
      int ticksToLand = 0;
      HitResult hitResult = null;

      for (int i = 0; i < 300; i++) {
         cache.path.add(new Vec3d(posX, posY, posZ));
         Vec3d nextPos = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
         hitResult = this.mc.world.raycast(new RaycastContext(currentPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, pearl));
         if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            nextPos = hitResult.getPos();
         }

         Box boundingBox = new Box(posX, posY, posZ, posX, posY, posZ).stretch(motionX, motionY, motionZ).expand(1.0);

         for (Entity entity : this.mc.world.getOtherEntities(pearl, boundingBox, e -> e.canHit() && e.isAlive())) {
            Box entBox = entity.getBoundingBox().expand(0.3F);
            if (entBox.contains(currentPos)) {
               hitResult = new EntityHitResult(entity);
               nextPos = currentPos;
               break;
            }
         }

         posX = nextPos.x;
         posY = nextPos.y;
         posZ = nextPos.z;
         ticksToLand++;
         if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            cache.path.add(new Vec3d(posX, posY, posZ));
            break;
         }

         BlockPos blockPos = BlockPos.ofFloored(posX, posY, posZ);
         double currentDrag = this.mc.world.getFluidState(blockPos).isEmpty() ? 0.99 : 0.8;
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
      MatrixStack matrices,
      TrajectoriesRenderer.CachedPearl cache,
      Vec3d camPos,
      Quaternionf cameraRot,
      Color color,
      TrajectoriesModule mod,
      int ticksToLand
   ) {
      this.drawZapLine(matrices, cache.path, camPos, cameraRot, color, mod.drawThroughWalls.get(), 1.0F, false);
      if (cache.hitResult != null && cache.hitResult.getType() != HitResult.Type.MISS) {
         if (mod.showHitbox.get()) {
            this.drawLandingBox(
               matrices, cache.hitResult, camPos, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, mod.drawThroughWalls.get()
            );
         }

         this.collectCircularTimer(cache.hitResult.getPos(), ticksToLand, camPos);
      }
   }

   private void collectCircularTimer(Vec3d hitPos, int ticksToLand, Vec3d camPos) {
      double dx = hitPos.x - camPos.x;
      double dy = hitPos.y + 0.75 - camPos.y;
      double dz = hitPos.z - camPos.z;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      Vector3f screenPos = this.project3DTo2D(hitPos.x, hitPos.y + 0.75, hitPos.z);
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

   public void renderHUD(DrawContext drawContext) {
      Module rawMod = AstolfoclientClient.moduleManager.getModuleByName("Trajectories");
      if (rawMod != null && rawMod.isEnabled()) {
         TrajectoriesModule mod = (TrajectoriesModule)rawMod;
         if (this.hasMatrices && !this.timersToRender.isEmpty()) {
            MatrixStack matrices = drawContext.getMatrices();
            VertexConsumerProvider.Immediate imm = ((DrawContextAccessor)drawContext).getVertexConsumers();
            Color themeColor = new Color(ThemeManager.getThemedColor(0L));
            this.timersToRender.sort((t1, t2) -> Double.compare(t2.distance, t1.distance));

            for (TrajectoriesRenderer.TimerTagInfo tag : this.timersToRender) {
               matrices.push();
               matrices.translate(tag.x, tag.y, 0.0);
               float scale = (float)(mod.scale.get() * (10.0 / Math.min(10.0, Math.max(2.0, tag.distance))));
               scale = (float)Math.min(scale, mod.scale.get() * 2.0);
               scale *= 0.8F;
               matrices.scale(scale, scale, 1.0F);
               this.drawCircularTimer2D(matrices, tag.ticksToLand, themeColor, mod, imm);
               matrices.pop();
            }
         }
      }
   }

   private void drawCircularTimer2D(MatrixStack matrices, int ticksToLand, Color themeColor, TrajectoriesModule mod, VertexConsumerProvider.Immediate imm) {
      float seconds = ticksToLand / 20.0F;
      String text = String.format("%.1fs", seconds);
      TextRenderer tr = this.mc.textRenderer;
      float textWidth = tr.getWidth(text);
      float circleSize = Math.max(20.0F, textWidth + 8.0F);
      matrices.push();
      Matrix4f mx = matrices.peek().getPositionMatrix();
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
               int currentAlpha = MathHelper.clamp((int)(255.0F * alpha), 0, 255);
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
      tr.draw(text, textX, textY, -1, true, mx, imm, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
      imm.draw();
      matrices.pop();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void drawLandingBox(MatrixStack matrices, HitResult hit, Vec3d camPos, float r, float g, float b, boolean drawThroughWalls) {
      Tessellator tessellator = Tessellator.getInstance();
      if (hit.getType() == HitResult.Type.BLOCK) {
         BlockHitResult blockHit = (BlockHitResult)hit;
         Vec3d hitPos = blockHit.getPos();
         float size = 1.0F;
         double hX = hitPos.x - camPos.x;
         double hY = hitPos.y - camPos.y;
         double hZ = hitPos.z - camPos.z;
         switch (blockHit.getSide()) {
            case UP:
               hY += 0.005;
               break;
            case DOWN:
               hY -= 0.005;
               break;
            case NORTH:
               hZ -= 0.005;
               break;
            case SOUTH:
               hZ += 0.005;
               break;
            case WEST:
               hX -= 0.005;
               break;
            case EAST:
               hX += 0.005;
         }

         matrices.push();
         matrices.translate(hX, hY, hZ);
         matrices.multiply(blockHit.getSide().getRotationQuaternion());
         matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0F));
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
         RenderSystem.disableCull();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
         }

         RenderSystem.setShaderTexture(0, HIT_TEXTURE);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         Matrix4f mx = matrices.peek().getPositionMatrix();
         buffer.vertex(mx, -size / 3.0F, size / 3.0F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, 1.0F);
         buffer.vertex(mx, size / 3.0F, size / 3.0F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, 1.0F);
         buffer.vertex(mx, size / 3.0F, -size / 3.0F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, 1.0F);
         buffer.vertex(mx, -size / 3.0F, -size / 3.0F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, 1.0F);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         matrices.pop();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
      } else if (hit.getType() == HitResult.Type.ENTITY) {
         Entity entity = ((EntityHitResult)hit).getEntity();
         Box bBox = entity.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z);
         Matrix4f mx = matrices.peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (drawThroughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
         }

         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         this.drawBoxLines(buffer, mx, bBox, r, g, b, 1.0F);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
      }
   }

   private void drawBoxLines(BufferBuilder buffer, Matrix4f mx, Box box, float r, float g, float b, float a) {
      float x1 = (float)box.minX;
      float y1 = (float)box.minY;
      float z1 = (float)box.minZ;
      float x2 = (float)box.maxX;
      float y2 = (float)box.maxY;
      float z2 = (float)box.maxZ;
      buffer.vertex(mx, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z1).color(r, g, b, a);
      buffer.vertex(mx, x2, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y1, z2).color(r, g, b, a);
      buffer.vertex(mx, x1, y2, z2).color(r, g, b, a);
   }

   private boolean isThrowable(Item item) {
      return item instanceof BowItem
         || item instanceof CrossbowItem
         || item instanceof TridentItem
         || item instanceof EnderPearlItem
         || item instanceof SnowballItem
         || item instanceof EggItem
         || item instanceof SplashPotionItem
         || item instanceof LingeringPotionItem
         || item instanceof ExperienceBottleItem;
   }

   @Environment(EnvType.CLIENT)
   private static class CachedPearl {
      List<Vec3d> path;
      HitResult hitResult;
      int ticksToLand;
      long lastUpdateTime;
   }

   @Environment(EnvType.CLIENT)
   private static class TimerTagInfo {
      Vec3d hitPos;
      int ticksToLand;
      double distance;
      double x;
      double y;

      public TimerTagInfo(Vec3d hitPos, int ticksToLand, double distance, double x, double y) {
         this.hitPos = hitPos;
         this.ticksToLand = ticksToLand;
         this.distance = distance;
         this.x = x;
         this.y = y;
      }
   }
}
