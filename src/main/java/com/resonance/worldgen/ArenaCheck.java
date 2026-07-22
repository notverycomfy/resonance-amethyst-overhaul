package com.resonance.worldgen;

import com.resonance.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Guards decoration features against generating inside (or overhanging into)
 * the Harmonic Arena structure.
 */
public final class ArenaCheck {

    private ArenaCheck() {}

    public static boolean isNearArena(WorldGenLevel level, BlockPos origin, int margin) {
        return isNearArena(level, origin, margin, 0);
    }

    /**
     * Checks the full area a wide or underground feature can occupy. Sampling
     * corners prevents diagonal overlap, while vertical sampling catches geodes
     * whose configured origin is below the arena floor.
     */
    public static boolean isNearArena(WorldGenLevel level, BlockPos origin, int horizontalMargin, int verticalMargin) {
        var structureManager = level.getLevel().structureManager();
        int verticalStep = verticalMargin == 0 ? 1 : 8;
        for (int dy = -verticalMargin; dy <= verticalMargin; dy += verticalStep) {
            for (int dx : new int[] {-horizontalMargin, 0, horizontalMargin}) {
                for (int dz : new int[] {-horizontalMargin, 0, horizontalMargin}) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (structureManager.getStructureWithPieceAt(pos,
                            holder -> holder.is(ModStructures.HARMONIC_ARENA_KEY)).isValid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
