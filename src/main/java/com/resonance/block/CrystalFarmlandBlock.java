package com.resonance.block;

import com.resonance.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CrystalFarmlandBlock extends FarmlandBlock {
    public CrystalFarmlandBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? ModBlocks.CRYSTAL_DIRT.get().defaultBlockState() : super.getStateForPlacement(context);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) turnToCrystalDirt(null, state, level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
            level.setBlockAndUpdate(pos, ModBlocks.CRYSTAL_DIRT.get().defaultBlockState());
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double distance) {
        if (level instanceof ServerLevel server && net.neoforged.neoforge.common.CommonHooks.onFarmlandTrample(
                server, pos, ModBlocks.CRYSTAL_DIRT.get().defaultBlockState(), distance, entity)) {
            turnToCrystalDirt(entity, state, level, pos);
        }
        super.fallOn(level, state, pos, entity, 0.0);
    }

    public static void turnToCrystalDirt(Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState replacement = Block.pushEntitiesUp(state, ModBlocks.CRYSTAL_DIRT.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, replacement);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, replacement));
    }
}
