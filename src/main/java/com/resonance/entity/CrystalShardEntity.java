package com.resonance.entity;

import com.resonance.registry.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CrystalShardEntity extends AbstractHurtingProjectile implements ItemSupplier {

    private float damage = 6.0F;

    public CrystalShardEntity(EntityType<? extends CrystalShardEntity> type, Level level) {
        super(type, level);
    }

    public CrystalShardEntity(Level level, LivingEntity owner, Vec3 direction) {
        super(ModEntities.CRYSTAL_SHARD.get(), owner, direction, level);
        this.accelerationPower = 0.15;
    }

    public CrystalShardEntity(Level level, LivingEntity owner, Vec3 direction, float damage) {
        this(level, owner, direction);
        this.damage = damage;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(com.resonance.registry.ModItems.CRYSTAL_SHARD_PROJECTILE.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity target = hitResult.getEntity();
            Entity owner = this.getOwner();
            if (target == owner) return;
            target.hurtServer(serverLevel, this.damageSources().mobProjectile(this, owner instanceof LivingEntity le ? le : null), damage);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 1.0F, 1.2F + this.random.nextFloat() * 0.4F);
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD),
                        this.getX(), this.getY(), this.getZ(), 5, 0.1, 0.1, 0.1, 0.15);
            }
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof CrystalShardEntity) return false;
        if (entity instanceof HarmonicAnchorEntity) return false;
        if (isResonanceMob(entity)) return false;
        return super.canHitEntity(entity);
    }

    static boolean isResonanceMob(Entity entity) {
        return entity instanceof TheHarmonicEntity
                || entity instanceof CrystalSentinelEntity
                || entity instanceof ShatteredEchoEntity
                || entity instanceof CrystalWraithEntity
                || entity instanceof ResonantStalkerEntity;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return ParticleTypes.END_ROD;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }
}
