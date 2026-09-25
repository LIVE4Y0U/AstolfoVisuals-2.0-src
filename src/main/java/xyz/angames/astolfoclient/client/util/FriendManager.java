package xyz.angames.astolfoclient.client.util;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FriendManager {
   private static final Set<String> friends = new HashSet<>();

   public static void addFriend(String name) {
      friends.add(name.toLowerCase());
   }

   public static void removeFriend(String name) {
      friends.remove(name.toLowerCase());
   }

   public static boolean isFriend(String name) {
      return name == null ? false : friends.contains(name.toLowerCase());
   }

   public static void clearFriends() {
      friends.clear();
   }

   public static Set<String> getFriends() {
      return friends;
   }
}
