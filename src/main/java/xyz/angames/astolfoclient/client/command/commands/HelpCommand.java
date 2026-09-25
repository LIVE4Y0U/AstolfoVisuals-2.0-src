package xyz.angames.astolfoclient.client.command.commands;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.command.Command;

@Environment(EnvType.CLIENT)
public class HelpCommand extends Command {
   public HelpCommand() {
      super("help", "Lists all commands", "$help");
   }

   @Override
   public void execute(String[] args) {
      sendMessage(minecraft.util.Formatting.BOLD + "--- Available Commands ---");

      for (Command c : AstolfoclientClient.commandManager.getCommands()) {
         sendMessage(minecraft.util.Formatting.AQUA + c.getName() + minecraft.util.Formatting.GRAY + ": " + c.getDescription());
         sendMessage(minecraft.util.Formatting.DARK_GRAY + "  " + c.getSyntax());
      }
   }
}
