package xyz.angames.astolfoclient.client.hud;

import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.SizeState;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5498;
import net.minecraft.class_239.class_240;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.CrosshairModule;

@Environment(EnvType.CLIENT)
public class CrosshairManager {
   private final class_310 client = class_310.method_1551();
   private final Color entityColor = new Color(255, 50, 50);

   public void render(class_332 context) {
      if (this.client.field_1724 != null && this.client.field_1690.method_31044() == class_5498.field_26664) {
         CrosshairModule module = (CrosshairModule)AstolfoclientClient.moduleManager.getModuleByName("Crosshair");
         if (module != null && module.isEnabled()) {
            Matrix4f matrix = context.method_51448().method_23760().method_23761();
            float x = this.client.method_22683().method_4486() / 2.0F;
            float y = this.client.method_22683().method_4502() / 2.0F;
            float gap = module.getGap();
            if (module.hasDynamicGap()) {
               float cooldown = 1.0F - this.client.field_1724.method_7261(0.0F);
               gap += 8.0F * cooldown * cooldown;
            }

            float thickness = module.getThickness();
            float length = module.getLength();
            Color color = module.usesEntityColor() && this.client.field_1765 != null && this.client.field_1765.method_17783() == class_240.field_1331
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
