package com.resonance.block;

import com.resonance.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Crystal grass / flowers — grow on crystal grass and the bare End surfaces. */
public class CrystalPlantBlock extends TallGrassBlock {

    public CrystalPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(ModBlocks.CRYSTAL_GRASS_BLOCK.get())
                || state.is(Blocks.END_STONE)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.AMETHYST_BLOCK);
    }
}
