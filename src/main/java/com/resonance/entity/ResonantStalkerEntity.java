package com.resonance.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import com.resonance.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * An invisible predator drawn to Resonance energy. Silently follows the
 * nearest player but only attacks when the player looks toward it.
 */
public class ResonantStalkerEntity extends Monster {

    private static final float AMBUSH_BONUS = 6.0F;
    private static final double LOOK_THRESHOLD = 0.92;
    private boolean hasAmbushed = false;
    private boolean provoked = false;

    public ResonantStalkerEntity(EntityType<? extends ResonantStalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this,
                ShatteredEchoEntity.class, CrystalWraithEntity.class, ResonantStalkerEntity.class,
                CrystalSentinelEntity.class, TheHarmonicEntity.class));
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        if (effect.is(net.minecraft.world.effect.MobEffects.GLOWING)) return false;
        return super.canBeAffected(effect);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (!provoked) {
                Player nearest = this.level().getNearestPlayer(this, 24.0);
                if (nearest != null) {
                    // Follow at a distance — walk toward player but don't set as target
                    double dist = this.distanceTo(nearest);
                    if (dist > 6.0) {
                        this.getNavigation().moveTo(nearest, 0.9);
                    } else if (dist > 3.0) {
                        this.getNavigation().moveTo(nearest, 0.5);
                    }

                    if (isPlayerLookingAtMe(nearest)) {
                        provoked = true;
                        this.setTarget(nearest);
                    }
                }
            }

            if (this.getTarget() == null && this.tickCount > 2400 && this.getRandom().nextInt(200) == 0) {
                this.discard();
            }

            if (this.tickCount % 20 == 0) {
                var dust = new DustParticleOptions(0x5D3A9A, 0.6F);
                serverLevel.sendParticles(dust,
                        this.getX(), this.getY() + 0.1, this.getZ(),
                        1, 0.3, 0.0, 0.3, 0.0);
            }
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 toStalker = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        return lookVec.dot(toStalker) > LOOK_THRESHOLD;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        if (!hasAmbushed && target instanceof LivingEntity living) {
            hasAmbushed = true;
            living.hurtServer(level, this.damageSources().mobAttack(this), AMBUSH_BONUS);
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.STALKER_LAUGH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }
}
