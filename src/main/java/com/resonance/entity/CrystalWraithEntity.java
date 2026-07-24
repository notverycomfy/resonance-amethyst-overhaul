package com.resonance.entity;

import com.resonance.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CrystalWraithEntity extends Monster {

    private static final int EMERGENCE_DURATION = 60;
    private static final EntityDataAccessor<Integer> DATA_EMERGENCE_TICKS = SynchedEntityData.defineId(
            CrystalWraithEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ARMOR_BROKEN = SynchedEntityData.defineId(
            CrystalWraithEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float CRYSTAL_ARMOR_MAX = 20.0F;
    private static final int SLAM_COOLDOWN = 80;

    private float crystalArmor = CRYSTAL_ARMOR_MAX;
    private int slamCooldown = SLAM_COOLDOWN;

    public CrystalWraithEntity(EntityType<? extends CrystalWraithEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_EMERGENCE_TICKS, EMERGENCE_DURATION);
        builder.define(DATA_ARMOR_BROKEN, false);
    }

    public boolean isEmerging() {
        return this.entityData.get(DATA_EMERGENCE_TICKS) > 0;
    }

    public float getEmergenceProgress(float partialTicks) {
        int ticks = this.entityData.get(DATA_EMERGENCE_TICKS);
        return 1.0F - Math.min(1.0F, Math.max(0.0F, (ticks - partialTicks) / (float) EMERGENCE_DURATION));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this,
                ShatteredEchoEntity.class, CrystalWraithEntity.class, ResonantStalkerEntity.class,
                CrystalSentinelEntity.class, TheHarmonicEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;

        if (isEmerging()) {
            int ticks = this.entityData.get(DATA_EMERGENCE_TICKS);
            this.entityData.set(DATA_EMERGENCE_TICKS, ticks - 1);
            this.getNavigation().stop();
            this.setTarget(null);
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (ticks == EMERGENCE_DURATION) {
                level.playSound(null, blockPosition(), ModSounds.CRYSTAL_WRAITH_EMERGE.get(), SoundSource.HOSTILE, 1.8F, 0.94F);
            }
            if (ticks % 3 == 0) {
                level.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.15, getZ(),
                        4, getBbWidth() * 0.45, 0.08, getBbWidth() * 0.45, 0.02);
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD),
                        getX(), getY() + 0.1, getZ(), 2, getBbWidth() * 0.4, 0.05, getBbWidth() * 0.4, 0.08);
            }
            if (ticks == 1) {
                level.playSound(null, blockPosition(), ModSounds.CRYSTAL_WRAITH_AMBIENT.get(), SoundSource.HOSTILE, 0.9F, 1.1F);
                level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 18, 0.6, 0.8, 0.6, 0.04);
            }
            return;
        }

        if (slamCooldown > 0) slamCooldown--;

        if (isArmorBroken() && getTarget() != null && getTarget().isAlive()) {
            if (slamCooldown <= 0 && distanceTo(getTarget()) < 4.0) {
                performGroundSlam(level);
                slamCooldown = SLAM_COOLDOWN;
            }
        }

        if (tickCount % 5 == 0) {
            if (!isArmorBroken()) {
                var shimmer = new DustParticleOptions(0xA678F1, 0.8F);
                level.sendParticles(shimmer, getX(), getY() + getBbHeight() * 0.5, getZ(),
                        2, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0.0);
            } else {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() * 0.5, getZ(),
                        1, getBbWidth() * 0.3, getBbHeight() * 0.3, getBbWidth() * 0.3, 0.005);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isEmerging()) return false;
        if (!isArmorBroken()) {
            crystalArmor -= amount;
            var shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
            level.sendParticles(shard, getX(), getY() + getBbHeight() * 0.5, getZ(),
                    6, 0.3, 0.3, 0.3, 0.3);
            playSound(ModSounds.CRYSTAL_WRAITH_HURT.get(), 1.2F, 0.92F + random.nextFloat() * 0.16F);

            if (crystalArmor <= 0) {
                breakCrystalArmor(level);
            }
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    private void breakCrystalArmor(ServerLevel level) {
        this.entityData.set(DATA_ARMOR_BROKEN, true);
        crystalArmor = 0;

        playSound(ModSounds.CRYSTAL_WRAITH_ARMOR_BREAK.get(), 2.0F, 0.95F);

        var shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
        level.sendParticles(shard, getX(), getY() + getBbHeight() * 0.5, getZ(),
                30, 0.5, 0.5, 0.5, 0.5);

        var brightPurple = new DustParticleOptions(0xA678F1, 2.0F);
        level.sendParticles(brightPurple, getX(), getY() + getBbHeight() * 0.5, getZ(),
                20, 0.5, 0.5, 0.5, 0.1);

        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(),
                3, 0.5, 0.3, 0.5, 0.0);

        var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(0.34);
        }
        var damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(10.0);
        }
    }

    private void performGroundSlam(ServerLevel level) {
        playSound(ModSounds.CRYSTAL_WRAITH_ATTACK.get(), 1.6F, 0.9F + random.nextFloat() * 0.12F);

        double cx = getX(), cy = getY(), cz = getZ();

        for (int i = 0; i < 24; i++) {
            double angle = Math.toRadians(i * 15);
            double rx = cx + Math.cos(angle) * 3.0;
            double rz = cz + Math.sin(angle) * 3.0;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, cy + 0.1, rz, 1, 0.0, 0.1, 0.0, 0.01);
        }
        var slamDust = new DustParticleOptions(0x7A5BB5, 1.5F);
        level.sendParticles(slamDust, cx, cy + 0.2, cz, 15, 2.0, 0.1, 2.0, 0.0);

        AABB area = getBoundingBox().inflate(3.5);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            mob.hurtServer(level, level.damageSources().mobAttack(this), 6.0F);
            Vec3 knockDir = mob.position().subtract(this.position()).normalize();
            mob.push(knockDir.x * 1.8, 0.4, knockDir.z * 1.8);
        }
    }

    public boolean isArmorBroken() {
        return this.entityData.get(DATA_ARMOR_BROKEN);
    }

    public float getCrystalArmorPercent() {
        return crystalArmor / CRYSTAL_ARMOR_MAX;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("emergence_ticks", this.entityData.get(DATA_EMERGENCE_TICKS));
        output.putBoolean("armor_broken", isArmorBroken());
        output.putFloat("crystal_armor", this.crystalArmor);
        output.putInt("slam_cooldown", this.slamCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getInt("emergence_ticks").ifPresent(value -> this.entityData.set(DATA_EMERGENCE_TICKS, value));
        this.entityData.set(DATA_ARMOR_BROKEN, input.getBooleanOr("armor_broken", false));
        this.crystalArmor = input.getFloatOr("crystal_armor", CRYSTAL_ARMOR_MAX);
        input.getInt("slam_cooldown").ifPresent(value -> this.slamCooldown = value);
        if (isArmorBroken()) {
            var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) speedAttr.setBaseValue(0.34);
            var damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) damageAttr.setBaseValue(10.0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTAL_WRAITH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTAL_WRAITH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTAL_WRAITH_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.92F + this.random.nextFloat() * 0.16F;
    }
}
