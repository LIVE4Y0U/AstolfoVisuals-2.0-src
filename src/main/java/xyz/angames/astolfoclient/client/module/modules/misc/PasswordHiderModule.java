package xyz.angames.astolfoclient.client.module.modules.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_5250;
import net.minecraft.class_7417;
import net.minecraft.class_8828.class_2585;
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

   public static void setupChatField(class_342 chatField) {
      if (chatField != null) {
         chatField.method_1854((originalStr, firstCharacterIndex) -> {
            if (AstolfoclientClient.moduleManager == null) {
               return class_2561.method_43470(originalStr).method_30937();
            }

            Module mod = AstolfoclientClient.moduleManager.getModuleByName("PasswordHider");
            if (mod != null && mod.isEnabled()) {
               String fullText = chatField.method_1882();
               if (!isPasswordCommand(fullText)) {
                  return class_2561.method_43470(originalStr).method_30937();
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

               return class_2561.method_43470(sb.toString()).method_30937();
            } else {
               return class_2561.method_43470(originalStr).method_30937();
            }
         });
      }
   }

   public static void renderChatFieldOverlay(class_332 context, class_342 chatField) {
   }

   public static class_2561 getProtectedChat(class_2561 message) {
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

   private static class_2561 protectChatTree(class_2561 text) {
      if (text == null) {
         return null;
      }

      class_7417 content = text.method_10851();
      class_7417 newContent = content;
      boolean contentChanged = false;
      if (content instanceof class_2585 literal) {
         String str = literal.comp_737();
         String masked = maskChatString(str);
         if (!masked.equals(str)) {
            newContent = class_2561.method_43470(masked).method_10851();
            contentChanged = true;
         }
      } else if (content instanceof class_2588 trans) {
         Object[] args = trans.method_11023();
         Object[] newArgs = new Object[args.length];
         boolean argsChanged = false;

         for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof class_2561 argText) {
               class_2561 protectedArg = protectChatTree(argText);
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
            newContent = new class_2588(trans.method_11022(), trans.method_48323(), newArgs);
            contentChanged = true;
         }
      }

      List<class_2561> siblings = text.method_10855();
      List<class_2561> newSiblings = new ArrayList<>(siblings.size());
      boolean siblingsChanged = false;

      for (class_2561 sibling : siblings) {
         class_2561 protectedSibling = protectChatTree(sibling);
         newSiblings.add(protectedSibling);
         if (protectedSibling != sibling) {
            siblingsChanged = true;
         }
      }

      if (!contentChanged && !siblingsChanged) {
         return text;
      }

      class_5250 result = class_5250.method_43477(newContent).method_10862(text.method_10866());

      for (class_2561 sibling : newSiblings) {
         result.method_10852(sibling);
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
