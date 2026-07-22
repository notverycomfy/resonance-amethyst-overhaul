package com.resonance.block;

import com.resonance.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalDirtPathBlock extends DirtPathBlock {
    public CrystalDirtPathBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? Block.pushEntitiesUp(defaultBlockState(), ModBlocks.CRYSTAL_DIRT.get().defaultBlockState(), context.getLevel(), context.getClickedPos())
                : super.getStateForPlacement(context);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlockAndUpdate(pos, Block.pushEntitiesUp(state, ModBlocks.CRYSTAL_DIRT.get().defaultBlockState(), level, pos));
    }
}
