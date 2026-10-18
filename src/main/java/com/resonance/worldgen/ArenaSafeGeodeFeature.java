package com.resonance.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;

/** Vanilla geode generation with a safety envelope around the Harmonic Arena. */
public record ArenaSafeGeodeFeature(GeodeFeature geode) implements Feature {

    public static final MapCodec<ArenaSafeGeodeFeature> CODEC =
            GeodeFeature.CODEC.xmap(ArenaSafeGeodeFeature::new, ArenaSafeGeodeFeature::geode);

    private static final int HORIZONTAL_SAFETY_MARGIN = 32;
    private static final int VERTICAL_SAFETY_MARGIN = 32;

    @Override
    public MapCodec<ArenaSafeGeodeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (ArenaCheck.isNearArena(level, origin,
                HORIZONTAL_SAFETY_MARGIN, VERTICAL_SAFETY_MARGIN)) {
            return false;
        }
        return geode.place(level, generator, random, origin);
    }
}
