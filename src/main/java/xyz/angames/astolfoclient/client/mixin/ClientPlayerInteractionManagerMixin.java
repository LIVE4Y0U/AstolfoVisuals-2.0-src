package xyz.angames.astolfoclient.client.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.type.ItemEnchantmentsComponent;
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
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
   @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
   public void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
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
            && target instanceof LivingEntity livingTarget
            && AstolfoclientClient.ragdollRenderer != null) {
            AstolfoclientClient.ragdollRenderer.addRagdoll(livingTarget);
         }
      }

      if (target instanceof FakePlayerEntity fake) {
         fake.handleStatus((byte)2);
         player.resetLastAttackedTicks();
         float cooldownProgress = player.getAttackCooldownProgress(0.5F);
         boolean fullyCharged = cooldownProgress > 0.9F;
         boolean isCrit = fullyCharged
            && !player.isOnGround()
            && !player.isClimbing()
            && !player.isTouchingWater()
            && !player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !player.hasVehicle();
         double baseDamage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
         if (baseDamage <= 1.0 && player.getMainHandStack() != null && !player.getMainHandStack().isEmpty()) {
            String name = player.getMainHandStack().getItem().toString().toLowerCase();
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
         if (player.getMainHandStack() != null && !player.getMainHandStack().isEmpty()) {
            ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(player.getMainHandStack());

            for (Entry<RegistryEntry<Enchantment>> entry : enchants.getEnchantmentEntries()) {
               String id = ((RegistryEntry)entry.getKey()).getKey().map(k -> k.getValue().toString()).orElse("");
               if (id.contains("fire_aspect")) {
                  fireAspectLevel = entry.getIntValue();
                  break;
               }
            }
         }

         if (fireAspectLevel > 0) {
            fake.setOnFireFor(fireAspectLevel * 4);
         }

         boolean isSword = player.getMainHandStack() != null && player.getMainHandStack().getItem() instanceof SwordItem;
         boolean isSweep = fullyCharged && !isCrit && player.isOnGround() && !player.isSprinting() && isSword;
         boolean isKnockback = fullyCharged && player.isSprinting();
         player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
         if (isCrit) {
            MinecraftClient.getInstance().particleManager.addEmitter(target, ParticleTypes.CRIT);
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
         } else if (isSweep) {
            double d = -Math.sin(player.getYaw() * (float) (Math.PI / 180.0));
            double e = Math.cos(player.getYaw() * (float) (Math.PI / 180.0));
            if (player.getWorld() instanceof ClientWorld) {
               player.getWorld()
                  .addParticle(ParticleTypes.SWEEP_ATTACK, target.getX() + d, target.getBodyY(0.5), target.getZ() + e, d, 0.0, e);
            }

            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
            if (player.getWorld() != null) {
               float sweepDamage = 1.0F + 0.5F * (float)baseDamage;

               for (Entity entity : player.getWorld()
                  .getEntitiesByClass(FakePlayerEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0), ent -> ent != target && ent != player)) {
                  entity.handleStatus((byte)2);
                  player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
                  float otherHealth = ((FakePlayerEntity)entity).getHealth() - sweepDamage;
                  if (otherHealth <= 0.0F) {
                     entity.handleStatus((byte)35);
                     MinecraftClient.getInstance().particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                     player.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0F, 1.0F);
                     ((FakePlayerEntity)entity).setHealth(20.0F);
                  } else {
                     ((FakePlayerEntity)entity).setHealth(otherHealth);
                  }
               }
            }
         } else if (isKnockback) {
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
         } else if (fullyCharged) {
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
         } else {
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, 1.0F, 1.0F);
         }

         if (player.getMainHandStack() != null && !player.getMainHandStack().isEmpty() && player.getMainHandStack().hasEnchantments()) {
            MinecraftClient.getInstance().particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
         }

         float newHealth = fake.getHealth() - damage;
         if (newHealth <= 0.0F) {
            fake.handleStatus((byte)35);
            MinecraftClient.getInstance().particleManager.addEmitter(fake, ParticleTypes.TOTEM_OF_UNDYING, 30);
            MinecraftClient.getInstance().gameRenderer.showFloatingItem(new ItemStack(Items.TOTEM_OF_UNDYING));
            player.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0F, 1.0F);
            fake.setHealth(20.0F);
         } else {
            fake.setHealth(newHealth);
         }

         ci.cancel();
      }
   }

   @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
   public void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (entity instanceof FakePlayerEntity) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
   public void onInteractEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (entity instanceof FakePlayerEntity) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }
}
