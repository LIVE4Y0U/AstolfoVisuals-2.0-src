package xyz.angames.astolfoclient.client.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_310;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;

@Environment(EnvType.CLIENT)
@Mixin(class_4717.class)
public abstract class ChatInputSuggestorMixin {
   @Shadow
   private class_342 field_21599;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private ParseResults<class_2172> field_21610;

   @Shadow
   public abstract void method_23937();

   @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
   public void onRefresh(CallbackInfo ci) {
      String text = this.field_21599.method_1882();
      if (text.startsWith("$")) {
         int cursor = this.field_21599.method_1881();
         int lastSpace = text.lastIndexOf(32, cursor - 1);
         int start = lastSpace == -1 ? 1 : lastSpace + 1;
         List<String> suggestionsList = AstolfoclientClient.commandManager.getSuggestions(text);
         SuggestionsBuilder builder = new SuggestionsBuilder(text, start);

         for (String s : suggestionsList) {
            builder.suggest(s);
         }

         this.field_21611 = builder.buildFuture();
         class_310 client = class_310.method_1551();
         if (client.field_1724 != null && client.field_1724.field_3944 != null) {
            CommandDispatcher<class_2172> dummyDispatcher = new CommandDispatcher();
            StringReader reader = new StringReader(text);
            reader.setCursor(text.length());
            CommandContextBuilder<class_2172> contextBuilder = new CommandContextBuilder(
               dummyDispatcher, client.field_1724.field_3944.method_2875(), dummyDispatcher.getRoot(), 0
            );
            this.field_21610 = new ParseResults(contextBuilder, reader, Collections.emptyMap());
         }

         this.method_23937();
         ci.cancel();
      }
   }
}
