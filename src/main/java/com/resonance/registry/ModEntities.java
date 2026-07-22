package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.entity.CrystalShardEntity;
import com.resonance.entity.HarmonicAnchorEntity;
import com.resonance.entity.ResonantArrowEntity;
import com.resonance.entity.CrystalSentinelEntity;
import com.resonance.entity.CrystalWraithEntity;
import com.resonance.entity.ResonantStalkerEntity;
import com.resonance.entity.ShatteredEchoEntity;
import com.resonance.entity.TheHarmonicEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Resonance.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ResonantArrowEntity>> RESONANT_ARROW =
            ENTITY_TYPES.register("resonant_arrow", () ->
                    EntityType.Builder.<ResonantArrowEntity>of(ResonantArrowEntity::new, MobCategory.MISC)
                            .noLootTable()
                            .sized(0.5F, 0.5F)
                            .eyeHeight(0.13F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "resonant_arrow")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ShatteredEchoEntity>> SHATTERED_ECHO =
            ENTITY_TYPES.register("shattered_echo", () ->
                    EntityType.Builder.of(ShatteredEchoEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "shattered_echo")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ResonantStalkerEntity>> RESONANT_STALKER =
            ENTITY_TYPES.register("resonant_stalker", () ->
                    EntityType.Builder.of(ResonantStalkerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "resonant_stalker")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CrystalWraithEntity>> CRYSTAL_WRAITH =
            ENTITY_TYPES.register("crystal_wraith", () ->
                    EntityType.Builder.of(CrystalWraithEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_wraith")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TheHarmonicEntity>> THE_HARMONIC =
            ENTITY_TYPES.register("the_harmonic", () ->
                    EntityType.Builder.of(TheHarmonicEntity::new, MobCategory.MONSTER)
                            // Closely contains the visible 1.875 x 3.875 block model.
                            .sized(2.0F, 4.0F)
                            .eyeHeight(3.5F)
                            .clientTrackingRange(16)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "the_harmonic")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CrystalShardEntity>> CRYSTAL_SHARD =
            ENTITY_TYPES.register("crystal_shard", () ->
                    EntityType.Builder.<CrystalShardEntity>of(CrystalShardEntity::new, MobCategory.MISC)
                            .noLootTable()
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_shard")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<HarmonicAnchorEntity>> HARMONIC_ANCHOR =
            ENTITY_TYPES.register("harmonic_anchor", () ->
                    EntityType.Builder.<HarmonicAnchorEntity>of(HarmonicAnchorEntity::new, MobCategory.MISC)
                            .noLootTable()
                            .sized(1.0F, 1.2F)
                            .clientTrackingRange(16)
                            .updateInterval(10)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "harmonic_anchor")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.resonance.entity.CrystalRabbitEntity>> CRYSTAL_RABBIT =
            ENTITY_TYPES.register("crystal_rabbit", () ->
                    EntityType.Builder.<com.resonance.entity.CrystalRabbitEntity>of(com.resonance.entity.CrystalRabbitEntity::new, MobCategory.CREATURE)
                            .sized(0.49F, 0.6F)
                            .eyeHeight(0.59F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_rabbit")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.resonance.entity.CrystalArmadilloEntity>> CRYSTAL_ARMADILLO =
            ENTITY_TYPES.register("crystal_armadillo", () ->
                    EntityType.Builder.<com.resonance.entity.CrystalArmadilloEntity>of(com.resonance.entity.CrystalArmadilloEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.65F)
                            .eyeHeight(0.26F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_armadillo")))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CrystalSentinelEntity>> CRYSTAL_SENTINEL =
            ENTITY_TYPES.register("crystal_sentinel", () ->
                    EntityType.Builder.of(CrystalSentinelEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 1.0F)
                            .eyeHeight(0.5F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_sentinel")))
            );
}
