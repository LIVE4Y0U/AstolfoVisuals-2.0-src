package xyz.angames.astolfoclient.client.hud;

import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.SizeState;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.hit.HitResult.Type;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.CrosshairModule;

@Environment(EnvType.CLIENT)
public class CrosshairManager {
   private final MinecraftClient client = MinecraftClient.getInstance();
   private final Color entityColor = new Color(255, 50, 50);

   public void render(DrawContext context) {
      if (this.client.player != null && this.client.options.getPerspective() == Perspective.FIRST_PERSON) {
         CrosshairModule module = (CrosshairModule)AstolfoclientClient.moduleManager.getModuleByName("Crosshair");
         if (module != null && module.isEnabled()) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            float x = this.client.getWindow().getScaledWidth() / 2.0F;
            float y = this.client.getWindow().getScaledHeight() / 2.0F;
            float gap = module.getGap();
            if (module.hasDynamicGap()) {
               float cooldown = 1.0F - this.client.player.getAttackCooldownProgress(0.0F);
               gap += 8.0F * cooldown * cooldown;
            }

            float thickness = module.getThickness();
            float length = module.getLength();
            Color color = module.usesEntityColor() && this.client.crosshairTarget != null && this.client.crosshairTarget.getType() == HitResult.Type.ENTITY
               ? this.entityColor
               : Color.WHITE;
            Builder.rectangle()
               .size(new SizeState(thickness, length))
               .color(new QuadColorState(color))
               .build()
               .render(matrix, x - thickness / 2.0F, y - gap - length);
            Builder.rectangle().size(new SizeState(thickness, length)).color(new QuadColorState(color)).build().render(matrix, x - thickness / 2.0F, y + gap);
            Builder.rectangle()
               .size(new SizeState(length, thickness))
               .color(new QuadColorState(color))
               .build()
               .render(matrix, x - gap - length, y - thickness / 2.0F);
            Builder.rectangle().size(new SizeState(length, thickness)).color(new QuadColorState(color)).build().render(matrix, x + gap, y - thickness / 2.0F);
         }
      }
   }
}
