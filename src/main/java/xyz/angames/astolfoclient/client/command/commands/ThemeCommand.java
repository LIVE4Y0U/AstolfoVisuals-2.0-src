package xyz.angames.astolfoclient.client.command.commands;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import xyz.angames.astolfoclient.client.command.Command;
import xyz.angames.astolfoclient.client.config.ThemeManager;

@Environment(EnvType.CLIENT)
public class ThemeCommand extends Command {
   public ThemeCommand() {
      super("theme", "Change client color", "$theme <color | set <#hex>>");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 1) {
         this.sendSyntax();
      } else {
         String action = args[0].toLowerCase();
         switch (action) {
            case "get":
            case "color":
               sendMessage(minecraft.util.Formatting.GOLD + "Current Theme Color: " + minecraft.util.Formatting.WHITE + ThemeManager.getCustomColor1Hex());
               break;
            case "set":
               if (args.length != 2) {
                  this.sendError("Usage: $theme set <#hex>");
                  return;
               }

               try {
                  ThemeManager.setCustomColor(args[1]);
                  sendMessage(minecraft.util.Formatting.GREEN + "Theme color set to " + args[1]);
               } catch (Exception e) {
                  this.sendError("Invalid hex color: " + args[1]);
               }
               break;
            default:
               if (args[0].startsWith("#")) {
                  try {
                     ThemeManager.setCustomColor(args[0]);
                     sendMessage(minecraft.util.Formatting.GREEN + "Theme color set to " + args[0]);
                  } catch (Exception e) {
                     this.sendError("Invalid hex color: " + args[0]);
                  }
               } else {
                  this.sendError("Usage: $theme set <#hex>");
               }
         }
      }
   }

   @Override
   public List<String> suggest(String[] args) {
      return args.length == 1 ? List.of("set", "color") : super.suggest(args);
   }
}
