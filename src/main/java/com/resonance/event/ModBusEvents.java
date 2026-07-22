package com.resonance.event;

import com.resonance.entity.CrystalSentinelEntity;
import com.resonance.entity.CrystalWraithEntity;
import com.resonance.entity.ResonantStalkerEntity;
import com.resonance.entity.ShatteredEchoEntity;
import com.resonance.entity.TheHarmonicEntity;
import com.resonance.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class ModBusEvents {

    public static void register(IEventBus modBus) {
        modBus.addListener(ModBusEvents::registerAttributes);
        modBus.addListener(ModBusEvents::registerSpawnPlacements);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHATTERED_ECHO.get(), ShatteredEchoEntity.createAttributes().build());
        event.put(ModEntities.RESONANT_STALKER.get(), ResonantStalkerEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_WRAITH.get(), CrystalWraithEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_SENTINEL.get(), CrystalSentinelEntity.createAttributes().build());
        event.put(ModEntities.THE_HARMONIC.get(), TheHarmonicEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_RABBIT.get(), com.resonance.entity.CrystalRabbitEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_ARMADILLO.get(), com.resonance.entity.CrystalArmadilloEntity.createAttributes().build());
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.SHATTERED_ECHO.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ShatteredEchoEntity::checkShatteredEchoSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_SENTINEL.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                CrystalSentinelEntity::checkSentinelSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_RABBIT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_ARMADILLO.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
