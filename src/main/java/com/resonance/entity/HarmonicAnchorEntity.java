package com.resonance.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class HarmonicAnchorEntity extends Entity implements ItemSupplier {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(
            HarmonicAnchorEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public int time;

    public HarmonicAnchorEntity(EntityType<? extends HarmonicAnchorEntity> type, Level level) {
        super(type, level);
        this.time = this.random.nextInt(100000);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BEAM_TARGET, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();
        this.time++;
        if (this.level().isClientSide() && this.time % 4 == 0) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.6,
                    this.getY() + 0.5 + (this.random.nextDouble() - 0.5) * 0.6,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.6,
                    0, 0.02, 0);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.AMETHYST_CLUSTER);
    }

    public void setBeamTarget(@Nullable BlockPos target) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(target));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return !this.isInvulnerableToBase(source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (source.getEntity() != null && CrystalShardEntity.isResonanceMob(source.getEntity())) return false;

        if (!this.isRemoved()) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
            level.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD),
                    this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.3, 0.3, 0.3, 0.2);
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
            this.remove(Entity.RemovalReason.KILLED);
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.getBeamTarget() != null) {
            output.store("beam_target", BlockPos.CODEC, this.getBeamTarget());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.setBeamTarget(input.read("beam_target", BlockPos.CODEC).orElse(null));
    }
}
