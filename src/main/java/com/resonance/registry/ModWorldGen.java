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
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModWorldGen {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Resonance.MODID);

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Resonance.MODID);

    public static final DeferredHolder<Feature<?>, AmethystSpireFeature> AMETHYST_SPIRE =
            FEATURES.register("amethyst_spire", () -> new AmethystSpireFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, CrystalScatterFeature> CRYSTAL_SCATTER =
            FEATURES.register("crystal_scatter", () -> new CrystalScatterFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, MassiveSpireFeature> MASSIVE_SPIRE =
            FEATURES.register("massive_spire", () -> new MassiveSpireFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, ArenaSafeGeodeFeature> ARENA_SAFE_GEODE =
            FEATURES.register("arena_safe_geode", () -> new ArenaSafeGeodeFeature(GeodeConfiguration.CODEC));

    static {
        BIOME_SOURCES.register("resonance_end", () -> ResonanceEndBiomeSource.CODEC);
    }
}
