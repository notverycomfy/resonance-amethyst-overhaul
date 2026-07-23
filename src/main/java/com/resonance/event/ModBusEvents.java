package com.resonance.event;

import com.resonance.entity.CrystalArmadilloEntity;
import com.resonance.entity.CrystalRabbitEntity;
import com.resonance.entity.CrystalSentinelEntity;
import com.resonance.entity.CrystalWraithEntity;
import com.resonance.entity.ResonantStalkerEntity;
import com.resonance.entity.ShatteredEchoEntity;
import com.resonance.entity.TheHarmonicEntity;
import com.resonance.registry.ModEntities;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModBusEvents {
    private ModBusEvents() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(ModEntities.SHATTERED_ECHO.get(), ShatteredEchoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.RESONANT_STALKER.get(), ResonantStalkerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CRYSTAL_WRAITH.get(), CrystalWraithEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CRYSTAL_SENTINEL.get(), CrystalSentinelEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.THE_HARMONIC.get(), TheHarmonicEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CRYSTAL_RABBIT.get(), CrystalRabbitEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CRYSTAL_ARMADILLO.get(), CrystalArmadilloEntity.createAttributes());

        SpawnPlacements.register(ModEntities.SHATTERED_ECHO.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ShatteredEchoEntity::checkShatteredEchoSpawnRules);
        SpawnPlacements.register(ModEntities.CRYSTAL_SENTINEL.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, CrystalSentinelEntity::checkSentinelSpawnRules);
        SpawnPlacements.register(ModEntities.CRYSTAL_RABBIT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> true);
        SpawnPlacements.register(ModEntities.CRYSTAL_ARMADILLO.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> true);
    }
}
