package com.resonance.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;

public class HarmonizedFarmlandData extends SavedData {

    private final Set<Long> positions = new HashSet<>();

    public static final Codec<HarmonizedFarmlandData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf().fieldOf("positions").forGetter(d -> List.copyOf(d.positions))
            ).apply(instance, longs -> {
                HarmonizedFarmlandData data = new HarmonizedFarmlandData();
                data.positions.addAll(longs);
                return data;
            })
    );

    public static final SavedDataType<HarmonizedFarmlandData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("resonance", "harmonized_farmland"),
            HarmonizedFarmlandData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public HarmonizedFarmlandData() {
    }

    public static HarmonizedFarmlandData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void add(BlockPos pos) {
        if (positions.add(pos.asLong())) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (positions.remove(pos.asLong())) {
            setDirty();
        }
    }

    public boolean isHarmonized(BlockPos pos) {
        return positions.contains(pos.asLong());
    }

    /** Removes markers after farmland is trampled, dries out, or is replaced. */
    public void pruneInvalid(ServerLevel level) {
        if (positions.removeIf(packed -> {
            BlockPos pos = BlockPos.of(packed);
            if (!level.hasChunkAt(pos)) return false;
            var state = level.getBlockState(pos);
            return !state.is(Blocks.FARMLAND)
                    && !state.is(com.resonance.registry.ModBlocks.CRYSTAL_FARMLAND.get());
        })) {
            setDirty();
        }
    }
}
