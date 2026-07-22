package com.resonance.effect;

import com.resonance.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Resonance: the entity's body vibrates in sympathy with crystal frequencies,
 * amplifying all incoming damage. The damage bonus itself is applied in
 * {@link com.resonance.event.ResonanceEvents}.
 */
public class ResonanceEffect extends MobEffect {
    public ResonanceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplifier) {
        if (Config.SHOW_PARTICLES.getAsBoolean()) {
            level.sendParticles(ParticleTypes.END_ROD,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(),
                    2, mob.getBbWidth() * 0.4, mob.getBbHeight() * 0.3, mob.getBbWidth() * 0.4, 0.005);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 10 == 0;
    }
}
