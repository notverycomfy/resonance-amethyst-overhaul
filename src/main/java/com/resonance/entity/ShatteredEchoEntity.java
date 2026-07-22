package com.resonance.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * A translucent amethyst humanoid that haunts geodes. Fights in melee but
 * periodically charges up a sonic burst fired at distant targets.
 */
public class ShatteredEchoEntity extends Monster {

    private static final int BURST_COOLDOWN = 100;
    private static final int BURST_CHARGE_TIME = 20;
    private static final float BURST_DAMAGE = 6.0F;
    private static final double BURST_MAX_RANGE = 12.0;
    private static final double BURST_MIN_RANGE = 3.0;

    private int burstCooldown = BURST_COOLDOWN;
    private int burstCharge = 0;

    public ShatteredEchoEntity(EntityType<? extends ShatteredEchoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
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

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.burstCharge = 0;
            return;
        }

        double dist = this.distanceTo(target);
        boolean inBand = dist >= BURST_MIN_RANGE && dist <= BURST_MAX_RANGE && this.hasLineOfSight(target);

        if (this.burstCooldown > 0) {
            this.burstCooldown--;
            return;
        }

        if (!inBand) {
            this.burstCharge = 0;
            return;
        }

        if (this.burstCharge == 0) {
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.5F, 1.6F);
        }
        this.burstCharge++;

        // Charging shimmer around the head
        if (this.burstCharge % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getEyeY(), this.getZ(),
                    2, 0.3, 0.3, 0.3, 0.02);
        }

        if (this.burstCharge >= BURST_CHARGE_TIME) {
            this.burstCharge = 0;
            this.burstCooldown = BURST_COOLDOWN;
            fireSonicBurst(level, target);
        }
    }

    private void fireSonicBurst(ServerLevel level, LivingEntity target) {
        Vec3 source = this.getEyePosition();
        Vec3 delta = target.getEyePosition().subtract(source);
        Vec3 dir = delta.normalize();

        var purpleDust = new DustParticleOptions(0x8D6ACC, 1.5F);
        int steps = Mth.floor(delta.length()) + 2;
        for (int i = 1; i < steps; i++) {
            Vec3 particlePos = source.add(dir.scale(i));
            level.sendParticles(purpleDust, particlePos.x, particlePos.y, particlePos.z, 4, 0.15, 0.15, 0.15, 0.0);
            level.sendParticles(ParticleTypes.WITCH, particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.0);
        }

        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 1.4F);
        if (target.hurtServer(level, level.damageSources().sonicBoom(this), BURST_DAMAGE)) {
            double vertical = 0.4 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double horizontal = 1.6 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            target.push(dir.x() * horizontal, dir.y() * vertical, dir.z() * horizontal);
        }
    }

    /** Geode-only natural spawns: standard monster rules plus nearby amethyst. */
    public static boolean checkShatteredEchoSpawnRules(
            EntityType<ShatteredEchoEntity> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if (EntitySpawnReason.isSpawner(spawnReason)) {
            return checkMonsterSpawnRules(type, level, spawnReason, pos, random);
        }
        return checkMonsterSpawnRules(type, level, spawnReason, pos, random) && isNearAmethyst(level, pos);
    }

    private static boolean isNearAmethyst(ServerLevelAccessor level, BlockPos pos) {
        boolean hasAmethyst = false;
        boolean hasShell = false;
        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-6, -4, -6), pos.offset(6, 4, 6))) {
            var state = level.getBlockState(check);
            if (state.is(Blocks.BUDDING_AMETHYST) || state.is(Blocks.AMETHYST_BLOCK)) {
                hasAmethyst = true;
            } else if (state.is(Blocks.CALCITE) || state.is(Blocks.SMOOTH_BASALT)) {
                hasShell = true;
            }
            if (hasAmethyst && hasShell) return true;
        }
        return false;
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
        return 0.7F + this.random.nextFloat() * 0.2F;
    }
}
