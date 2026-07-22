package com.resonance.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

public class CrystalArmadilloEntity extends Armadillo {

    private int crystalScuteTimer;

    public CrystalArmadilloEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.crystalScuteTimer = 6000 + this.random.nextInt(6000);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return com.resonance.registry.ModEntities.CRYSTAL_ARMADILLO.get()
                .create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean brushOffScute(@Nullable Entity interactingEntity, ItemStack tool) {
        if (this.isBaby()) {
            return false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            this.spawnAtLocation(serverLevel, new ItemStack(com.resonance.registry.ModItems.CRYSTAL_SCUTE.get()));
            this.playSound(SoundEvents.ARMADILLO_BRUSH);
            this.gameEvent(GameEvent.ENTITY_INTERACT);
        }
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isBaby() && --this.crystalScuteTimer <= 0) {
            ItemStack scute = new ItemStack(com.resonance.registry.ModItems.CRYSTAL_SCUTE.get());
            ItemEntity drop = new ItemEntity(this.level(),
                    this.getX(), this.getY(), this.getZ(), scute);
            drop.setDefaultPickUpDelay();
            this.level().addFreshEntity(drop);
            this.playSound(SoundEvents.ARMADILLO_SCUTE_DROP, 1.0F, 1.2F + this.random.nextFloat() * 0.3F);
            this.crystalScuteTimer = 6000 + this.random.nextInt(6000);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(50) == 0) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + 0.35,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0.01, 0);
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
    public float getVoicePitch() {
        return 0.9F + this.random.nextFloat() * 0.3F;
    }
}
