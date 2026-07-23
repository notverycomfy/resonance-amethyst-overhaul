package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.block.entity.ChorusResonatorBlockEntity;
import com.resonance.block.entity.ResonantLanternBlockEntity;
import com.resonance.block.entity.FrequencyRelayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.resonance.fabric.registry.DeferredHolder;
import com.resonance.fabric.registry.DeferredRegister;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Resonance.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantLanternBlockEntity>> RESONANT_LANTERN =
            BLOCK_ENTITIES.register("resonant_lantern", () ->
                    FabricBlockEntityTypeBuilder.create(ResonantLanternBlockEntity::new,
                            ModBlocks.RESONANT_LANTERN.get()).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrequencyRelayBlockEntity>> FREQUENCY_RELAY =
            BLOCK_ENTITIES.register("frequency_relay", () ->
                    FabricBlockEntityTypeBuilder.create(FrequencyRelayBlockEntity::new,
                            ModBlocks.FREQUENCY_RELAY.get()).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChorusResonatorBlockEntity>> CHORUS_RESONATOR =
            BLOCK_ENTITIES.register("chorus_resonator", () ->
                    FabricBlockEntityTypeBuilder.create(ChorusResonatorBlockEntity::new,
                            ModBlocks.CHORUS_RESONATOR.get()).build());
}
