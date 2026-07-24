package com.resonance.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks active "crystal forest" spreads: after The Harmonic falls, the biome
 * around its death site slowly converts into resonance:crystal_forest, and the
 * end stone blooms with crystal growth — bringing life to the End.
 */
public class CrystalForestSpreadData extends SavedData {

    public static final ResourceKey<Biome> CRYSTAL_FOREST =
            ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("resonance", "crystal_forest"));

    private static final double MAX_RADIUS = 96.0;
    private static final double STEP_RADIUS = 0.3;
    private static final int STEP_INTERVAL = 60;
    private static final int VERTICAL_BELOW = 24;
    private static final int VERTICAL_ABOVE = 40;

    public record Spread(BlockPos center, double radius) {
        public static final Codec<Spread> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BlockPos.CODEC.fieldOf("center").forGetter(Spread::center),
                        Codec.DOUBLE.fieldOf("radius").forGetter(Spread::radius)
                ).apply(instance, Spread::new));
    }

    private final List<Spread> spreads = new ArrayList<>();

    public static final Codec<CrystalForestSpreadData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Spread.CODEC.listOf().fieldOf("spreads").forGetter(d -> List.copyOf(d.spreads))
            ).apply(instance, list -> {
                CrystalForestSpreadData data = new CrystalForestSpreadData();
                data.spreads.addAll(list);
                return data;
            }));

    public static final SavedDataType<CrystalForestSpreadData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("resonance", "crystal_forest_spread"),
            CrystalForestSpreadData::new,
            CODEC
    );

    public CrystalForestSpreadData() {
    }

    public static CrystalForestSpreadData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void startSpread(BlockPos center) {
        spreads.add(new Spread(center, 2.0));
        setDirty();
    }

    public void tick(ServerLevel level) {
        if (spreads.isEmpty() || level.getGameTime() % STEP_INTERVAL != 0) return;

        Holder<Biome> forestBiome = level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .get(CRYSTAL_FOREST)
                .map(h -> (Holder<Biome>) h)
                .orElse(null);
        if (forestBiome == null) return;

        for (int i = 0; i < spreads.size(); i++) {
            Spread spread = spreads.get(i);
            double newRadius = spread.radius() + STEP_RADIUS;

            convertBiomeRing(level, spread.center(), newRadius, forestBiome);
            decorateBand(level, spread.center(), newRadius);

            if (newRadius >= MAX_RADIUS) {
                spreads.remove(i--);
                level.playSound(null, spread.center(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.AMBIENT, 3.0F, 0.5F);
            } else {
                spreads.set(i, new Spread(spread.center(), newRadius));
            }
        }
        setDirty();
    }

    /** Converts the biome inside the current radius, resyncing only the chunks near the advancing edge. */
    private static void convertBiomeRing(ServerLevel level, BlockPos center, double radius, Holder<Biome> biome) {
        int minY = center.getY() - VERTICAL_BELOW;
        int maxY = center.getY() + VERTICAL_ABOVE;

        Set<ChunkAccess> touched = new HashSet<>();
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - (int) radius - 1);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + (int) radius + 1);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - (int) radius - 1);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + (int) radius + 1);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                // Only chunks whose area intersects the advancing edge band
                double nearestX = Mth.clamp(center.getX(), cx << 4, (cx << 4) + 15);
                double nearestZ = Mth.clamp(center.getZ(), cz << 4, (cz << 4) + 15);
                double cornerDist = Math.max(
                        Math.max(dist(center, cx << 4, cz << 4), dist(center, (cx << 4) + 15, cz << 4)),
                        Math.max(dist(center, cx << 4, (cz << 4) + 15), dist(center, (cx << 4) + 15, (cz << 4) + 15)));
                double nearest = Math.sqrt(
                        (nearestX - center.getX()) * (nearestX - center.getX())
                                + (nearestZ - center.getZ()) * (nearestZ - center.getZ()));
                if (nearest > radius || cornerDist < radius - 8) continue;

                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk != null) touched.add(chunk);
            }
        }

        if (touched.isEmpty()) return;

        double radiusSq = radius * radius;
        for (ChunkAccess chunk : touched) {
            chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) -> {
                int bx = QuartPos.toBlock(quartX) + 2;
                int by = QuartPos.toBlock(quartY) + 2;
                int bz = QuartPos.toBlock(quartZ) + 2;
                Holder<Biome> current = chunk.getNoiseBiome(quartX, quartY, quartZ);
                if (by >= minY && by <= maxY
                        && distSq(center, bx, bz) <= radiusSq
                        && !current.is(CRYSTAL_FOREST)) {
                    return biome;
                }
                return current;
            }, level.getChunkSource().randomState().sampler());
            chunk.markUnsaved();
        }
        level.getChunkSource().chunkMap.resendBiomesForChunks(new ArrayList<>(touched));
    }

    private static double dist(BlockPos center, int x, int z) {
        return Math.sqrt(distSq(center, x, z));
    }

    private static double distSq(BlockPos center, int x, int z) {
        double dx = x - center.getX();
        double dz = z - center.getZ();
        return dx * dx + dz * dz;
    }

    /** Brings crystal life along the spread: crystal grass, flowers, trees, and critters. */
    private static void decorateBand(ServerLevel level, BlockPos center, double radius) {
        RandomSource random = level.getRandom();
        int samples = 10 + (int) (radius * 1.2);

        for (int i = 0; i < samples; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            // Uniform sampling over the whole converted disc, so coverage
            // keeps thickening behind the advancing front
            double r = radius * Math.sqrt(random.nextDouble());
            int x = center.getX() + (int) (Math.cos(angle) * r);
            int z = center.getZ() + (int) (Math.sin(angle) * r);

            if (!level.isLoaded(new BlockPos(x, center.getY(), z))) continue;

            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
            BlockPos ground = surface.below();
            if (Math.abs(surface.getY() - center.getY()) > VERTICAL_ABOVE) continue;

            var groundState = level.getBlockState(ground);
            boolean bareGround = groundState.is(Blocks.END_STONE)
                    || groundState.is(Blocks.BASALT) || groundState.is(Blocks.SMOOTH_BASALT);
            boolean livingGround = groundState.is(com.resonance.registry.ModBlocks.CRYSTAL_GRASS_BLOCK.get());
            if (!bareGround && !livingGround) continue;

            float roll = random.nextFloat();

            if (bareGround) {
                if (roll < 0.75F) {
                    level.setBlock(ground, com.resonance.registry.ModBlocks.CRYSTAL_GRASS_BLOCK.get().defaultBlockState(), 3);
                } else {
                    level.setBlock(ground, Blocks.CALCITE.defaultBlockState(), 3);
                }
            } else {
                if (!level.getBlockState(surface).isAir()) continue;

                if (roll < 0.035F) {
                    growCrystalTree(level, surface, random);
                } else if (roll < 0.30F) {
                    level.setBlock(surface, com.resonance.registry.ModBlocks.CRYSTAL_GRASS.get().defaultBlockState(), 3);
                } else if (roll < 0.42F) {
                    level.setBlock(surface, com.resonance.registry.ModBlocks.CRYSTAL_BLOOM.get().defaultBlockState(), 3);
                } else if (roll < 0.50F) {
                    level.setBlock(surface, com.resonance.registry.ModBlocks.SHARD_BLOSSOM.get().defaultBlockState(), 3);
                } else if (roll < 0.58F) {
                    float budRoll = random.nextFloat();
                    var bud = budRoll < 0.5F ? Blocks.SMALL_AMETHYST_BUD
                            : budRoll < 0.8F ? Blocks.MEDIUM_AMETHYST_BUD
                            : Blocks.LARGE_AMETHYST_BUD;
                    level.setBlock(surface, bud.defaultBlockState(), 3);
                } else if (roll < 0.585F) {
                    trySpawnCritter(level, surface, random);
                }
            }

            level.sendParticles(ParticleTypes.END_ROD,
                    x + 0.5, surface.getY() + 0.8, z + 0.5, 2, 0.4, 0.4, 0.4, 0.02);
        }

        if (random.nextFloat() < 0.3F) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            level.playSound(null,
                    center.getX() + Math.cos(angle) * radius * 0.7,
                    center.getY(),
                    center.getZ() + Math.sin(angle) * radius * 0.7,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                    1.2F, 0.6F + random.nextFloat() * 0.8F);
        }
    }

    /** Keeps a small population of crystal critters living in the growing forest. */
    private static void trySpawnCritter(ServerLevel level, BlockPos pos, RandomSource random) {
        int nearbyRabbits = level.getEntitiesOfClass(com.resonance.entity.CrystalRabbitEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(48)).size();
        int nearbyArmadillos = level.getEntitiesOfClass(com.resonance.entity.CrystalArmadilloEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(48)).size();
        if (nearbyRabbits + nearbyArmadillos >= 8) return;

        var type = random.nextBoolean()
                ? com.resonance.registry.ModEntities.CRYSTAL_RABBIT.get()
                : com.resonance.registry.ModEntities.CRYSTAL_ARMADILLO.get();
        var critter = type.spawn(level, pos, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (critter != null) {
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.0F, 1.8F);
        }
    }

    /**
     * Grows a crystal tree with vanilla's exact cherry tree shape distribution.
     * Only the trunk, foliage, and below-trunk block providers differ.
     */
    private static void growCrystalTree(ServerLevel level, BlockPos base, RandomSource random) {
        IntProvider branchCount = new WeightedListInt(WeightedList.<IntProvider>builder()
                .add(ConstantInt.of(1))
                .add(ConstantInt.of(2))
                .add(ConstantInt.of(3))
                .build());

        CherryTrunkPlacer trunkPlacer = new CherryTrunkPlacer(
                7, 1, 0,
                branchCount,
                UniformInt.of(2, 4),
                UniformInt.of(-4, -3),
                UniformInt.of(-1, 0));
        CherryFoliagePlacer foliagePlacer = new CherryFoliagePlacer(
                ConstantInt.of(4),
                ConstantInt.ZERO,
                ConstantInt.of(5),
                0.25F,
                0.25F,
                1.0F / 6.0F,
                1.0F / 3.0F);

        TreeConfiguration configuration = new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(com.resonance.registry.ModBlocks.CRYSTAL_LOG.get()),
                trunkPlacer,
                BlockStateProvider.simple(com.resonance.registry.ModBlocks.CRYSTAL_LEAVES.get()),
                foliagePlacer,
                new TwoLayersFeatureSize(1, 0, 2))
                .belowTrunkProvider(BlockStateProvider.simple(
                        com.resonance.registry.ModBlocks.ROOTED_CRYSTAL_DIRT.get()))
                .ignoreVines()
                .build();

        if (Feature.TREE.place(configuration, level, level.getChunkSource().getGenerator(), random, base)) {
            level.playSound(null, base, SoundEvents.AMETHYST_CLUSTER_PLACE,
                    SoundSource.AMBIENT, 1.5F, 0.8F);
            level.sendParticles(ParticleTypes.END_ROD,
                    base.getX() + 0.5, base.getY() + 4.0, base.getZ() + 0.5,
                    15, 0.5, 3.0, 0.5, 0.05);
        }
    }
}
