package com.resonance.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Fabric equivalent of the NeoForge global exploration-loot modifiers.
 */
public final class ModLootModifiers {
    private ModLootModifiers() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            String id = key.identifier().toString();
            if ("minecraft:chests/ancient_city".equals(id)) {
                add(table, ModItems.AMETHYST_INGOT.get(), 0.175F, 1, 3);
                add(table, ModItems.RESONANT_HELMET.get(), 0.04F, 1, 1);
                add(table, ModItems.RESONANT_CHESTPLATE.get(), 0.04F, 1, 1);
                add(table, ModItems.RESONANT_LEGGINGS.get(), 0.04F, 1, 1);
                add(table, ModItems.RESONANT_BOOTS.get(), 0.04F, 1, 1);
                add(table, ModItems.RESONANT_HORSE_ARMOR.get(), 0.0075F, 1, 1);
            } else if ("minecraft:chests/end_city_treasure".equals(id)) {
                add(table, ModItems.AMETHYST_INGOT.get(), 0.15F, 2, 4);
                add(table, ModItems.RESONANT_HELMET.get(), 0.065F, 1, 1);
                add(table, ModItems.RESONANT_CHESTPLATE.get(), 0.065F, 1, 1);
                add(table, ModItems.RESONANT_LEGGINGS.get(), 0.065F, 1, 1);
                add(table, ModItems.RESONANT_BOOTS.get(), 0.065F, 1, 1);
                add(table, ModItems.RESONANT_HORSE_ARMOR.get(), 0.01F, 1, 1);
            } else if ("minecraft:chests/buried_treasure".equals(id)) {
                add(table, ModItems.RESONANT_NAUTILUS_ARMOR.get(), 0.01F, 1, 1);
            } else if ("minecraft:chests/shipwreck_treasure".equals(id)) {
                add(table, ModItems.RESONANT_NAUTILUS_ARMOR.get(), 0.0075F, 1, 1);
            }
        });
    }

    private static void add(LootTable.Builder table, Item item, float chance, int min, int max) {
        LootItem.Builder<?> entry = LootItem.lootTableItem(item)
                .when(LootItemRandomChanceCondition.randomChance(chance));
        if (min != 1 || max != 1) {
            entry.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }
        table.withPool(LootPool.lootPool().add(entry));
    }
}
