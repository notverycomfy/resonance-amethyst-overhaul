package com.resonance.mixin;

import com.resonance.Config;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModItems;
import com.resonance.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean resonance$heldResonantTotem;

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"))
    private void resonance$captureTotem(net.minecraft.world.damagesource.DamageSource source,
                                        CallbackInfoReturnable<Boolean> callback) {
        LivingEntity self = (LivingEntity) (Object) this;
        resonance$heldResonantTotem = false;
        for (InteractionHand hand : InteractionHand.values()) {
            if (self.getItemInHand(hand).is(ModItems.RESONANT_TOTEM.get())) {
                resonance$heldResonantTotem = true;
                break;
            }
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
    private void resonance$activateTotem(net.minecraft.world.damagesource.DamageSource source,
                                         CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() || !resonance$heldResonantTotem
                || !((Object) this instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = player.getBoundingBox().inflate(8.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player)) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(),
                    Config.RESONANCE_DURATION.getAsInt() * 2, 2), player);
        }
        level.playSound(null, player.blockPosition(), ModSounds.RESONANCE_PULSE.get(),
                SoundSource.PLAYERS, 1.5F, 0.6F);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                30, 2.0, 1.0, 2.0, 0.08);
    }

    @ModifyVariable(method = "calculateFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double resonance$reduceFallDistance(double distance) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack boots = self.getItemBySlot(EquipmentSlot.FEET);
        return boots.is(ModItems.RESONANT_BOOTS.get()) ? Math.max(0.0, distance - 2.0) : distance;
    }
}
