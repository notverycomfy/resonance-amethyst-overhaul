package com.resonance.entity;

import com.resonance.registry.ModEffects;
import com.resonance.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jspecify.annotations.Nullable;

public class CrystalSentinelEntity extends Shulker {

    private static final EntityDataAccessor<Integer> DATA_BEAM_TARGET_ID =
            SynchedEntityData.defineId(CrystalSentinelEntity.class, EntityDataSerializers.INT);

    private static final int ATTACK_DURATION = 80;
    private static final float BEAM_DAMAGE = 6.0F;
    private static final double BEAM_RANGE = 16.0;

    private @Nullable LivingEntity clientCachedBeamTarget;
    private int clientBeamTime;

    public CrystalSentinelEntity(EntityType<? extends Shulker> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ARMOR, 10.0);
    }

    public static boolean checkSentinelSpawnRules(EntityType<CrystalSentinelEntity> type,
                                                   ServerLevelAccessor level, EntitySpawnReason spawnReason,
                                                   BlockPos pos, RandomSource random) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isSolid()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BEAM_TARGET_ID, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SentinelBeamGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new SentinelPeekGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this,
                ShatteredEchoEntity.class, CrystalWraithEntity.class, ResonantStalkerEntity.class,
                CrystalSentinelEntity.class, TheHarmonicEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- Beam target (synced for client rendering) ---

    void setBeamTargetId(int entityId) {
        this.entityData.set(DATA_BEAM_TARGET_ID, entityId);
    }

    public boolean hasBeamTarget() {
        return this.entityData.get(DATA_BEAM_TARGET_ID) != 0;
    }

    public @Nullable LivingEntity getBeamTarget() {
        if (!this.hasBeamTarget()) {
            return null;
        }
        if (this.level().isClientSide()) {
            if (this.clientCachedBeamTarget != null) {
                return this.clientCachedBeamTarget;
            }
            Entity entity = this.level().getEntity(this.entityData.get(DATA_BEAM_TARGET_ID));
            if (entity instanceof LivingEntity living) {
                this.clientCachedBeamTarget = living;
                return living;
            }
            return null;
        } else {
            return this.getTarget();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_BEAM_TARGET_ID.equals(accessor)) {
            this.clientBeamTime = 0;
            this.clientCachedBeamTarget = null;
        }
    }

    public int getAttackDuration() {
        return ATTACK_DURATION;
    }

    public float getAttackAnimationScale(float partialTick) {
        return (this.clientBeamTime + partialTick) / this.getAttackDuration();
    }

    public float getClientBeamTime() {
        return this.clientBeamTime;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide() && this.hasBeamTarget()) {
            if (this.clientBeamTime < this.getAttackDuration()) {
                this.clientBeamTime++;
            }

            LivingEntity target = this.getBeamTarget();
            if (target != null) {
                this.getLookControl().setLookAt(target, 90.0F, 90.0F);
                this.getLookControl().tick();
            }
        }

        if (this.hasBeamTarget()) {
            this.setYRot(this.yHeadRot);
        }
    }

    // --- Sounds ---

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getRawPeekAmount() == 0 ? SoundEvents.SHULKER_BULLET_HIT : SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.3F;
    }

    @Override
    protected boolean teleportSomewhere() {
        // Don't teleport or reset during an active beam attack
        if (this.hasBeamTarget()) {
            return false;
        }
        return super.teleportSomewhere();
    }

    @Override
    public void setPos(double x, double y, double z) {
        // Prevent the parent's setPos from resetting peek to 0 during an active attack
        int peekBefore = this.getRawPeekAmount();
        boolean attacking = this.hasBeamTarget();
        super.setPos(x, y, z);
        if (attacking && peekBefore > 0) {
            this.entityData.set(DATA_PEEK_ID, (byte) peekBefore);
        }
    }

    public int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID);
    }

    public void setRawPeekAmount(int amount) {
        this.entityData.set(DATA_PEEK_ID, (byte) amount);
    }

    // --- Beam Attack Goal (guardian-style) ---

    static class SentinelBeamGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final CrystalSentinelEntity sentinel;
        private int attackTime;

        SentinelBeamGoal(CrystalSentinelEntity sentinel) {
            this.sentinel = sentinel;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = sentinel.getTarget();
            return target != null && target.isAlive()
                    && sentinel.distanceToSqr(target) <= BEAM_RANGE * BEAM_RANGE
                    && sentinel.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = sentinel.getTarget();
            return target != null && target.isAlive()
                    && sentinel.distanceToSqr(target) <= BEAM_RANGE * BEAM_RANGE;
        }

        @Override
        public void start() {
            attackTime = -10;
            sentinel.setRawPeekAmount(100);
            LivingEntity target = sentinel.getTarget();
            if (target != null) {
                sentinel.getLookControl().setLookAt(target, 90.0F, 90.0F);
            }
        }

        @Override
        public void stop() {
            sentinel.setBeamTargetId(0);
            sentinel.setRawPeekAmount(0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = sentinel.getTarget();
            if (target != null) {
                sentinel.getLookControl().setLookAt(target, 90.0F, 90.0F);

                if (!sentinel.hasLineOfSight(target)) {
                    sentinel.setBeamTargetId(0);
                    sentinel.setTarget(null);
                    return;
                }

                // Keep peek open during attack
                if (sentinel.getRawPeekAmount() < 100) {
                    sentinel.setRawPeekAmount(100);
                }

                attackTime++;
                if (attackTime == 0) {
                    sentinel.setBeamTargetId(target.getId());
                    sentinel.playSound(SoundEvents.GUARDIAN_ATTACK, 1.0F, 1.2F);
                } else if (attackTime >= sentinel.getAttackDuration()) {
                    if (sentinel.level() instanceof ServerLevel serverLevel) {
                        target.hurtServer(serverLevel, serverLevel.damageSources().indirectMagic(sentinel, sentinel), BEAM_DAMAGE);
                        target.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 1), sentinel);
                    }
                    sentinel.setBeamTargetId(0);
                    attackTime = -10;
                }

                super.tick();
            }
        }
    }

    // --- Peek Goal ---

    static class SentinelPeekGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final CrystalSentinelEntity sentinel;
        private int peekTime;

        SentinelPeekGoal(CrystalSentinelEntity sentinel) {
            this.sentinel = sentinel;
        }

        @Override
        public boolean canUse() {
            return sentinel.getTarget() == null && sentinel.getRandom().nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return peekTime > 0 && sentinel.getTarget() == null;
        }

        @Override
        public void start() {
            peekTime = 20 + sentinel.getRandom().nextInt(40);
            sentinel.setRawPeekAmount(30);
        }

        @Override
        public void stop() {
            if (sentinel.getTarget() == null) {
                sentinel.setRawPeekAmount(0);
            }
        }

        @Override
        public void tick() {
            peekTime--;
        }
    }
}
