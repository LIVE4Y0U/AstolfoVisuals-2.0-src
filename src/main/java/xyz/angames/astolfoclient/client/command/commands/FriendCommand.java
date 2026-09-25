package xyz.angames.astolfoclient.client.command.commands;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import net.minecraft.client.network.PlayerListEntry;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.command.Command;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "Manage your friends list", "$friend <add | remove | list | clear> [name]");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 1) {
         this.sendSyntax();
      } else {
         String action = args[0].toLowerCase();
         switch (action) {
            case "add":
               if (args.length != 2) {
                  this.sendError("Usage: $friend add <name>");
                  return;
               }

               String nameToAdd = args[1];
               if (FriendManager.isFriend(nameToAdd)) {
                  this.sendError(nameToAdd + " is already in your friends list!");
               } else {
                  FriendManager.addFriend(nameToAdd);
                  AstolfoclientClient.configManager.saveFriends();
                  sendMessage(minecraft.util.Formatting.GREEN + "Added " + minecraft.util.Formatting.AQUA + nameToAdd + minecraft.util.Formatting.GREEN + " to friends list.");
               }
               break;
            case "remove":
               if (args.length != 2) {
                  this.sendError("Usage: $friend remove <name>");
                  return;
               }

               String nameToRemove = args[1];
               if (!FriendManager.isFriend(nameToRemove)) {
                  this.sendError(nameToRemove + " is not in your friends list!");
               } else {
                  FriendManager.removeFriend(nameToRemove);
                  AstolfoclientClient.configManager.saveFriends();
                  sendMessage(minecraft.util.Formatting.RED + "Removed " + minecraft.util.Formatting.AQUA + nameToRemove + minecraft.util.Formatting.RED + " from friends list.");
               }
               break;
            case "list":
               if (FriendManager.getFriends().isEmpty()) {
                  sendMessage(minecraft.util.Formatting.GRAY + "Your friends list is currently empty.");
               } else {
                  sendMessage(minecraft.util.Formatting.GOLD + "--- Friends List ---");

                  for (String friend : FriendManager.getFriends()) {
                     sendMessage(minecraft.util.Formatting.GRAY + "- " + minecraft.util.Formatting.AQUA + friend);
                  }
               }
               break;
            case "clear":
               FriendManager.clearFriends();
               AstolfoclientClient.configManager.saveFriends();
               sendMessage(minecraft.util.Formatting.GREEN + "Cleared all friends from the list.");
               break;
            default:
               this.sendError("Unknown action: " + action);
         }
      }
   }

   @Override
   public List<String> suggest(String[] args) {
      if (args.length == 1) {
         return List.of("add", "remove", "list", "clear");
      }

      if (args.length == 2) {
         if (args[0].equalsIgnoreCase("add")) {
            if (this.mc.getNetworkHandler() != null) {
               return this.mc
                  .getNetworkHandler()
                  .getPlayerList()
                  .stream()
                  .<GameProfile>map(client.network.PlayerListEntry::getProfile)
                  .map(profile -> profile.getName())
                  .filter(name -> !FriendManager.isFriend(name))
                  .collect(Collectors.toList());
            }
         } else if (args[0].equalsIgnoreCase("remove")) {
            return new ArrayList<>(FriendManager.getFriends());
         }
      }

      return super.suggest(args);
   }
}
