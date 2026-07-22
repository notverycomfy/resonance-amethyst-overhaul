package com.resonance.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.HashSet;
import java.util.Set;

public class MassiveSpireFeature extends Feature<NoneFeatureConfiguration> {

    public MassiveSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState below = level.getBlockState(origin.below());
        if (!isSolid(below)) return false;
        if (ArenaCheck.isNearArena(level, origin, 20)) return false;

        int height = 55 + random.nextInt(40);
        int baseRadius = 4 + random.nextInt(4);

        float spiralFreq = 0.08F + random.nextFloat() * 0.12F;
        float spiralAmp = baseRadius * (0.3F + random.nextFloat() * 0.5F);
        float spiralPhase = random.nextFloat() * Mth.TWO_PI;
        int helixCount = 1 + random.nextInt(3);

        Set<Long> placedSolid = new HashSet<>();

        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            float taper = (1.0F - t * t) * (1.0F + 0.15F * Mth.sin(y * 0.3F));
            float r = Math.max(1.0F, baseRadius * taper);

            for (int h = 0; h < helixCount; h++) {
                float phase = spiralPhase + h * (Mth.TWO_PI / helixCount);
                float cx = spiralAmp * taper * Mth.sin(y * spiralFreq + phase);
                float cz = spiralAmp * taper * Mth.cos(y * spiralFreq + phase);
                float hr = h == 0 ? r : r * 0.6F;

                int ir = (int) Math.ceil(hr);
                for (int x = -ir; x <= ir; x++) {
                    for (int z = -ir; z <= ir; z++) {
                        float dx = x - cx;
                        float dz = z - cz;
                        if (dx * dx + dz * dz <= hr * hr + 0.5F) {
                            BlockPos pos = origin.offset(x, y, z);
                            BlockState existing = level.getBlockState(pos);
                            if (existing.isAir() || isSolid(existing)) {
                                level.setBlock(pos, pickBlock(random, t), 2);
                                placedSolid.add(pos.asLong());
                            }
                        }
                    }
                }
            }

            // Always fill core column
            BlockPos corePos = origin.above(y);
            if (level.getBlockState(corePos).isAir() || isSolid(level.getBlockState(corePos))) {
                level.setBlock(corePos, pickBlock(random, t), 2);
                placedSolid.add(corePos.asLong());
            }
        }

        for (int y = 3; y < height; y += 3) {
            float t = (float) y / height;
            float taper = 1.0F - t * t;
            float cx = spiralAmp * taper * Mth.sin(y * spiralFreq + spiralPhase);
            float cz = spiralAmp * taper * Mth.cos(y * spiralFreq + spiralPhase);
            int icx = Math.round(cx);
            int icz = Math.round(cz);
            float r = Math.max(1.0F, baseRadius * taper);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.3F) {
                    BlockPos clusterBase = origin.offset(icx, y, icz).relative(dir, (int) r + 1);
                    BlockPos behind = clusterBase.relative(dir.getOpposite());
                    if (level.getBlockState(clusterBase).isAir() && placedSolid.contains(behind.asLong())) {
                        level.setBlock(clusterBase, pickCluster(random, dir), 2);
                    }
                }
            }
            if (random.nextFloat() < 0.15F) {
                BlockPos upPos = origin.offset(icx, y, icz).above((int) r + 1);
                BlockPos belowUp = upPos.below();
                if (level.getBlockState(upPos).isAir() && placedSolid.contains(belowUp.asLong())) {
                    level.setBlock(upPos, pickCluster(random, Direction.UP), 2);
                }
            }
        }

        BlockPos top = origin.above(height);
        BlockPos topBelow = top.below();
        if (level.getBlockState(top).isAir() && placedSolid.contains(topBelow.asLong())) {
            level.setBlock(top, Blocks.AMETHYST_CLUSTER.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
        }

        return true;
    }

    private static boolean isSolid(BlockState state) {
        return state.is(Blocks.END_STONE) || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.CALCITE) || state.is(Blocks.BUDDING_AMETHYST);
    }

    private static BlockState pickBlock(RandomSource random, float t) {
        float val = random.nextFloat();
        if (t > 0.8F) {
            return val < 0.25F ? Blocks.BUDDING_AMETHYST.defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState();
        }
        if (val < 0.08F) return Blocks.BUDDING_AMETHYST.defaultBlockState();
        if (val < 0.2F) return Blocks.CALCITE.defaultBlockState();
        if (val < 0.35F) return Blocks.SMOOTH_BASALT.defaultBlockState();
        return Blocks.AMETHYST_BLOCK.defaultBlockState();
    }

    private static BlockState pickCluster(RandomSource random, Direction dir) {
        float val = random.nextFloat();
        if (val < 0.5F) return Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        if (val < 0.75F) return Blocks.LARGE_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        return Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
    }
}
