package xyz.angames.astolfoclient.client.command.commands;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.command.Command;

@Environment(EnvType.CLIENT)
public class HelpCommand extends Command {
   public HelpCommand() {
      super("help", "Lists all commands", "$help");
   }

   @Override
   public void execute(String[] args) {
      sendMessage(class_124.field_1067 + "--- Available Commands ---");

      for (Command c : AstolfoclientClient.commandManager.getCommands()) {
         sendMessage(class_124.field_1075 + c.getName() + class_124.field_1080 + ": " + c.getDescription());
         sendMessage(class_124.field_1063 + "  " + c.getSyntax());
      }
   }
}
