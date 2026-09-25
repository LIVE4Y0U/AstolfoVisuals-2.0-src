package xyz.angames.astolfoclient.client.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public class IASAccountHelper {
   public static void onScreenInit(Screen screen, Consumer<ButtonWidget> buttonAdder) {
      if (screen != null && buttonAdder != null) {
         String className = screen.getClass().getName();
         if (className.contains("ias") && (className.contains("Account") || className.contains("Switcher"))) {
            rebalanceAndAddRandomButton(screen, buttonAdder);
         }
      }
   }

   private static void rebalanceAndAddRandomButton(Screen screen, Consumer<ButtonWidget> buttonAdder) {
      int targetRowY = screen.height - 48;
      int btnHeight = 20;
      int btnWidth = 74;
      int gap = 4;
      int startX = screen.width / 2 - 154;
      List<ClickableWidget> topRowWidgets = new ArrayList<>();

      try {
         for (Element child : screen.children()) {
            if (child instanceof ClickableWidget widget && widget.getY() >= screen.height - 60 && widget.getY() <= screen.height - 35) {
               topRowWidgets.add(widget);
            }
         }
      } catch (Throwable var11) {
      }

      topRowWidgets.sort(Comparator.comparingInt(ClickableWidget::getX));
      if (topRowWidgets.size() >= 3) {
         ClickableWidget btn0 = topRowWidgets.get(0);
         btn0.setX(startX);
         btn0.setY(targetRowY);
         btn0.setWidth(btnWidth);
         ClickableWidget btn1 = topRowWidgets.get(1);
         btn1.setX(startX + btnWidth + gap);
         btn1.setY(targetRowY);
         btn1.setWidth(btnWidth);
         ClickableWidget btn2 = topRowWidgets.get(2);
         btn2.setX(startX + (btnWidth + gap) * 2);
         btn2.setY(targetRowY);
         btn2.setWidth(btnWidth);
      }

      int randomBtnX = startX + (btnWidth + gap) * 3;
      int randomBtnY = targetRowY;
      ButtonWidget randomBtn = ButtonWidget.builder(Text.literal("Random"), btn -> addRandomOfflineAccount(screen))
         .dimensions(randomBtnX, randomBtnY, btnWidth, btnHeight)
         .build();
      buttonAdder.accept(randomBtn);
   }

   public static void addRandomOfflineAccount(Screen screen) {
      try {
         String randomName = RandomNameGenerator.generateUniqueName();

         // IAS — опциональная зависимость. Все обращения рефлексией, чтобы мод
         // компилировался и грузился без установленного IAS.
         Class<?> offlineAccountClass = Class.forName("ru.vidtu.ias.account.OfflineAccount");
         Object account = offlineAccountClass
            .getConstructor(String.class, String.class)
            .newInstance(randomName, null);

         Class<?> storageClass = Class.forName("ru.vidtu.ias.config.IASStorage");
         Object accountsObj = storageClass.getField("ACCOUNTS").get(null);
         if (accountsObj instanceof java.util.List<?> accounts) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> raw = (java.util.List<Object>)accounts;
            raw.add(account);
         }

         Class<?> iasClass = Class.forName("ru.vidtu.ias.IAS");
         iasClass.getMethod("disclaimersStorage").invoke(null);
         iasClass.getMethod("saveStorage").invoke(null);

         for (Field field : screen.getClass().getDeclaredFields()) {
            if (field.getType().getName().equals("ru.vidtu.ias.screen.AccountList")) {
               field.setAccessible(true);
               Object listObj = field.get(screen);
               if (listObj != null) {
                  Method updateMethod = listObj.getClass().getDeclaredMethod("update", String.class);
                  updateMethod.setAccessible(true);
                  String searchText = "";

                  for (Field sField : screen.getClass().getDeclaredFields()) {
                     if (TextFieldWidget.class.isAssignableFrom(sField.getType())) {
                        sField.setAccessible(true);
                        TextFieldWidget tf = (TextFieldWidget)sField.get(screen);
                        if (tf != null) {
                           searchText = tf.getText();
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
