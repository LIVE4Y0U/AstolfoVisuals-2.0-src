package xyz.angames.astolfoclient.client.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1829;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_2398;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_3966;
import net.minecraft.class_5134;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.RagdollModule;
import xyz.angames.astolfoclient.client.util.FakePlayerEntity;

@Environment(EnvType.CLIENT)
@Mixin(class_636.class)
public class ClientPlayerInteractionManagerMixin {
   @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
   public void onAttackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
      if (AstolfoclientClient.killEffectManager != null && AstolfoclientClient.moduleManager != null) {
         Module mod = AstolfoclientClient.moduleManager.getModuleByName("KillEffect");
         if (mod != null && mod.isEnabled()) {
            AstolfoclientClient.killEffectManager.onAttack(target);
         }
      }

      if (AstolfoclientClient.moduleManager != null) {
         Module ragdollModule = AstolfoclientClient.moduleManager.getModuleByName("Ragdoll");
         if (ragdollModule != null
            && ragdollModule.isEnabled()
            && ((RagdollModule)ragdollModule).hit.get()
            && target instanceof class_1309 livingTarget
            && AstolfoclientClient.ragdollRenderer != null) {
            AstolfoclientClient.ragdollRenderer.addRagdoll(livingTarget);
         }
      }

      if (target instanceof FakePlayerEntity fake) {
         fake.method_5711((byte)2);
         player.method_7350();
         float cooldownProgress = player.method_7261(0.5F);
         boolean fullyCharged = cooldownProgress > 0.9F;
         boolean isCrit = fullyCharged
            && !player.method_24828()
            && !player.method_6101()
            && !player.method_5799()
            && !player.method_6059(class_1294.field_5919)
            && !player.method_5765();
         double baseDamage = player.method_45325(class_5134.field_23721);
         if (baseDamage <= 1.0 && player.method_6047() != null && !player.method_6047().method_7960()) {
            String name = player.method_6047().method_7909().toString().toLowerCase();
            if (name.contains("sword")) {
               if (name.contains("netherite")) {
                  baseDamage = 8.0;
               } else if (name.contains("diamond")) {
                  baseDamage = 7.0;
               } else if (name.contains("iron")) {
                  baseDamage = 6.0;
               } else if (name.contains("stone")) {
                  baseDamage = 5.0;
               } else {
                  baseDamage = 4.0;
               }
            } else if (name.contains("axe")) {
               if (!name.contains("netherite") && !name.contains("diamond") && !name.contains("iron") && !name.contains("stone")) {
                  baseDamage = 7.0;
               } else {
                  baseDamage = 9.0;
               }
            }
         }

         float damage = (float)(baseDamage * (0.2F + cooldownProgress * cooldownProgress * 0.8F));
         if (damage < 1.0F) {
            damage = 1.0F;
         }

         if (isCrit) {
            damage *= 1.5F;
         }

         int fireAspectLevel = 0;
         if (player.method_6047() != null && !player.method_6047().method_7960()) {
            class_9304 enchants = class_1890.method_57532(player.method_6047());

            for (Entry<class_6880<class_1887>> entry : enchants.method_57539()) {
               String id = ((class_6880)entry.getKey()).method_40230().map(k -> k.method_29177().toString()).orElse("");
               if (id.contains("fire_aspect")) {
                  fireAspectLevel = entry.getIntValue();
                  break;
               }
            }
         }

         if (fireAspectLevel > 0) {
            fake.method_5639(fireAspectLevel * 4);
         }

         boolean isSword = player.method_6047() != null && player.method_6047().method_7909() instanceof class_1829;
         boolean isSweep = fullyCharged && !isCrit && player.method_24828() && !player.method_5624() && isSword;
         boolean isKnockback = fullyCharged && player.method_5624();
         player.method_5783(class_3417.field_15115, 1.0F, 1.0F);
         if (isCrit) {
            class_310.method_1551().field_1713.method_3061(target, class_2398.field_11205);
            player.method_5783(class_3417.field_15016, 1.0F, 1.0F);
         } else if (isSweep) {
            double d = -Math.sin(player.method_36454() * (float) (Math.PI / 180.0));
            double e = Math.cos(player.method_36454() * (float) (Math.PI / 180.0));
            if (player.method_37908() instanceof class_638) {
               player.method_37908()
                  .method_8406(class_2398.field_11227, target.method_23317() + d, target.method_23323(0.5), target.method_23321() + e, d, 0.0, e);
            }

            player.method_5783(class_3417.field_14706, 1.0F, 1.0F);
            if (player.method_37908() != null) {
               float sweepDamage = 1.0F + 0.5F * (float)baseDamage;

               for (class_1297 entity : player.method_37908()
                  .method_8390(FakePlayerEntity.class, target.method_5829().method_1009(1.0, 0.25, 1.0), ent -> ent != target && ent != player)) {
                  entity.method_5711((byte)2);
                  player.method_5783(class_3417.field_15115, 1.0F, 1.0F);
                  float otherHealth = ((FakePlayerEntity)entity).method_6032() - sweepDamage;
                  if (otherHealth <= 0.0F) {
                     entity.method_5711((byte)35);
                     class_310.method_1551().field_1713.method_3051(entity, class_2398.field_11220, 30);
                     player.method_5783(class_3417.field_14931, 1.0F, 1.0F);
                     ((FakePlayerEntity)entity).method_6033(20.0F);
                  } else {
                     ((FakePlayerEntity)entity).method_6033(otherHealth);
                  }
               }
            }
         } else if (isKnockback) {
            player.method_5783(class_3417.field_14999, 1.0F, 1.0F);
         } else if (fullyCharged) {
            player.method_5783(class_3417.field_14840, 1.0F, 1.0F);
         } else {
            player.method_5783(class_3417.field_14625, 1.0F, 1.0F);
         }

         if (player.method_6047() != null && !player.method_6047().method_7960() && player.method_6047().method_7942()) {
            class_310.method_1551().field_1713.method_3061(target, class_2398.field_11208);
         }

         float newHealth = fake.method_6032() - damage;
         if (newHealth <= 0.0F) {
            fake.method_5711((byte)35);
            class_310.method_1551().field_1713.method_3051(fake, class_2398.field_11220, 30);
            class_310.method_1551().field_1773.method_3189(new class_1799(class_1802.field_8288));
            player.method_5783(class_3417.field_14931, 1.0F, 1.0F);
            fake.method_6033(20.0F);
         } else {
            fake.method_6033(newHealth);
         }

         ci.cancel();
      }
   }

   @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
   public void onInteractEntity(class_1657 player, class_1297 entity, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      if (entity instanceof FakePlayerEntity) {
         cir.setReturnValue(class_1269.field_5811);
      }
   }

   @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
   public void onInteractEntityAtLocation(class_1657 player, class_1297 entity, class_3966 hitResult, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      if (entity instanceof FakePlayerEntity) {
         cir.setReturnValue(class_1269.field_5811);
      }
   }
}
