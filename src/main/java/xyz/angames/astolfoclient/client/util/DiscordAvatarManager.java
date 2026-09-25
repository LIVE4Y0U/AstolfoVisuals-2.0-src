package xyz.angames.astolfoclient.client.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class DiscordAvatarManager {
   private static minecraft.util.Identifier avatarTexture = null;
   private static volatile String lastAvatarUrl = "";
   private static volatile boolean isDownloading = false;

   public static void update(String username, String userId, String avatarHash) {
      if (userId != null && !userId.isEmpty() && avatarHash != null && !avatarHash.isEmpty()) {
         String url = "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png?size=128";
         fetch(url);
      } else if (userId != null && !userId.isEmpty()) {
         try {
            long id = Long.parseLong(userId);
            int index = (int)((id >> 22) % 6L);
            String defaultUrl = "https://cdn.discordapp.com/embed/avatars/" + index + ".png";
            fetch(defaultUrl);
         } catch (Exception var7) {
         }
      }
   }

   public static void fetch(String url) {
      if (url != null && !url.isEmpty() && !url.equals(lastAvatarUrl) && !isDownloading) {
         lastAvatarUrl = url;
         isDownloading = true;
         Thread thread = new Thread(() -> {
            try {
               HttpURLConnection conn = (HttpURLConnection)new URI(url).toURL().openConnection();
               conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)");
               conn.setConnectTimeout(6000);
               conn.setReadTimeout(6000);
               conn.connect();
               if (conn.getResponseCode() == 200) {
                  try (InputStream in = conn.getInputStream()) {
                     client.texture.NativeImage img = client.texture.NativeImage.read(in);
                     if (img != null) {
                        minecraft.client.MinecraftClient.getInstance().execute(() -> {
                           minecraft.util.Identifier id = minecraft.util.Identifier.of("astolfoclient", "discord_avatar_" + System.currentTimeMillis());
                           minecraft.client.MinecraftClient.getInstance().getTextureManager().registerTexture(id, new client.texture.NativeImageBackedTexture(img));
                           avatarTexture = id;
                        });
                     }
                  }
               }
            } catch (Exception var12) {
            } finally {
               isDownloading = false;
            }
         }, "discord-avatar-downloader");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public static minecraft.util.Identifier getAvatarTexture() {
      return avatarTexture;
   }
}
