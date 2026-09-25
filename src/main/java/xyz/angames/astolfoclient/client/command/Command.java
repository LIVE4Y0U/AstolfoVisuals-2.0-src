package xyz.angames.astolfoclient.client.command;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public abstract class Command {
   private final String name;
   private final String description;
   private final String syntax;
   private final String[] aliases;
   protected final class_310 mc = class_310.method_1551();

   public Command(String name, String description, String syntax, String... aliases) {
      this.name = name;
      this.description = description;
      this.syntax = syntax;
      this.aliases = aliases;
   }

   public abstract void execute(String[] var1);

   public List<String> suggest(String[] args) {
      return Collections.emptyList();
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getSyntax() {
      return this.syntax;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public static void sendMessage(String message) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1705 != null && mc.field_1705.method_1743() != null) {
         mc.field_1705.method_1743().method_1812(class_2561.method_43470("§d[Astolfo] §7" + message));
      }
   }

   protected void sendError(String message) {
      sendMessage(class_124.field_1061 + message);
   }

   protected void sendSyntax() {
      sendMessage(class_124.field_1061 + "Usage: " + this.getSyntax());
   }
}
