package com.resonance.registry;

import com.mojang.serialization.MapCodec;
import com.resonance.Resonance;
import com.resonance.worldgen.AmethystSpireFeature;
import com.resonance.worldgen.ArenaSafeGeodeFeature;
import com.resonance.worldgen.CrystalScatterFeature;
import com.resonance.worldgen.MassiveSpireFeature;
import com.resonance.worldgen.ResonanceEndBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.resonance.fabric.registry.DeferredHolder;
import com.resonance.fabric.registry.DeferredRegister;

public class ModWorldGen {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Resonance.MODID);

    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, Resonance.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<AmethystSpireFeature>> AMETHYST_SPIRE =
            FEATURES.register("amethyst_spire", () -> AmethystSpireFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CrystalScatterFeature>> CRYSTAL_SCATTER =
            FEATURES.register("crystal_scatter", () -> CrystalScatterFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MassiveSpireFeature>> MASSIVE_SPIRE =
            FEATURES.register("massive_spire", () -> MassiveSpireFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArenaSafeGeodeFeature>> ARENA_SAFE_GEODE =
            FEATURES.register("arena_safe_geode", () -> ArenaSafeGeodeFeature.CODEC);

    static {
        BIOME_SOURCES.register("resonance_end", () -> ResonanceEndBiomeSource.CODEC);
    }
}
