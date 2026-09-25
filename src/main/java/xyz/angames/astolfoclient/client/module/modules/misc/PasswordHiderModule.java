package xyz.angames.astolfoclient.client.module.modules.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextContent;
import net.minecraft.text.PlainTextContent.Literal;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public class PasswordHiderModule extends Module {
   public static PasswordHiderModule INSTANCE;
   public final BooleanSetting hideInChat = new BooleanSetting("Hide In Chat", true);
   private static final Pattern CHAT_COMMAND_PATTERN = Pattern.compile(
      "(?i)/(?:login|register|reg|changepassword|cp|auth|pin|2fa|l)\\s+([^\\s]+(?:\\s+[^\\s]+)*)"
   );

   public PasswordHiderModule() {
      super("PasswordHider", "Hides passwords typed in chat commands using asterisks", Module.Category.MISC);
      INSTANCE = this;
      this.addSetting(this.hideInChat);
   }

   public static boolean isPasswordCommand(String text) {
      if (text != null && !text.isEmpty()) {
         String trimmed = text.trim();
         if (!trimmed.startsWith("/")) {
            return false;
         }

         int firstSpace = trimmed.indexOf(32);
         if (firstSpace == -1) {
            return false;
         }

         String command = trimmed.substring(1, firstSpace).toLowerCase(Locale.ROOT);
         return isTargetCommand(command);
      } else {
         return false;
      }
   }

   private static boolean isTargetCommand(String cmd) {
      return cmd.equals("l")
         || cmd.equals("login")
         || cmd.equals("reg")
         || cmd.equals("register")
         || cmd.equals("changepassword")
         || cmd.equals("cp")
         || cmd.equals("auth")
         || cmd.equals("pin")
         || cmd.equals("2fa");
   }

   public static String getMaskedText(String text) {
      if (!isPasswordCommand(text)) {
         return text;
      }

      int firstSpace = text.indexOf(32);
      if (firstSpace == -1) {
         return text;
      }

      String prefix = text.substring(0, firstSpace + 1);
      String password = text.substring(firstSpace + 1);
      StringBuilder sb = new StringBuilder(prefix);

      for (int i = 0; i < password.length(); i++) {
         char c = password.charAt(i);
         if (c == ' ') {
            sb.append(' ');
         } else {
            sb.append('*');
         }
      }

      return sb.toString();
   }

   public static void setupChatField(gui.widget.TextFieldWidget chatField) {
      if (chatField != null) {
         chatField.setRenderTextProvider((originalStr, firstCharacterIndex) -> {
            if (AstolfoclientClient.moduleManager == null) {
               return minecraft.text.Text.literal(originalStr).asOrderedText();
            }

            Module mod = AstolfoclientClient.moduleManager.getModuleByName("PasswordHider");
            if (mod != null && mod.isEnabled()) {
               String fullText = chatField.getText();
               if (!isPasswordCommand(fullText)) {
                  return minecraft.text.Text.literal(originalStr).asOrderedText();
               }

               int firstSpace = fullText.indexOf(32);
               int passStart = firstSpace + 1;
               StringBuilder sb = new StringBuilder(originalStr.length());

               for (int j = 0; j < originalStr.length(); j++) {
                  int globalIndex = firstCharacterIndex + j;
                  char c = originalStr.charAt(j);
                  if (globalIndex >= passStart && c != ' ') {
                     sb.append('*');
                  } else {
                     sb.append(c);
                  }
               }

               return minecraft.text.Text.literal(sb.toString()).asOrderedText();
            } else {
               return minecraft.text.Text.literal(originalStr).asOrderedText();
            }
         });
      }
   }

   public static void renderChatFieldOverlay(client.gui.DrawContext context, gui.widget.TextFieldWidget chatField) {
   }

   public static minecraft.text.Text getProtectedChat(minecraft.text.Text message) {
      if (message == null) {
         return null;
      }

      if (AstolfoclientClient.moduleManager == null) {
         return message;
      }

      Module mod = AstolfoclientClient.moduleManager.getModuleByName("PasswordHider");
      if (mod != null && mod.isEnabled()) {
         PasswordHiderModule ph = (PasswordHiderModule)mod;
         if (!ph.hideInChat.get()) {
            return message;
         }

         try {
            return protectChatTree(message);
         } catch (Exception e) {
            return message;
         }
      } else {
         return message;
      }
   }

   private static minecraft.text.Text protectChatTree(minecraft.text.Text text) {
      if (text == null) {
         return null;
      }

      minecraft.text.TextContent content = text.getContent();
      minecraft.text.TextContent newContent = content;
      boolean contentChanged = false;
      if (content instanceof text.PlainTextContent.Literal literal) {
         String str = literal.comp_737();
         String masked = maskChatString(str);
         if (!masked.equals(str)) {
            newContent = minecraft.text.Text.literal(masked).getContent();
            contentChanged = true;
         }
      } else if (content instanceof minecraft.text.TranslatableTextContent trans) {
         Object[] args = trans.getArgs();
         Object[] newArgs = new Object[args.length];
         boolean argsChanged = false;

         for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof minecraft.text.Text argText) {
               minecraft.text.Text protectedArg = protectChatTree(argText);
               newArgs[i] = protectedArg;
               if (protectedArg != argText) {
                  argsChanged = true;
               }
            } else if (arg instanceof String argStr) {
               String masked = maskChatString(argStr);
               newArgs[i] = masked;
               if (!masked.equals(argStr)) {
                  argsChanged = true;
               }
            } else {
               newArgs[i] = arg;
            }
         }

         if (argsChanged) {
            newContent = new minecraft.text.TranslatableTextContent(trans.getKey(), trans.getFallback(), newArgs);
            contentChanged = true;
         }
      }

      List<minecraft.text.Text> siblings = text.getSiblings();
      List<minecraft.text.Text> newSiblings = new ArrayList<>(siblings.size());
      boolean siblingsChanged = false;

      for (minecraft.text.Text sibling : siblings) {
         minecraft.text.Text protectedSibling = protectChatTree(sibling);
         newSiblings.add(protectedSibling);
         if (protectedSibling != sibling) {
            siblingsChanged = true;
         }
      }

      if (!contentChanged && !siblingsChanged) {
         return text;
      }

      minecraft.text.MutableText result = minecraft.text.MutableText.of(newContent).setStyle(text.getStyle());

      for (minecraft.text.Text sibling : newSiblings) {
         result.append(sibling);
      }

      return result;
   }

   public static String maskChatString(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher matcher = CHAT_COMMAND_PATTERN.matcher(input);
         if (!matcher.find()) {
            return input;
         }

         StringBuilder sb = new StringBuilder();
         matcher.reset();

         while (matcher.find()) {
            String fullMatch = matcher.group(0);
            int spaceIdx = fullMatch.indexOf(32);
            if (spaceIdx != -1) {
               String cmdPart = fullMatch.substring(0, spaceIdx + 1);
               String passPart = fullMatch.substring(spaceIdx + 1);
               String[] passWords = passPart.split("\\s+");
               StringBuilder maskedPass = new StringBuilder();

               for (int i = 0; i < passWords.length; i++) {
                  if (i > 0) {
                     maskedPass.append(" ");
                  }

                  maskedPass.append("****");
               }

               matcher.appendReplacement(sb, Matcher.quoteReplacement(cmdPart + maskedPass));
            }
         }

         matcher.appendTail(sb);
         return sb.toString();
      } else {
         return input;
      }
   }
}
