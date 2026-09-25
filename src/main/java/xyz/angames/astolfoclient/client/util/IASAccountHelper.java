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
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASStorage;

@Environment(EnvType.CLIENT)
public class IASAccountHelper {
   public static void onScreenInit(gui.screen.Screen screen, Consumer<gui.widget.ButtonWidget> buttonAdder) {
      if (screen != null && buttonAdder != null) {
         String className = screen.getClass().getName();
         if (className.contains("ias") && (className.contains("Account") || className.contains("Switcher"))) {
            rebalanceAndAddRandomButton(screen, buttonAdder);
         }
      }
   }

   private static void rebalanceAndAddRandomButton(gui.screen.Screen screen, Consumer<gui.widget.ButtonWidget> buttonAdder) {
      int targetRowY = screen.height - 48;
      int btnHeight = 20;
      int btnWidth = 74;
      int gap = 4;
      int startX = screen.width / 2 - 154;
      List<gui.widget.ClickableWidget> topRowWidgets = new ArrayList<>();

      try {
         for (client.gui.Element child : screen.children()) {
            if (child instanceof gui.widget.ClickableWidget widget && widget.getY() >= screen.height - 60 && widget.getY() <= screen.height - 35) {
               topRowWidgets.add(widget);
            }
         }
      } catch (Throwable var11) {
      }

      topRowWidgets.sort(Comparator.comparingInt(gui.widget.ClickableWidget::getX));
      if (topRowWidgets.size() >= 3) {
         gui.widget.ClickableWidget btn0 = topRowWidgets.get(0);
         btn0.setX(startX);
         btn0.setY(targetRowY);
         btn0.setWidth(btnWidth);
         gui.widget.ClickableWidget btn1 = topRowWidgets.get(1);
         btn1.setX(startX + btnWidth + gap);
         btn1.setY(targetRowY);
         btn1.setWidth(btnWidth);
         gui.widget.ClickableWidget btn2 = topRowWidgets.get(2);
         btn2.setX(startX + (btnWidth + gap) * 2);
         btn2.setY(targetRowY);
         btn2.setWidth(btnWidth);
      }

      int randomBtnX = startX + (btnWidth + gap) * 3;
      int randomBtnY = targetRowY;
      gui.widget.ButtonWidget randomBtn = gui.widget.ButtonWidget.builder(minecraft.text.Text.literal("Random"), btn -> addRandomOfflineAccount(screen))
         .dimensions(randomBtnX, randomBtnY, btnWidth, btnHeight)
         .build();
      buttonAdder.accept(randomBtn);
   }

   public static void addRandomOfflineAccount(gui.screen.Screen screen) {
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
                     if (gui.widget.TextFieldWidget.class.isAssignableFrom(sField.getType())) {
                        sField.setAccessible(true);
                        gui.widget.TextFieldWidget tf = (gui.widget.TextFieldWidget)sField.get(screen);
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
