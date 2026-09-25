package xyz.angames.astolfoclient.client.command.commands;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1297.class_5529;
import xyz.angames.astolfoclient.client.command.Command;
import xyz.angames.astolfoclient.client.util.FakePlayerEntity;

@Environment(EnvType.CLIENT)
public class FakePlayerCommand extends Command {
   public FakePlayerCommand() {
      super("fakeplayer", "Spawns/Despawns a client-side fake player for module testing", "$fakeplayer [spawn <name> | despawn]");
   }

   @Override
   public void execute(String[] args) {
      if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
         this.sendError("You must be in-game to use this command.");
      } else if (args.length == 0) {
         if (FakePlayerEntity.instance != null) {
            this.despawn();
         } else {
            this.spawn("FakePlayer");
         }
      } else {
         String action = args[0].toLowerCase();
         if (action.equals("spawn")) {
            String name = "FakePlayer";
            if (args.length > 1 && !args[1].trim().isEmpty()) {
               name = args[1];
            }

            this.spawn(name);
         } else if (!action.equals("despawn") && !action.equals("remove") && !action.equals("clear") && !action.equals("off")) {
            this.sendError("Unknown action. Usage: " + this.getSyntax());
         } else {
            this.despawn();
         }
      }
   }

   private void spawn(String name) {
      if (FakePlayerEntity.instance != null) {
         this.despawn();
      }

      GameProfile profile = new GameProfile(UUID.randomUUID(), name);
      FakePlayerEntity fakePlayer = new FakePlayerEntity(this.mc.field_1687, profile);
      fakePlayer.method_5719(this.mc.field_1724);
      fakePlayer.field_6241 = this.mc.field_1724.field_6241;
      fakePlayer.field_6283 = this.mc.field_1724.field_6283;
      fakePlayer.method_31548().method_7377(this.mc.field_1724.method_31548());
      int fakeId = -987654;
      fakePlayer.method_5838(fakeId);
      this.mc.field_1687.method_53875(fakePlayer);
      FakePlayerEntity.instance = fakePlayer;
      sendMessage("Spawned fake player: " + class_124.field_1075 + name + class_124.field_1080 + " (ID: " + fakeId + ")");
   }

   private void despawn() {
      if (FakePlayerEntity.instance != null) {
         int id = FakePlayerEntity.instance.method_5628();
         FakePlayerEntity.instance.method_31472();
         this.mc.field_1687.method_2945(id, class_5529.field_26999);
         FakePlayerEntity.instance = null;
         sendMessage("Despawned fake player.");
      } else {
         this.sendError("No fake player currently spawned.");
      }
   }
}
