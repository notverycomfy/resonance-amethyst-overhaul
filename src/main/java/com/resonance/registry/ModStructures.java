package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.worldgen.HarmonicArenaStructure;
import com.resonance.worldgen.HarmonicArenaPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Resonance.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Resonance.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<HarmonicArenaStructure>> HARMONIC_ARENA_TYPE =
            STRUCTURE_TYPES.register("harmonic_arena", () -> () -> HarmonicArenaStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> ARENA_PIECE =
            STRUCTURE_PIECE_TYPES.register("harmonic_arena", () -> HarmonicArenaPiece::new);

    public static final ResourceKey<Structure> HARMONIC_ARENA_KEY =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(Resonance.MODID, "harmonic_arena"));
}
