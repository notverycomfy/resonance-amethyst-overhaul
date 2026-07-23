package com.resonance.block.entity;

import com.mojang.serialization.Codec;
import com.resonance.block.ChorusResonatorBlock;
import com.resonance.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.resonance.entity.HarmonicAnchorEntity;
import com.resonance.entity.TheHarmonicEntity;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ChorusResonatorBlockEntity extends BlockEntity {

    private static final int PILLAR_SEARCH_RADIUS = 22;
    private static final int PILLAR_MIN_HEIGHT = 8;
    private static final int REQUIRED_PILLARS = 8;
    private static final int TICKS_PER_PILLAR = 25;
    private static final int FINAL_DELAY = 40;

    private boolean active = false;
    private int tickCount = 0;
    private int pillarsLit = 0;
    private final List<BlockPos> pillarPositions = new ArrayList<>();

    // Summoning offerings: one of each shard must be placed on the resonator
    private boolean hasHarmonicFragment = false;
    private boolean hasWhisperFragment = false;
    private boolean hasCrystalShard = false;

    public ChorusResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHORUS_RESONATOR.get(), pos, state);
    }

    public boolean hasHarmonicFragment() { return hasHarmonicFragment; }
    public boolean hasWhisperFragment() { return hasWhisperFragment; }
    public boolean hasCrystalShard() { return hasCrystalShard; }

    public boolean hasAllShards() {
        return hasHarmonicFragment && hasWhisperFragment && hasCrystalShard;
    }

    /** Accepts one of the three summoning shards; returns true if it was placed. */
    public boolean tryInsertShard(Level level, BlockPos pos, ItemStack stack) {
        if (active) return false;

        boolean inserted = false;
        if (stack.is(ModItems.HARMONIC_FRAGMENT.get()) && !hasHarmonicFragment) {
            hasHarmonicFragment = true;
            inserted = true;
        } else if (stack.is(ModItems.WHISPER_FRAGMENT.get()) && !hasWhisperFragment) {
            hasWhisperFragment = true;
            inserted = true;
        } else if (stack.is(ModItems.CRYSTAL_FRAGMENT.get()) && !hasCrystalShard) {
            hasCrystalShard = true;
            inserted = true;
        }

        if (inserted) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5F,
                    0.8F + (countShards() * 0.3F));
            setChanged();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        }
        return inserted;
    }

    private int countShards() {
        return (hasHarmonicFragment ? 1 : 0) + (hasWhisperFragment ? 1 : 0) + (hasCrystalShard ? 1 : 0);
    }

    private void clearShards(Level level, BlockPos pos) {
        hasHarmonicFragment = false;
        hasWhisperFragment = false;
        hasCrystalShard = false;
        setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }

    /** Drops any inserted shards when the block is destroyed. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            if (hasHarmonicFragment) Block.popResource(level, pos, new ItemStack(ModItems.HARMONIC_FRAGMENT.get()));
            if (hasWhisperFragment) Block.popResource(level, pos, new ItemStack(ModItems.WHISPER_FRAGMENT.get()));
            if (hasCrystalShard) Block.popResource(level, pos, new ItemStack(ModItems.CRYSTAL_FRAGMENT.get()));
        }
    }

    public boolean tryActivate(Level level, BlockPos pos, Player player) {
        if (active) return false;

        pillarPositions.clear();
        findPillars(level, pos);

        if (pillarPositions.size() < REQUIRED_PILLARS) return false;

        pillarPositions.sort((a, b) -> {
            double angleA = Mth.atan2(a.getZ() - pos.getZ(), a.getX() - pos.getX());
            double angleB = Mth.atan2(b.getZ() - pos.getZ(), b.getX() - pos.getX());
            return Double.compare(angleA, angleB);
        });

        while (pillarPositions.size() > REQUIRED_PILLARS) {
            pillarPositions.remove(pillarPositions.size() - 1);
        }

        active = true;
        tickCount = 0;
        pillarsLit = 0;

        level.setBlock(pos, level.getBlockState(pos).setValue(ChorusResonatorBlock.ACTIVE, true), 3);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 0.5F);

        setChanged();
        return true;
    }

    private void findPillars(Level level, BlockPos center) {
        for (int dx = -PILLAR_SEARCH_RADIUS; dx <= PILLAR_SEARCH_RADIUS; dx++) {
            for (int dz = -PILLAR_SEARCH_RADIUS; dz <= PILLAR_SEARCH_RADIUS; dz++) {
                if (dx * dx + dz * dz > PILLAR_SEARCH_RADIUS * PILLAR_SEARCH_RADIUS) continue;
                if (dx * dx + dz * dz < 100) continue;

                BlockPos base = new BlockPos(center.getX() + dx, center.getY(), center.getZ() + dz);
                if (isPillarBase(level, base)) {
                    pillarPositions.add(base);
                }
            }
        }
    }

    private boolean isPillarBase(Level level, BlockPos base) {
        int height = 0;
        for (int y = 0; y < 20; y++) {
            if (level.getBlockState(base.above(y)).is(Blocks.AMETHYST_BLOCK)) {
                height++;
            } else if (height > 0) {
                break;
            }
        }
        return height >= PILLAR_MIN_HEIGHT;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChorusResonatorBlockEntity entity) {
        if (!entity.active) {
            // All three shards offered: try to begin the ritual once a second
            if (entity.hasAllShards() && level.getGameTime() % 20 == 0) {
                entity.tryActivate(level, pos, null);
            }
            return;
        }

        entity.tickCount++;

        int phase = Math.min(9, (entity.pillarsLit * 9) / REQUIRED_PILLARS);
        if (state.getValue(ChorusResonatorBlock.PHASE) != phase) {
            level.setBlock(pos, state.setValue(ChorusResonatorBlock.PHASE, phase), 3);
        }

        if (level instanceof ServerLevel serverLevel && entity.tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    2, 0.2, 0.5, 0.2, 0.02);
        }

        if (entity.pillarsLit < REQUIRED_PILLARS) {
            int targetTick = (entity.pillarsLit + 1) * TICKS_PER_PILLAR;
            if (entity.tickCount >= targetTick) {
                entity.lightPillar(level, pos, entity.pillarsLit);
                entity.pillarsLit++;
                entity.setChanged();
            }
            return;
        }

        int summonTick = REQUIRED_PILLARS * TICKS_PER_PILLAR + FINAL_DELAY;
        if (entity.tickCount == summonTick - 20) {
            level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 2.0F, 1.5F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                        50, 0.5, 1.0, 0.5, 0.1);
            }
        }

        if (entity.tickCount >= summonTick) {
            entity.summonBoss(level, pos);
            entity.clearShards(level, pos);
            entity.active = false;
            entity.tickCount = 0;
            entity.pillarsLit = 0;
            level.setBlock(pos, state
                    .setValue(ChorusResonatorBlock.ACTIVE, false)
                    .setValue(ChorusResonatorBlock.PHASE, 0), 3);
            entity.setChanged();
        }
    }

    private void lightPillar(Level level, BlockPos resonatorPos, int pillarIndex) {
        if (pillarIndex >= pillarPositions.size()) return;

        BlockPos pillarBase = pillarPositions.get(pillarIndex);

        int topY = 0;
        for (int y = 19; y >= 0; y--) {
            if (level.getBlockState(pillarBase.above(y)).is(Blocks.AMETHYST_BLOCK)) {
                topY = y;
                break;
            }
        }

        BlockPos topPos = pillarBase.above(topY + 1);
        if (level.getBlockState(topPos).is(Blocks.AMETHYST_CLUSTER)) {
            level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
        }
        if (level instanceof ServerLevel sl) {
            HarmonicAnchorEntity anchor = new HarmonicAnchorEntity(ModEntities.HARMONIC_ANCHOR.get(), sl);
            anchor.setPos(topPos.getX() + 0.5, topPos.getY(), topPos.getZ() + 0.5);
            anchor.setBeamTarget(resonatorPos.above());
            sl.addFreshEntity(anchor);
        }

        if (level instanceof ServerLevel serverLevel) {
            double px = pillarBase.getX() + 0.5;
            double py = pillarBase.getY() + topY + 1.5;
            double pz = pillarBase.getZ() + 0.5;

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    px, py, pz, 15, 0.3, 0.3, 0.3, 0.05);

            double dx = resonatorPos.getX() + 0.5 - px;
            double dy = resonatorPos.getY() + 1.0 - py;
            double dz = resonatorPos.getZ() + 0.5 - pz;
            for (int i = 0; i < 10; i++) {
                double t = i / 10.0;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        px + dx * t, py + dy * t, pz + dz * t,
                        1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        level.playSound(null, pillarBase, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                2.0F, 0.6F + pillarIndex * 0.1F);
    }

    private void summonBoss(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                    100, 2.0, 2.0, 2.0, 0.1);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                    80, 1.0, 1.0, 1.0, 0.2);

            level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.8F);

            TheHarmonicEntity boss = ModEntities.THE_HARMONIC.get().spawn(serverLevel, pos.above(2), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (boss != null) {
                boss.setPos(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
                boss.setPillarPositions(pillarPositions);
                for (net.minecraft.server.level.ServerPlayer player : serverLevel.getEntitiesOfClass(
                        net.minecraft.server.level.ServerPlayer.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(32))) {
                    net.minecraft.advancements.triggers.CriteriaTriggers.SUMMONED_ENTITY.trigger(player, boss);
                }
            }

            // Shatter the ritual anchors and restore the pillar-top clusters
            for (HarmonicAnchorEntity anchor : serverLevel.getEntitiesOfClass(
                    HarmonicAnchorEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(PILLAR_SEARCH_RADIUS + 4))) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        anchor.getX(), anchor.getY() + 0.5, anchor.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                anchor.discard();
            }
            for (BlockPos pillarBase : pillarPositions) {
                for (int y = 19; y >= 0; y--) {
                    BlockPos top = pillarBase.above(y);
                    if (level.getBlockState(top).is(Blocks.AMETHYST_BLOCK)) {
                        BlockPos above = top.above();
                        if (level.getBlockState(above).isAir()) {
                            level.setBlock(above, Blocks.AMETHYST_CLUSTER.defaultBlockState(), 3);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("active", Codec.BOOL, active);
        output.store("tickCount", Codec.INT, tickCount);
        output.store("pillarsLit", Codec.INT, pillarsLit);
        output.store("pillarPositions", BlockPos.CODEC.listOf(), new ArrayList<>(pillarPositions));
        output.store("hasHarmonicFragment", Codec.BOOL, hasHarmonicFragment);
        output.store("hasWhisperFragment", Codec.BOOL, hasWhisperFragment);
        output.store("hasCrystalShard", Codec.BOOL, hasCrystalShard);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.active = input.read("active", Codec.BOOL).orElse(false);
        this.tickCount = input.read("tickCount", Codec.INT).orElse(0);
        this.pillarsLit = input.read("pillarsLit", Codec.INT).orElse(0);
        pillarPositions.clear();
        input.read("pillarPositions", BlockPos.CODEC.listOf()).ifPresent(pillarPositions::addAll);
        this.hasHarmonicFragment = input.read("hasHarmonicFragment", Codec.BOOL).orElse(false);
        this.hasWhisperFragment = input.read("hasWhisperFragment", Codec.BOOL).orElse(false);
        this.hasCrystalShard = input.read("hasCrystalShard", Codec.BOOL).orElse(false);
    }

    // --- Client sync so the floating shards render ---
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
