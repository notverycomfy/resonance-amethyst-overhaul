package com.resonance.entity;

import com.mojang.serialization.Codec;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TheHarmonicEntity extends Mob {

    /** The model floats above its logical arena anchor instead of touching it. */
    private static final double HITBOX_Y_OFFSET = 1.5;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(TheHarmonicEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(TheHarmonicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ACTIVE_BEAMS =
            SynchedEntityData.defineId(TheHarmonicEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent;

    private int attackCooldown = 60;
    private int shieldRegenTimer = 0;
    private int transitionTimer = 0;
    private int pendingPhase = 0;
    private int shockwaveTimer = 0;
    private int spikeTimer = 0;
    private int phase4AttackCount = 0;
    private int sentinelSpawnTimer = 0;
    private static final int TRANSITION_DURATION = 60;
    private Vec3 anchorPos;

    private final List<BlockPos> pillarPositions = new ArrayList<>();
    private final List<UUID> spawnedMinions = new ArrayList<>();
    private final List<UUID> anchorUUIDs = new ArrayList<>();
    private final List<UUID> phase4Sentinels = new ArrayList<>();
    private int activeBeams = 0;
    private int noPlayerTicks = 0;

    private boolean shockwaveActive = false;
    private float shockwaveRadius = 0.0F;
    private final Set<UUID> shockwaveHitPlayers = new HashSet<>();

    private boolean spikeActive = false;
    private int spikeAnimTimer = 0;
    private final List<BlockPos> spikeCenters = new ArrayList<>();

    private int rainTimer = -1;
    private final List<Vec3> rainZones = new ArrayList<>();

    // Delays minion/anchor liveness checks after world load so tracked
    // entities in not-yet-loaded chunks aren't mistaken for dead
    private int loadGraceTicks = 0;

    public TheHarmonicEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.bossEvent = level instanceof ServerLevel
                ? new ServerBossEvent(UUID.randomUUID(),
                        Component.translatable("entity.resonance.the_harmonic"),
                        BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10)
                : null;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        return super.makeBoundingBox(position.add(0.0, HITBOX_Y_OFFSET, 0.0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
        builder.define(DATA_SHIELDED, true);
        builder.define(DATA_ACTIVE_BEAMS, 0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public void setPillarPositions(List<BlockPos> positions) {
        pillarPositions.clear();
        pillarPositions.addAll(positions);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    private void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
    }

    public boolean isShielded() {
        return this.entityData.get(DATA_SHIELDED);
    }

    private void setShielded(boolean shielded) {
        this.entityData.set(DATA_SHIELDED, shielded);
    }

    public int getActiveBeams() {
        return this.entityData.get(DATA_ACTIVE_BEAMS);
    }

    private void setActiveBeams(int beams) {
        this.entityData.set(DATA_ACTIVE_BEAMS, beams);
        this.activeBeams = beams;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (anchorPos != null) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(anchorPos.x, anchorPos.y, anchorPos.z);
        } else if (!this.level().isClientSide()) {
            anchorPos = this.position();
        }

        if (this.level().isClientSide()) {
            clientParticles();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (bossEvent != null) {
            bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        // If everyone runs or dies, the fight is forfeit: the boss despawns
        if (this.tickCount % 20 == 0) {
            if (serverLevel.hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), 48.0)) {
                noPlayerTicks = 0;
            } else {
                noPlayerTicks += 20;
                if (noPlayerTicks >= 200) {
                    despawnFight(serverLevel);
                    return;
                }
            }
        }

        if (transitionTimer > 0) {
            tickTransition(serverLevel);
            return;
        }

        float healthPercent = this.getHealth() / this.getMaxHealth();

        switch (getPhase()) {
            case 1 -> tickPhase1(serverLevel, healthPercent);
            case 2 -> tickPhase2(serverLevel);
            case 3 -> tickPhase3(serverLevel, healthPercent);
            case 4 -> tickPhase4(serverLevel);
        }

        if (!isShielded() && getPhase() != 2 && getPhase() != 4) {
            shieldRegenTimer++;
            if (shieldRegenTimer >= getShieldRegenTime()) {
                setShielded(true);
                shieldRegenTimer = 0;
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 1.5F);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 2.0, this.getZ(), 30, 1.5, 1.5, 1.5, 0.05);
            }
        }
    }

    private int getShieldRegenTime() {
        return switch (getPhase()) {
            case 3 -> 160;
            case 4 -> 200;
            default -> 120;
        };
    }

    // --- Phase 1: Crystal Barrage (100%-70%) ---
    private void tickPhase1(ServerLevel level, float healthPercent) {
        if (healthPercent <= 0.7F) {
            beginTransition(level, 2);
            return;
        }

        attackCooldown--;
        if (attackCooldown <= 0 && !getArenaPlayers(level).isEmpty()) {
            crystalBarrage(level, 3);
            attackCooldown = 40;
        }
    }

    // --- Phase 2: Minion Phase ---
    private void tickPhase2(ServerLevel level) {
        if (loadGraceTicks > 0) {
            loadGraceTicks--;
            return;
        }

        if (spawnedMinions.isEmpty()) {
            spawnMinions(level);
        }

        spawnedMinions.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });

        if (spawnedMinions.isEmpty()) {
            beginTransition(level, 3);
        }
    }

    private void spawnMinions(ServerLevel level) {
        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * Mth.TWO_PI;
            double sx = this.getX() + Mth.cos((float) angle) * 8.0;
            double sz = this.getZ() + Mth.sin((float) angle) * 8.0;
            BlockPos spawnPos = BlockPos.containing(sx, groundHeight(), sz);
            CrystalSentinelEntity sentinel = ModEntities.CRYSTAL_SENTINEL.get().spawn(level, spawnPos, EntitySpawnReason.TRIGGERED);
            if (sentinel != null) {
                sentinel.setPos(sx, groundHeight(), sz);
                spawnedMinions.add(sentinel.getUUID());
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, sx, groundHeight() + 1, sz, 15, 0.3, 0.5, 0.3, 0.1);
            }
        }
        for (int i = 0; i < 3; i++) {
            double angle = (i / 3.0) * Mth.TWO_PI + 0.5;
            double sx = this.getX() + Mth.cos((float) angle) * 6.0;
            double sz = this.getZ() + Mth.sin((float) angle) * 6.0;
            BlockPos spawnPos = BlockPos.containing(sx, groundHeight(), sz);
            ShatteredEchoEntity echo = ModEntities.SHATTERED_ECHO.get().spawn(level, spawnPos, EntitySpawnReason.TRIGGERED);
            if (echo != null) {
                echo.setPos(sx, groundHeight(), sz);
                spawnedMinions.add(echo.getUUID());
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, sx, groundHeight() + 1, sz, 15, 0.3, 0.5, 0.3, 0.1);
            }
        }
        {
            double sx = this.getX() + 5.0;
            double sz = this.getZ() + 5.0;
            BlockPos spawnPos = BlockPos.containing(sx, groundHeight(), sz);
            CrystalWraithEntity wraith = ModEntities.CRYSTAL_WRAITH.get().spawn(level, spawnPos, EntitySpawnReason.TRIGGERED);
            if (wraith != null) {
                wraith.setPos(sx, groundHeight(), sz);
                spawnedMinions.add(wraith.getUUID());
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, sx, groundHeight() + 1, sz, 15, 0.3, 0.5, 0.3, 0.1);
            }
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 1.2F);
    }

    // --- Phase 3: Shatter Storm (70%-40%) ---
    private void tickPhase3(ServerLevel level, float healthPercent) {
        if (healthPercent <= 0.4F) {
            beginTransition(level, 4);
            return;
        }

        attackCooldown--;
        if (attackCooldown <= 0 && !getArenaPlayers(level).isEmpty()) {
            crystalBarrage(level, 5);
            attackCooldown = 30;
        }

        spikeTimer++;
        if (spikeTimer >= 100 && !spikeActive && !getArenaPlayers(level).isEmpty()) {
            spikeTimer = 0;
            startCrystalSpikes(level);
        }

        if (spikeActive) {
            tickCrystalSpikes(level);
        }
    }

    private static final int SPIKE_ANIM_DURATION = 24;

    private void startCrystalSpikes(ServerLevel level) {
        spikeCenters.clear();
        for (Player player : getArenaPlayers(level)) {
            BlockPos center = player.blockPosition();
            if (!spikeCenters.contains(center)) {
                spikeCenters.add(center);
            }
        }
        if (spikeCenters.isEmpty()) {
            return;
        }

        spikeActive = true;
        spikeAnimTimer = 0;

        // Warning rumble before the crystals surface beneath every arena player.
        for (BlockPos center : spikeCenters) {
            level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0F, 0.4F);
        }
    }

    private void tickCrystalSpikes(ServerLevel level) {
        spikeAnimTimer++;

        if (spikeAnimTimer > SPIKE_ANIM_DURATION) {
            spikeActive = false;
            spikeCenters.clear();
            return;
        }

        // Crystals rise out of the ground, peak mid-animation, then sink back in
        float progress = (float) spikeAnimTimer / SPIKE_ANIM_DURATION;
        float height = Mth.sin(progress * Mth.PI) * 2.5F;

        ItemParticleOption shardParticle = new ItemParticleOption(ParticleTypes.ITEM, net.minecraft.world.item.Items.AMETHYST_SHARD);
        for (BlockPos center : spikeCenters) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    double px = center.getX() + 0.5 + dx + (this.random.nextDouble() - 0.5) * 0.4;
                    double pz = center.getZ() + 0.5 + dz + (this.random.nextDouble() - 0.5) * 0.4;
                    // Column of shards from the ground up to the current spike height
                    double columnHeight = height * (0.6 + this.random.nextDouble() * 0.4);
                    for (double y = 0; y <= columnHeight; y += 0.5) {
                        level.sendParticles(shardParticle,
                                px, center.getY() + y, pz, 1, 0.05, 0.05, 0.05, 0.02);
                    }
                }
            }
        }

        // Eruption peak: damage + knockup
        if (spikeAnimTimer == SPIKE_ANIM_DURATION / 2) {
            Set<UUID> hitPlayers = new HashSet<>();
            for (BlockPos center : spikeCenters) {
                level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
                List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(center).inflate(2));
                for (Player player : players) {
                    if (hitPlayers.add(player.getUUID())) {
                        player.push(0, 0.8, 0);
                        player.hurtServer(level, level.damageSources().magic(), 6.0F);
                    }
                }
            }
        }
    }

    // --- Phase 4: Final Phase (40%-0%) ---
    private void tickPhase4(ServerLevel level) {
        if (loadGraceTicks > 0) {
            loadGraceTicks--;
        } else if (activeBeams > 0) {
            checkPillarBeams(level);
        }

        attackCooldown--;

        if (attackCooldown <= 0 && !getArenaPlayers(level).isEmpty()) {
            phase4AttackCount++;
            if (phase4AttackCount % 2 == 0) {
                startCrystalRain(level);
            } else {
                crystalBarrage(level, 5);
            }
            attackCooldown = 40;
        }

        if (rainTimer >= 0) {
            tickCrystalRain(level);
        }

        // Cap of 4 live sentinels; the spawn timer only runs while below cap
        if (loadGraceTicks <= 0) {
            phase4Sentinels.removeIf(uuid -> {
                Entity e = level.getEntity(uuid);
                return e == null || !e.isAlive();
            });
        }
        if (phase4Sentinels.size() < 4) {
            sentinelSpawnTimer++;
            if (sentinelSpawnTimer >= 400) {
                sentinelSpawnTimer = 0;
                spawnPhase4Sentinel(level);
            }
        } else {
            sentinelSpawnTimer = 0;
        }

        shockwaveTimer++;
        if (shockwaveTimer >= 200) {
            shockwaveTimer = 0;
            startShockwave(level);
        }

        if (shockwaveActive) {
            tickShockwave(level);
        }
    }

    private void activatePillarBeams(ServerLevel level) {
        anchorUUIDs.clear();
        BlockPos beamTarget = BlockPos.containing(this.getX(), this.getY() + 3.0, this.getZ());
        for (BlockPos pillarBase : pillarPositions) {
            int topY = findPillarTop(level, pillarBase);
            if (topY >= 0) {
                HarmonicAnchorEntity anchor = new HarmonicAnchorEntity(ModEntities.HARMONIC_ANCHOR.get(), level);
                anchor.setPos(pillarBase.getX() + 0.5, pillarBase.getY() + topY + 1.0, pillarBase.getZ() + 0.5);
                anchor.setBeamTarget(beamTarget);
                level.addFreshEntity(anchor);
                anchorUUIDs.add(anchor.getUUID());

                level.sendParticles(ParticleTypes.END_ROD,
                        pillarBase.getX() + 0.5, pillarBase.getY() + topY + 1.5, pillarBase.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.05);
            }
        }
        setActiveBeams(anchorUUIDs.size());
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private void checkPillarBeams(ServerLevel level) {
        int remaining = 0;
        for (UUID uuid : anchorUUIDs) {
            Entity e = level.getEntity(uuid);
            if (e != null && e.isAlive()) {
                remaining++;
            }
        }

        if (remaining != activeBeams) {
            setActiveBeams(remaining);
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 1.5F);
            level.sendParticles(ParticleTypes.CRIT,
                    this.getX(), this.getY() + 2.0, this.getZ(), 15, 1.0, 1.0, 1.0, 0.1);
        }

        if (remaining <= 0) {
            setActiveBeams(0);
            anchorUUIDs.clear();
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.5F, 1.0F);
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 2.0, this.getZ(), 40, 2.0, 2.0, 2.0, 0.1);
        }
    }

    private int findPillarTop(Level level, BlockPos base) {
        int topY = -1;
        for (int y = 19; y >= 0; y--) {
            if (level.getBlockState(base.above(y)).is(Blocks.AMETHYST_BLOCK)) {
                topY = y;
                break;
            }
        }
        return topY;
    }

    // --- Crystal Rain: telegraphed zones over each player, then falling shards ---
    private static final int RAIN_TELEGRAPH_TICKS = 25;
    private static final int RAIN_DURATION = 50;
    private static final double RAIN_ZONE_RADIUS = 3.0;

    private void startCrystalRain(ServerLevel level) {
        rainZones.clear();
        List<Player> players = getArenaPlayers(level);
        if (players.isEmpty()) return;

        for (Player player : players) {
            rainZones.add(player.position());
        }
        rainTimer = 0;

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0F, 1.6F);
    }

    private void tickCrystalRain(ServerLevel level) {
        rainTimer++;

        if (rainTimer > RAIN_DURATION) {
            rainTimer = -1;
            rainZones.clear();
            return;
        }

        if (rainTimer <= RAIN_TELEGRAPH_TICKS) {
            // Telegraph: a spark ring marks each impact zone on the ground
            for (Vec3 zone : rainZones) {
                int segments = 20;
                for (int i = 0; i < segments; i++) {
                    double angle = (i / (double) segments) * Mth.TWO_PI;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            zone.x + Mth.cos((float) angle) * RAIN_ZONE_RADIUS,
                            zone.y + 0.2,
                            zone.z + Mth.sin((float) angle) * RAIN_ZONE_RADIUS,
                            1, 0.02, 0.02, 0.02, 0.0);
                }
            }
            if (rainTimer == RAIN_TELEGRAPH_TICKS) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
            }
        } else if (rainTimer % 3 == 0) {
            // Rain: shards fall from above into each zone
            for (Vec3 zone : rainZones) {
                double angle = this.random.nextDouble() * Mth.TWO_PI;
                double r = this.random.nextDouble() * RAIN_ZONE_RADIUS;
                double sx = zone.x + Mth.cos((float) angle) * r;
                double sz = zone.z + Mth.sin((float) angle) * r;
                double sy = zone.y + 12.0;

                CrystalShardEntity shard = new CrystalShardEntity(level, this,
                        new Vec3((this.random.nextDouble() - 0.5) * 0.1, -1.0, (this.random.nextDouble() - 0.5) * 0.1), 6.0F);
                shard.setPos(sx, sy, sz);
                level.addFreshEntity(shard);
            }
        }
    }

    private void spawnPhase4Sentinel(ServerLevel level) {
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double sx = this.getX() + Mth.cos((float) angle) * 12.0;
        double sz = this.getZ() + Mth.sin((float) angle) * 12.0;
        BlockPos spawnPos = BlockPos.containing(sx, groundHeight(), sz);
        CrystalSentinelEntity sentinel = ModEntities.CRYSTAL_SENTINEL.get().spawn(level, spawnPos, EntitySpawnReason.TRIGGERED);
        if (sentinel != null) {
            sentinel.setPos(sx, groundHeight(), sz);
            phase4Sentinels.add(sentinel.getUUID());
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, sx, groundHeight() + 1, sz, 15, 0.3, 0.5, 0.3, 0.1);
        }
    }

    private void startShockwave(ServerLevel level) {
        shockwaveActive = true;
        shockwaveRadius = 1.0F;
        shockwaveHitPlayers.clear();

        // Warning cue so the player knows a wave is coming
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.4F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0F, 0.3F);
    }

    private void tickShockwave(ServerLevel level) {
        shockwaveRadius += 0.35F;

        if (shockwaveRadius > 21.0F) {
            shockwaveActive = false;
            return;
        }

        double groundY = groundHeight();

        // Crisp ring on the ground — short-lived particles so no trail is left behind
        int segments = Math.max(16, (int)(shockwaveRadius * 6));
        for (int i = 0; i < segments; i++) {
            double angle = (i / (double) segments) * Mth.TWO_PI;
            double px = this.getX() + Mth.cos((float) angle) * shockwaveRadius;
            double pz = this.getZ() + Mth.sin((float) angle) * shockwaveRadius;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    px, groundY + 0.2, pz, 1, 0.02, 0.02, 0.02, 0.0);
        }

        if ((int)(shockwaveRadius * 2) % 4 == 0) {
            level.playSound(null, this.getX(), groundY, this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.HOSTILE, 1.5F,
                    0.5F + shockwaveRadius * 0.02F);
        }

        // Only players standing on the ground where the ring currently is get hit
        List<Player> players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(22));
        for (Player player : players) {
            if (shockwaveHitPlayers.contains(player.getUUID())) continue;
            if (!player.onGround()) continue;

            double dist = Math.sqrt(
                    Mth.square(player.getX() - this.getX()) + Mth.square(player.getZ() - this.getZ()));
            if (Math.abs(dist - shockwaveRadius) <= 1.0) {
                shockwaveHitPlayers.add(player.getUUID());
                Vec3 dir = player.position().subtract(this.getX(), player.getY(), this.getZ()).normalize();
                player.hurtServer(level, level.damageSources().sonicBoom(this), 12.0F);
                player.addEffect(new MobEffectInstance(ModEffects.RESONANCE, 100, 1), this);
                player.push(dir.x * 2.0, 0.6, dir.z * 2.0);
            }
        }
    }

    private double groundHeight() {
        // Boss floats 2 blocks above the arena floor at its anchor position
        return (anchorPos != null ? anchorPos.y : this.getY()) - 2.0;
    }

    // --- Shared attack ---
    private void crystalBarrage(ServerLevel level, int count) {
        List<Player> targets = getArenaPlayers(level);
        if (targets.isEmpty()) return;

        for (int targetIndex = 0; targetIndex < targets.size(); targetIndex++) {
            Player target = targets.get(targetIndex);
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            for (int i = 0; i < count; i++) {
                double angle = ((i / (double) count) + (targetIndex / (double) targets.size())) * Mth.TWO_PI;
                double spawnX = this.getX() + Mth.cos((float) angle) * 1.5;
                double spawnY = this.getY() + 3.0 + i * 0.3;
                double spawnZ = this.getZ() + Mth.sin((float) angle) * 1.5;
                Vec3 direction = targetPos.subtract(spawnX, spawnY, spawnZ);
                CrystalShardEntity shard = new CrystalShardEntity(level, this, direction, 6.0F);
                shard.setPos(spawnX, spawnY, spawnZ);
                level.addFreshEntity(shard);
            }
        }

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F + count * 0.05F);
    }

    private List<Player> getArenaPlayers(ServerLevel level) {
        return level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24),
                player -> player.isAlive() && !player.isSpectator());
    }

    // --- Phase Transitions ---
    private void beginTransition(ServerLevel level, int newPhase) {
        pendingPhase = newPhase;
        transitionTimer = TRANSITION_DURATION;
        attackCooldown = TRANSITION_DURATION + 40;

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, 0.5F + newPhase * 0.15F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 2.0F, 0.3F + newPhase * 0.2F);

        List<Player> players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(12));
        for (Player player : players) {
            Vec3 dir = player.position().subtract(this.position()).normalize();
            player.push(dir.x * 2.0, 0.8, dir.z * 2.0);
        }
    }

    private void tickTransition(ServerLevel level) {
        transitionTimer--;

        if (transitionTimer % 5 == 0) {
            double radius = 3.0 + (TRANSITION_DURATION - transitionTimer) * 0.05;
            int count = 8 + (TRANSITION_DURATION - transitionTimer) / 4;
            for (int i = 0; i < count; i++) {
                double angle = (i / (double) count) * Mth.TWO_PI + transitionTimer * 0.15;
                double px = this.getX() + Mth.cos((float) angle) * radius;
                double pz = this.getZ() + Mth.sin((float) angle) * radius;
                level.sendParticles(ParticleTypes.END_ROD,
                        px, this.getY() + 1.0 + transitionTimer * 0.04, pz,
                        1, 0.05, 0.1, 0.05, 0.01);
            }
        }

        if (transitionTimer % 10 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    20, 1.5, 1.5, 1.5, 0.15);
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0F,
                    0.4F + (TRANSITION_DURATION - transitionTimer) * 0.02F);
        }

        if (transitionTimer == 10) {
            level.sendParticles(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY() + 2.0, this.getZ(), 8, 2.5, 2.5, 2.5, 0.0);
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
        }

        if (transitionTimer <= 0) {
            setPhase(pendingPhase);
            setShielded(pendingPhase != 2 && pendingPhase != 4);
            shieldRegenTimer = 0;
            transitionTimer = 0;
            spikeTimer = 0;
            spikeActive = false;
            spikeAnimTimer = 0;
            spikeCenters.clear();
            shockwaveTimer = 0;
            phase4AttackCount = 0;
            sentinelSpawnTimer = 0;
            rainTimer = -1;
            rainZones.clear();
            spawnedMinions.clear();

            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.5F, 1.5F);
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 2.0, this.getZ(), 50, 2.0, 2.0, 2.0, 0.08);

            List<Player> players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10));
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(ModEffects.RESONANCE, 60, pendingPhase - 1), this);
            }

            if (pendingPhase == 4) {
                activatePillarBeams(level);
            }
        }
    }

    // --- Damage ---
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Block environmental damage, but let /kill and creative-mode hits through
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (source.getEntity() == null) return false;

        if (transitionTimer > 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 2.0, this.getZ(), 10, 1.0, 1.0, 1.0, 0.5);
            return false;
        }

        if (getPhase() == 2) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 2.0, this.getZ(), 10, 1.0, 1.0, 1.0, 0.5);
            return false;
        }

        if (getPhase() == 4 && activeBeams > 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 2.0, this.getZ(), 10, 1.0, 1.0, 1.0, 0.5);
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.5F);
            return false;
        }

        if (isShielded()) {
            setShielded(false);
            shieldRegenTimer = 0;
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.0F, 1.5F);
            level.sendParticles(ParticleTypes.CRIT,
                    this.getX(), this.getY() + 2.0, this.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
            return false;
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel serverLevel) {
            cleanupAnchors(serverLevel);
            // The Harmonic's death seeds the crystal forest
            com.resonance.data.CrystalForestSpreadData.get(serverLevel).startSpread(this.blockPosition());
        }
    }

    private void cleanupAnchors(ServerLevel level) {
        for (UUID uuid : anchorUUIDs) {
            Entity e = level.getEntity(uuid);
            if (e != null) {
                e.discard();
            }
        }
        anchorUUIDs.clear();
    }

    /** No players left in the arena — the fight is lost and the boss vanishes. */
    private void despawnFight(ServerLevel level) {
        cleanupAnchors(level);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 2.0F, 0.5F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 3.0, this.getZ(), 80, 1.5, 3.0, 1.5, 0.15);
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 3.0, this.getZ(), 40, 1.5, 3.0, 1.5, 0.08);

        this.discard();
    }

    // --- Movement locks ---
    @Override
    public void push(Entity entity) {}

    @Override
    public void push(double x, double y, double z) {}

    @Override
    public boolean isPushable() {
        return false;
    }

    // --- Client particles ---
    private void clientParticles() {
        float phase = getPhase();
        int particleCount = (int)(phase * 2);
        for (int i = 0; i < particleCount; i++) {
            double angle = this.tickCount * 0.05 + i * Mth.TWO_PI / particleCount;
            double radius = 1.5 + 0.5 * Mth.sin(this.tickCount * 0.1F);
            double px = this.getX() + Mth.cos((float) angle) * radius;
            double pz = this.getZ() + Mth.sin((float) angle) * radius;
            double py = this.getY() + 2.0 + 0.5 * Mth.sin(this.tickCount * 0.08F + i);
            this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02, 0);
        }

        if (isShielded()) {
            for (int i = 0; i < 4; i++) {
                double angle = this.tickCount * 0.04 + i * Mth.TWO_PI / 4;
                double r = 2.5 + 0.3 * Mth.sin(this.tickCount * 0.06F);
                double px = this.getX() + Mth.cos((float) angle) * r;
                double pz = this.getZ() + Mth.sin((float) angle) * r;
                this.level().addParticle(ParticleTypes.ENCHANT,
                        px, this.getY() + 0.5 + this.random.nextDouble() * 3.5, pz,
                        0, 0.1, 0);
            }
        }
    }

    // --- Boss bar ---
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (bossEvent != null) bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (bossEvent != null) bossEvent.removePlayer(player);
    }

    // --- Save/Load ---
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Phase", Codec.INT, getPhase());
        output.store("Shielded", Codec.BOOL, isShielded());
        output.store("TransitionTimer", Codec.INT, transitionTimer);
        output.store("PendingPhase", Codec.INT, pendingPhase);
        output.store("ActiveBeams", Codec.INT, activeBeams);
        output.store("AttackCooldown", Codec.INT, attackCooldown);
        output.store("ShieldRegenTimer", Codec.INT, shieldRegenTimer);
        output.store("SentinelSpawnTimer", Codec.INT, sentinelSpawnTimer);
        output.store("Phase4AttackCount", Codec.INT, phase4AttackCount);
        output.store("pillarPositions", BlockPos.CODEC.listOf(), new ArrayList<>(pillarPositions));
        output.store("spawnedMinions", Codec.STRING.listOf(),
                spawnedMinions.stream().map(UUID::toString).toList());
        output.store("anchorUUIDs", Codec.STRING.listOf(),
                anchorUUIDs.stream().map(UUID::toString).toList());
        output.store("phase4Sentinels", Codec.STRING.listOf(),
                phase4Sentinels.stream().map(UUID::toString).toList());
        if (anchorPos != null) {
            output.store("AnchorX", Codec.DOUBLE, anchorPos.x);
            output.store("AnchorY", Codec.DOUBLE, anchorPos.y);
            output.store("AnchorZ", Codec.DOUBLE, anchorPos.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Phase", Codec.INT).ifPresent(this::setPhase);
        input.read("Shielded", Codec.BOOL).ifPresent(this::setShielded);
        this.transitionTimer = input.read("TransitionTimer", Codec.INT).orElse(0);
        this.pendingPhase = input.read("PendingPhase", Codec.INT).orElse(0);
        this.activeBeams = input.read("ActiveBeams", Codec.INT).orElse(0);
        setActiveBeams(this.activeBeams);
        this.attackCooldown = input.read("AttackCooldown", Codec.INT).orElse(60);
        this.shieldRegenTimer = input.read("ShieldRegenTimer", Codec.INT).orElse(0);
        this.sentinelSpawnTimer = input.read("SentinelSpawnTimer", Codec.INT).orElse(0);
        this.phase4AttackCount = input.read("Phase4AttackCount", Codec.INT).orElse(0);
        // Mid-frame area attacks are deliberately canceled on reload; resuming
        // them without their transient hit lists could damage players twice.
        this.shockwaveTimer = 0;
        this.shockwaveActive = false;
        this.shockwaveRadius = 0.0F;
        this.shockwaveHitPlayers.clear();
        this.spikeTimer = 0;
        this.spikeActive = false;
        this.spikeAnimTimer = 0;
        this.spikeCenters.clear();
        this.rainTimer = -1;
        this.rainZones.clear();
        pillarPositions.clear();
        input.read("pillarPositions", BlockPos.CODEC.listOf()).ifPresent(pillarPositions::addAll);
        spawnedMinions.clear();
        input.read("spawnedMinions", Codec.STRING.listOf()).ifPresent(list ->
                list.forEach(s -> spawnedMinions.add(UUID.fromString(s))));
        anchorUUIDs.clear();
        input.read("anchorUUIDs", Codec.STRING.listOf()).ifPresent(list ->
                list.forEach(s -> anchorUUIDs.add(UUID.fromString(s))));
        phase4Sentinels.clear();
        input.read("phase4Sentinels", Codec.STRING.listOf()).ifPresent(list ->
                list.forEach(s -> phase4Sentinels.add(UUID.fromString(s))));
        var ax = input.read("AnchorX", Codec.DOUBLE);
        var ay = input.read("AnchorY", Codec.DOUBLE);
        var az = input.read("AnchorZ", Codec.DOUBLE);
        if (ax.isPresent() && ay.isPresent() && az.isPresent()) {
            anchorPos = new Vec3(ax.get(), ay.get(), az.get());
        }
        this.loadGraceTicks = 100;
    }

    // --- Sounds ---
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_RESONATE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.3F + this.random.nextFloat() * 0.2F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
