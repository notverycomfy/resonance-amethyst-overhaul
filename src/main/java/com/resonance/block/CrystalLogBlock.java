package com.resonance.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/** A crystal log/wood pillar that exposes its axe-stripped Fabric state. */
public class CrystalLogBlock extends RotatedPillarBlock {
    @Nullable
    private final Supplier<Block> stripped;

    public CrystalLogBlock(Supplier<Block> stripped, Properties properties) {
        super(properties);
        this.stripped = stripped;
    }

    public CrystalLogBlock(Properties properties) {
        super(properties);
        this.stripped = null;
    }

    @Nullable
    public BlockState getStrippedState(BlockState state) {
        return stripped == null
                ? null
                : stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }
}
