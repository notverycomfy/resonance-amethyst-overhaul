package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.block.entity.ChorusResonatorBlockEntity;
import com.resonance.block.entity.ResonantLanternBlockEntity;
import com.resonance.block.entity.FrequencyRelayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Resonance.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantLanternBlockEntity>> RESONANT_LANTERN =
            BLOCK_ENTITIES.register("resonant_lantern", () ->
                    new BlockEntityType<>(ResonantLanternBlockEntity::new, ModBlocks.RESONANT_LANTERN.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrequencyRelayBlockEntity>> FREQUENCY_RELAY =
            BLOCK_ENTITIES.register("frequency_relay", () ->
                    new BlockEntityType<>(FrequencyRelayBlockEntity::new, ModBlocks.FREQUENCY_RELAY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChorusResonatorBlockEntity>> CHORUS_RESONATOR =
            BLOCK_ENTITIES.register("chorus_resonator", () ->
                    new BlockEntityType<>(ChorusResonatorBlockEntity::new, ModBlocks.CHORUS_RESONATOR.get()));
}
