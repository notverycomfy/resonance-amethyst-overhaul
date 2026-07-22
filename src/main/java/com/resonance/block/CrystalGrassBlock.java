package com.resonance.block;

import com.resonance.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.tags.FluidTags;

public class CrystalGrassBlock extends GrassBlock {
    public CrystalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static boolean canStayAlive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(Blocks.SNOW) && above.getValue(SnowLayerBlock.LAYERS) == 1) return true;
        if (above.getFluidState().isFull()) return false;
        return LightEngine.getLightBlockInto(state, above, Direction.UP, above.getLightDampening()) < 15;
    }

    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        return canStayAlive(state, level, pos) && !level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayAlive(state, level, pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.CRYSTAL_DIRT.get().defaultBlockState());
        } else if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (level.getBlockState(target).is(ModBlocks.CRYSTAL_DIRT.get()) && canPropagate(state, level, target)) {
                    level.setBlockAndUpdate(target, defaultBlockState().setValue(SNOWY, isSnowySetting(level.getBlockState(target.above()))));
                }
            }
        }
    }
}
