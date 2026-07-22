package com.resonance.block;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/** A crystal log/wood pillar that strips to its stripped counterpart with an axe. */
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

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP && stripped != null) {
            return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
