package com.resonance.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resonance.Resonance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.stream.Stream;

public class ResonanceEndBiomeSource extends BiomeSource {

    public static final ResourceKey<Biome> CRYSTALLIZED_END =
            ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystallized_end"));

    private static final long INNER_EDGE_MIN = 64L * 64L;
    private static final long INNER_EDGE_MAX = 90L * 90L;

    public static final MapCodec<ResonanceEndBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    RegistryOps.retrieveElement(Biomes.THE_END),
                    RegistryOps.retrieveElement(Biomes.END_HIGHLANDS),
                    RegistryOps.retrieveElement(Biomes.END_MIDLANDS),
                    RegistryOps.retrieveElement(Biomes.SMALL_END_ISLANDS),
                    RegistryOps.retrieveElement(Biomes.END_BARRENS),
                    RegistryOps.retrieveElement(CRYSTALLIZED_END)
            ).apply(i, i.stable(ResonanceEndBiomeSource::new))
    );

    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;
    private final Holder<Biome> crystallized;

    public ResonanceEndBiomeSource(Holder<Biome> end, Holder<Biome> highlands, Holder<Biome> midlands,
                                    Holder<Biome> islands, Holder<Biome> barrens, Holder<Biome> crystallized) {
        this.end = end;
        this.highlands = highlands;
        this.midlands = midlands;
        this.islands = islands;
        this.barrens = barrens;
        this.crystallized = crystallized;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.end, this.highlands, this.midlands, this.islands, this.barrens, this.crystallized);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(quartX);
        int blockY = QuartPos.toBlock(quartY);
        int blockZ = QuartPos.toBlock(quartZ);
        int chunkX = SectionPos.blockToSectionCoord(blockX);
        int chunkZ = SectionPos.blockToSectionCoord(blockZ);

        long chunkDistSq = (long) chunkX * chunkX + (long) chunkZ * chunkZ;

        if (chunkDistSq <= 4096L) {
            return this.end;
        }

        int weirdBlockX = (SectionPos.blockToSectionCoord(blockX) * 2 + 1) * 8;
        int weirdBlockZ = (SectionPos.blockToSectionCoord(blockZ) * 2 + 1) * 8;
        double erosion = sampler.erosion().compute(new DensityFunction.SinglePointContext(weirdBlockX, blockY, weirdBlockZ));

        if (chunkDistSq >= INNER_EDGE_MIN && chunkDistSq <= INNER_EDGE_MAX && erosion > -0.21875) {
            return this.crystallized;
        }

        if (erosion > 0.25) {
            return this.highlands;
        } else if (erosion >= -0.0625) {
            return this.midlands;
        } else {
            return erosion < -0.21875 ? this.islands : this.barrens;
        }
    }
}
