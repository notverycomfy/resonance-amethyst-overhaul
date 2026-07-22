package com.resonance.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;

/** Vanilla geode generation with a safety envelope around the Harmonic Arena. */
public class ArenaSafeGeodeFeature extends GeodeFeature {

    private static final int HORIZONTAL_SAFETY_MARGIN = 32;
    private static final int VERTICAL_SAFETY_MARGIN = 32;

    public ArenaSafeGeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> context) {
        if (ArenaCheck.isNearArena(context.level(), context.origin(),
                HORIZONTAL_SAFETY_MARGIN, VERTICAL_SAFETY_MARGIN)) {
            return false;
        }
        return super.place(context);
    }
}
