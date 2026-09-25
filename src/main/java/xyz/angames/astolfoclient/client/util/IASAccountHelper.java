package xyz.angames.astolfoclient.client.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASStorage;

@Environment(EnvType.CLIENT)
public class IASAccountHelper {
   public static void onScreenInit(class_437 screen, Consumer<class_4185> buttonAdder) {
      if (screen != null && buttonAdder != null) {
         String className = screen.getClass().getName();
         if (className.contains("ias") && (className.contains("Account") || className.contains("Switcher"))) {
            rebalanceAndAddRandomButton(screen, buttonAdder);
         }
      }
   }

   private static void rebalanceAndAddRandomButton(class_437 screen, Consumer<class_4185> buttonAdder) {
      int targetRowY = screen.field_22790 - 48;
      int btnHeight = 20;
      int btnWidth = 74;
      int gap = 4;
      int startX = screen.field_22789 / 2 - 154;
      List<class_339> topRowWidgets = new ArrayList<>();

      try {
         for (class_364 child : screen.method_25396()) {
            if (child instanceof class_339 widget && widget.method_46427() >= screen.field_22790 - 60 && widget.method_46427() <= screen.field_22790 - 35) {
               topRowWidgets.add(widget);
            }
         }
      } catch (Throwable var11) {
      }

      topRowWidgets.sort(Comparator.comparingInt(class_339::method_46426));
      if (topRowWidgets.size() >= 3) {
         class_339 btn0 = topRowWidgets.get(0);
         btn0.method_46421(startX);
         btn0.method_46419(targetRowY);
         btn0.method_25358(btnWidth);
         class_339 btn1 = topRowWidgets.get(1);
         btn1.method_46421(startX + btnWidth + gap);
         btn1.method_46419(targetRowY);
         btn1.method_25358(btnWidth);
         class_339 btn2 = topRowWidgets.get(2);
         btn2.method_46421(startX + (btnWidth + gap) * 2);
         btn2.method_46419(targetRowY);
         btn2.method_25358(btnWidth);
      }

      int randomBtnX = startX + (btnWidth + gap) * 3;
      int randomBtnY = targetRowY;
      class_4185 randomBtn = class_4185.method_46430(class_2561.method_43470("Random"), btn -> addRandomOfflineAccount(screen))
         .method_46434(randomBtnX, randomBtnY, btnWidth, btnHeight)
         .method_46431();
      buttonAdder.accept(randomBtn);
   }

   public static void addRandomOfflineAccount(class_437 screen) {
      try {
         String randomName = RandomNameGenerator.generateUniqueName();
         OfflineAccount account = new OfflineAccount(randomName, null);
         IASStorage.ACCOUNTS.add(account);
         IAS.disclaimersStorage();
         IAS.saveStorage();

         for (Field field : screen.getClass().getDeclaredFields()) {
            if (field.getType().getName().equals("ru.vidtu.ias.screen.AccountList")) {
               field.setAccessible(true);
               Object listObj = field.get(screen);
               if (listObj != null) {
                  Method updateMethod = listObj.getClass().getDeclaredMethod("update", String.class);
                  updateMethod.setAccessible(true);
                  String searchText = "";

                  for (Field sField : screen.getClass().getDeclaredFields()) {
                     if (class_342.class.isAssignableFrom(sField.getType())) {
                        sField.setAccessible(true);
                        class_342 tf = (class_342)sField.get(screen);
                        if (tf != null) {
                           searchText = tf.method_1882();
                           break;
                        }
                     }
                  }

                  updateMethod.invoke(listObj, searchText);
               }
               break;
            }
         }
      } catch (Throwable t) {
         t.printStackTrace();
      }
   }
}
