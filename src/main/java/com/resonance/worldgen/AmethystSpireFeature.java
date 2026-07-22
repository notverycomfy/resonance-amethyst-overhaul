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

public class AmethystSpireFeature extends Feature<NoneFeatureConfiguration> {

    public AmethystSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState below = level.getBlockState(origin.below());
        if (!isSolid(below)) {
            return false;
        }
        if (ArenaCheck.isNearArena(level, origin, 12)) {
            return false;
        }

        float roll = random.nextFloat();
        int height;
        int baseRadius;
        if (roll < 0.35F) {
            height = 4 + random.nextInt(5);
            baseRadius = 1;
        } else if (roll < 0.65F) {
            height = 12 + random.nextInt(10);
            baseRadius = 1 + random.nextInt(2);
        } else if (roll < 0.88F) {
            height = 25 + random.nextInt(15);
            baseRadius = 2 + random.nextInt(2);
        } else {
            height = 45 + random.nextInt(25);
            baseRadius = 3 + random.nextInt(3);
        }

        float spiralFreq = 0.12F + random.nextFloat() * 0.25F;
        float spiralAmp = baseRadius * (0.2F + random.nextFloat() * 0.5F);
        float spiralPhase = random.nextFloat() * Mth.TWO_PI;
        boolean doubleHelix = random.nextFloat() < 0.25F;

        Set<Long> placedSolid = new HashSet<>();

        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            float taper = 1.0F - t * t;
            float r = Math.max(1.0F, baseRadius * taper);

            float cx = spiralAmp * taper * Mth.sin(y * spiralFreq + spiralPhase);
            float cz = spiralAmp * taper * Mth.cos(y * spiralFreq + spiralPhase);

            placeDisc(level, origin, random, y, cx, cz, r, t, placedSolid);

            if (doubleHelix) {
                float cx2 = -cx * 0.6F;
                float cz2 = -cz * 0.6F;
                placeDisc(level, origin, random, y, cx2, cz2, r * 0.7F, t, placedSolid);
            }

            // Always fill the core column to prevent gaps
            BlockPos corePos = origin.above(y);
            if (level.getBlockState(corePos).isAir() || isSolid(level.getBlockState(corePos))) {
                level.setBlock(corePos, pickSpireBlock(random, t), 2);
                placedSolid.add(corePos.asLong());
            }
        }

        for (int y = 2; y < height; y += 2) {
            float t = (float) y / height;
            float taper = 1.0F - t * t;
            float cx = spiralAmp * taper * Mth.sin(y * spiralFreq + spiralPhase);
            float cz = spiralAmp * taper * Mth.cos(y * spiralFreq + spiralPhase);
            int icx = Math.round(cx);
            int icz = Math.round(cz);
            float r = Math.max(1.0F, baseRadius * taper);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.2F) {
                    BlockPos clusterBase = origin.offset(icx, y, icz).relative(dir, (int) r + 1);
                    BlockPos behind = clusterBase.relative(dir.getOpposite());
                    if (level.getBlockState(clusterBase).isAir() && placedSolid.contains(behind.asLong())) {
                        level.setBlock(clusterBase, pickCluster(random, dir), 2);
                    }
                }
            }
        }

        return true;
    }

    private void placeDisc(WorldGenLevel level, BlockPos origin, RandomSource random,
                           int y, float cx, float cz, float r, float t, Set<Long> placedSolid) {
        int ir = (int) Math.ceil(r);
        for (int x = -ir; x <= ir; x++) {
            for (int z = -ir; z <= ir; z++) {
                float dx = x - cx;
                float dz = z - cz;
                if (dx * dx + dz * dz <= r * r + 0.5F) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir() || isSolid(existing)) {
                        level.setBlock(pos, pickSpireBlock(random, t), 2);
                        placedSolid.add(pos.asLong());
                    }
                }
            }
        }
    }

    private static boolean isSolid(BlockState state) {
        return state.is(Blocks.END_STONE) || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.CALCITE) || state.is(Blocks.BUDDING_AMETHYST);
    }

    private static BlockState pickSpireBlock(RandomSource random, float heightRatio) {
        float val = random.nextFloat();
        if (heightRatio > 0.7F) {
            return val < 0.2F ? Blocks.BUDDING_AMETHYST.defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState();
        }
        if (val < 0.1F) return Blocks.BUDDING_AMETHYST.defaultBlockState();
        if (val < 0.3F) return Blocks.CALCITE.defaultBlockState();
        return Blocks.AMETHYST_BLOCK.defaultBlockState();
    }

    private static BlockState pickCluster(RandomSource random, Direction dir) {
        float val = random.nextFloat();
        if (val < 0.4F) {
            return Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        } else if (val < 0.7F) {
            return Blocks.LARGE_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        } else {
            return Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        }
    }
}
