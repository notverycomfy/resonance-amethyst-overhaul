package com.resonance.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class HarmonicArenaStructure extends Structure {

    public static final MapCodec<HarmonicArenaStructure> CODEC = simpleCodec(HarmonicArenaStructure::new);
    public static final int PLATFORM_RADIUS = 24;
    public static final int PILLAR_COUNT = 8;

    public HarmonicArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        // Sample terrain across the platform footprint, not just the center,
        // so the arena sits on real ground instead of a lone island peak.
        int[][] offsets = {{0, 0}, {16, 0}, {-16, 0}, {0, 16}, {0, -16}};
        int valid = 0;
        int sum = 0;
        for (int[] off : offsets) {
            int h = context.chunkGenerator().getFirstOccupiedHeight(
                    x + off[0], z + off[1], Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            if (h >= 40) {
                valid++;
                sum += h;
            }
        }

        // Require most of the footprint to be over terrain
        if (valid < 4) return Optional.empty();

        int y = sum / valid;
        BlockPos center = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(center, (builder) -> {
            builder.addPiece(new HarmonicArenaPiece(center));
        }));
    }

    @Override
    public StructureType<?> type() {
        return com.resonance.registry.ModStructures.HARMONIC_ARENA_TYPE.get();
    }
}
