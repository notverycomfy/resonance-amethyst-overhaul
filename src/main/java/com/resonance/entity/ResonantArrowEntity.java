package com.resonance.entity;

import com.resonance.Config;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import com.resonance.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ResonantArrowEntity extends AbstractArrow {

    public ResonantArrowEntity(EntityType<? extends ResonantArrowEntity> type, Level level) {
        super(type, level);
    }

    public ResonantArrowEntity(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.RESONANT_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
    }

    public ResonantArrowEntity(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.RESONANT_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && !this.isInGround()) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), this.getEffectSource());
        if (this.level() instanceof ServerLevel level) {
            spawnImpactEffects(level, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            LivingEntity shooter = this.getOwner() instanceof LivingEntity owner ? owner : null;
            AABB area = target.getBoundingBox().inflate(3.0);
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != target && e != shooter)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), this.getEffectSource());
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            Vec3 pos = result.getLocation();
            spawnImpactEffects(level, pos.x, pos.y, pos.z);
            LivingEntity shooter = this.getOwner() instanceof LivingEntity owner ? owner : null;
            AABB area = new AABB(pos.x - 3, pos.y - 3, pos.z - 3, pos.x + 3, pos.y + 3, pos.z + 3);
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != shooter)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), this.getEffectSource());
            }
        }
    }

    private void spawnImpactEffects(ServerLevel level, double x, double y, double z) {
        level.playSound(null, x, y, z, ModSounds.RESONANCE_PULSE.get(), SoundSource.PLAYERS, 1.0F, 1.4F);
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
        for (int i = 0; i < 20; i++) {
            double dx = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dy = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dz = level.getRandom().nextDouble() * 2.0 - 1.0;
            Vec3 dir = new Vec3(dx, dy, dz).normalize();
            level.sendParticles(shard, x, y, z, 0, dir.x, dir.y, dir.z, 0.5);
        }
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0, 0, 0, 0.1);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.RESONANT_ARROW.get());
    }
}
