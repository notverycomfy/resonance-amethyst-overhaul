package com.resonance.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CrystalScatterFeature extends Feature<NoneFeatureConfiguration> {

    public CrystalScatterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (ArenaCheck.isNearArena(level, origin, 6)) return false;
        boolean placed = false;

        for (int i = 0; i < 12; i++) {
            BlockPos pos = origin.offset(
                    random.nextInt(8) - 4,
                    random.nextInt(4) - 2,
                    random.nextInt(8) - 4);

            if (!level.getBlockState(pos).isAir()) continue;

            BlockState below = level.getBlockState(pos.below());
            if (below.isSolid()) {
                float roll = random.nextFloat();
                if (roll < 0.35F) {
                    level.setBlock(pos, pickCluster(random, Direction.UP), 2);
                } else if (roll < 0.55F) {
                    level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    if (level.getBlockState(pos.above()).isAir()) {
                        level.setBlock(pos.above(), pickCluster(random, Direction.UP), 2);
                    }
                } else if (roll < 0.7F) {
                    level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    BlockPos above1 = pos.above();
                    if (level.getBlockState(above1).isAir()) {
                        level.setBlock(above1, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                        BlockPos above2 = above1.above();
                        if (level.getBlockState(above2).isAir()) {
                            level.setBlock(above2, pickCluster(random, Direction.UP), 2);
                        }
                    }
                } else {
                    level.setBlock(pos, random.nextFloat() < 0.3F
                            ? Blocks.BUDDING_AMETHYST.defaultBlockState()
                            : Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                }
                placed = true;
            }
        }

        return placed;
    }

    private static BlockState pickCluster(RandomSource random, Direction dir) {
        float val = random.nextFloat();
        if (val < 0.4F) {
            return Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        } else if (val < 0.65F) {
            return Blocks.LARGE_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        } else if (val < 0.85F) {
            return Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
        }
        return Blocks.SMALL_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, dir);
    }
}
