package xyz.angames.astolfoclient.client.hud;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.QuadRadiusState;
import dev.sxmurxy.mre.builders.states.SizeState;
import dev.sxmurxy.mre.msdf.MsdfFont;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class ScoreboardManager {
   public float x = 0.0F;
   public float y = 0.0F;
   private final minecraft.client.MinecraftClient client = minecraft.client.MinecraftClient.getInstance();
   private static final Supplier<MsdfFont> BOLD_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("bold").data("bold").build());
   private static final Supplier<MsdfFont> SEMIBOLD_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("semibold").data("semibold").build());
   private static final Supplier<MsdfFont> MEDIUM_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("medium").data("medium").build());

   public void render(client.gui.DrawContext context) {
      Module module = AstolfoclientClient.moduleManager.getModuleByName("Scoreboard");
      if (module != null && module.isEnabled()) {
         if (this.client.world != null && this.client.player != null) {
            minecraft.scoreboard.Scoreboard scoreboard = this.client.world.getScoreboard();
            minecraft.scoreboard.ScoreboardObjective objective = scoreboard.getObjectiveForSlot(minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
            if (objective != null) {
               List<String> lines = new ArrayList<>();
               Collection<minecraft.scoreboard.ScoreboardEntry> scores = scoreboard.getScoreboardEntries(objective);
               List<minecraft.scoreboard.ScoreboardEntry> list = scores.stream()
                  .filter(score -> score.comp_2127() != null && !score.comp_2127().startsWith("#"))
                  .sorted((s1, s2) -> Integer.compare(s2.comp_2128(), s1.comp_2128()))
                  .limit(15L)
                  .collect(Collectors.toList());
               String title = objective.getDisplayName().getString();

               for (minecraft.scoreboard.ScoreboardEntry score : list) {
                  minecraft.scoreboard.Team team = scoreboard.getScoreHolderTeam(score.comp_2127());
                  minecraft.text.Text text = minecraft.scoreboard.Team.decorateName(team, minecraft.text.Text.literal(score.comp_2127()));
                  lines.add(text.getString());
               }

               MsdfFont bold = (MsdfFont)BOLD_FONT.get();
               MsdfFont semibold = (MsdfFont)SEMIBOLD_FONT.get();
               MsdfFont medium = (MsdfFont)MEDIUM_FONT.get();
               float titleWidth = bold != null ? bold.getWidth(title, 8.5F) : 40.0F;
               float maxWidth = titleWidth;

               for (String line : lines) {
                  float w = semibold != null ? semibold.getWidth(line, 7.5F) : 30.0F;
                  if (w > maxWidth) {
                     maxWidth = w;
                  }
               }

               float padding = 7.5F;
               float width = maxWidth + padding * 2.0F;
               float headerHeight = 18.0F;
               float lineHeight = 11.0F;
               float totalHeight = headerHeight + lines.size() * lineHeight + 6.0F;
               double currentGuiScale = this.client.getWindow().getScaleFactor();
               if (currentGuiScale <= 0.0) {
                  currentGuiScale = 2.0;
               }

               float scaleModifier = (float)(2.0 / currentGuiScale);
               this.x = this.client.getWindow().getScaledWidth() - width * scaleModifier - 6.0F;
               this.y = (this.client.getWindow().getScaledHeight() - totalHeight * scaleModifier) / 2.0F;
               context.getMatrices().push();
               context.getMatrices().translate(this.x, this.y, 0.0F);
               context.getMatrices().scale(scaleModifier, scaleModifier, 1.0F);
               context.getMatrices().translate(-this.x, -this.y, 0.0F);
               Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
               long now = System.currentTimeMillis();
               new Color(ThemeManager.getThemedColor(now / 10L));
               this.renderShadow(matrix, this.x, this.y, width, totalHeight, 6.5F, 1.0F);
               Builder.rectangle()
                  .size(new SizeState(width, totalHeight))
                  .radius(new QuadRadiusState(6.5F))
                  .color(new QuadColorState(new Color(0, 0, 0, 255)))
                  .build()
                  .render(matrix, this.x, this.y);
               if (bold != null) {
                  float titleX = this.x + width / 2.0F - titleWidth / 2.0F;
                  Builder.text().font(bold).text(title).color(Color.WHITE).size(8.5F).build().render(matrix, titleX, this.y + 4.5F);
               }

               Builder.rectangle()
                  .size(new SizeState(width - 12.0F, 1.0F))
                  .color(new QuadColorState(new Color(25, 25, 30, 200)))
                  .build()
                  .render(matrix, this.x + 6.0F, this.y + 15.5F);
               float currentY = this.y + headerHeight + 2.0F;

               for (String line : lines) {
                  if (semibold != null) {
                     Builder.text().font(semibold).text(line).color(new Color(200, 200, 205)).size(7.5F).build().render(matrix, this.x + padding, currentY);
                  }

                  currentY += lineHeight;
               }

               context.getMatrices().pop();
            }
         }
      }
   }

   private void renderShadow(Matrix4f matrix, float x, float y, float w, float h, float radius, float masterAlpha) {
      int layers = 12;
      float maxSpread = 8.0F;

      for (int i = layers - 1; i >= 0; i--) {
         float progress = (float)i / layers;
         float spread = progress * maxSpread;
         float alphaFactor = (1.0F - progress) * (1.0F - progress);
         int alpha = (int)(102.0F * alphaFactor * masterAlpha);
         if (alpha > 0) {
            Builder.rectangle()
               .size(new SizeState(w + spread * 2.0F, h + spread * 2.0F))
               .radius(new QuadRadiusState(radius + spread))
               .color(new QuadColorState(new Color(0, 0, 0, alpha)))
               .build()
               .render(matrix, x - spread, y - spread + 1.0F);
         }
      }
   }
}
