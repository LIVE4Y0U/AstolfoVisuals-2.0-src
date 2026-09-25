package xyz.angames.astolfoclient.client.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.InterfaceModule;

@Environment(EnvType.CLIENT)
public class HudRenderer {
   private final LogoRenderer logoRenderer = new LogoRenderer();

   public LogoRenderer getLogoRenderer() {
      return this.logoRenderer;
   }

   public void render(class_332 context, float tickDelta) {
      InterfaceModule interfaceMod = (InterfaceModule)AstolfoclientClient.moduleManager.getModuleByName("Interface");
      if (interfaceMod != null && interfaceMod.isEnabled() && interfaceMod.logo.get()) {
         this.logoRenderer.render(context);
      }

      class_310 client = class_310.method_1551();
      Module boyKisserModule = AstolfoclientClient.moduleManager.getModuleByName("BoyKisser");
      if (boyKisserModule != null && boyKisserModule.isEnabled() && client.field_1755 == null) {
         AstolfoclientClient.boyKisserManager.render(context, tickDelta);
      }
   }
}
