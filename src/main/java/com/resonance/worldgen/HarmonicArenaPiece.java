package com.resonance.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.resonance.registry.ModBlocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class HarmonicArenaPiece extends StructurePiece {

    private static final int RADIUS = 24;
    private static final int PILLAR_HEIGHT = 12;
    private static final int PILLAR_COUNT = 8;
    private final BlockPos center;

    public HarmonicArenaPiece(BlockPos center) {
        super(com.resonance.registry.ModStructures.ARENA_PIECE.get(), 0,
                new BoundingBox(
                        center.getX() - RADIUS - 2, center.getY() - 3, center.getZ() - RADIUS - 2,
                        center.getX() + RADIUS + 2, center.getY() + PILLAR_HEIGHT + 5, center.getZ() + RADIUS + 2));
        this.center = center;
    }

    public HarmonicArenaPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(com.resonance.registry.ModStructures.ARENA_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx").orElse(0), tag.getInt("cy").orElse(0), tag.getInt("cz").orElse(0));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pivot) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                float dist = Mth.sqrt(dx * dx + dz * dz);
                if (dist > RADIUS) continue;

                BlockPos pos = new BlockPos(cx + dx, cy, cz + dz);
                if (!chunkBB.isInside(pos)) continue;

                level.setBlock(pos, Blocks.SMOOTH_BASALT.defaultBlockState(), 2);
                level.setBlock(pos.below(), Blocks.BASALT.defaultBlockState(), 2);
                level.setBlock(pos.below(2), Blocks.BASALT.defaultBlockState(), 2);

                for (int y = 1; y <= PILLAR_HEIGHT + 4; y++) {
                    BlockPos above = pos.above(y);
                    if (chunkBB.isInside(above)) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                if (dist > RADIUS - 2 && dist <= RADIUS) {
                    level.setBlock(pos.above(), Blocks.SMOOTH_BASALT.defaultBlockState(), 2);
                }

                if (dist >= 10 && dist <= 11) {
                    level.setBlock(pos, Blocks.CALCITE.defaultBlockState(), 2);
                }

                if (dist <= 3) {
                    level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    if (dist <= 1) {
                        level.setBlock(pos, Blocks.BUDDING_AMETHYST.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos resonatorPos = new BlockPos(cx, cy + 1, cz);
        if (chunkBB.isInside(resonatorPos)) {
            level.setBlock(resonatorPos, ModBlocks.CHORUS_RESONATOR.get().defaultBlockState(), 2);
        }

        for (int i = 0; i < PILLAR_COUNT; i++) {
            float angle = i * Mth.TWO_PI / PILLAR_COUNT;
            int px = cx + Math.round(18 * Mth.cos(angle));
            int pz = cz + Math.round(18 * Mth.sin(angle));

            for (int y = 0; y <= PILLAR_HEIGHT; y++) {
                BlockPos pillarPos = new BlockPos(px, cy + y, pz);
                if (!chunkBB.isInside(pillarPos)) continue;

                level.setBlock(pillarPos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                if (y <= 2) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos adj = pillarPos.relative(dir);
                        if (chunkBB.isInside(adj)) {
                            level.setBlock(adj, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }
            }

            BlockPos top = new BlockPos(px, cy + PILLAR_HEIGHT + 1, pz);
            if (chunkBB.isInside(top)) {
                level.setBlock(top, Blocks.AMETHYST_CLUSTER.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos sidePos = new BlockPos(px, cy + PILLAR_HEIGHT, pz).relative(dir);
                if (chunkBB.isInside(sidePos) && level.getBlockState(sidePos).isAir()) {
                    level.setBlock(sidePos, Blocks.LARGE_AMETHYST_BUD.defaultBlockState()
                            .setValue(AmethystClusterBlock.FACING, dir), 2);
                }
            }
        }
    }
}
