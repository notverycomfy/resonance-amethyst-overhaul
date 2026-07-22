package com.resonance.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class CrystalRabbitEntity extends Rabbit {

    public CrystalRabbitEntity(EntityType<? extends Rabbit> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable Rabbit getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return com.resonance.registry.ModEntities.CRYSTAL_RABBIT.get()
                .create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(30) == 0) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + 0.25,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.015, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    public float getVoicePitch() {
        return 1.5F + this.random.nextFloat() * 0.4F;
    }
}
