package com.resonance;

import com.resonance.event.ModBusEvents;
import com.resonance.registry.ModBlockEntities;
import com.resonance.registry.ModBlocks;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModItems;
import com.resonance.registry.ModLootModifiers;
import com.resonance.registry.ModPotions;
import com.resonance.registry.ModSounds;
import com.resonance.registry.ModStructures;
import com.resonance.registry.ModWorldGen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@Mod(Resonance.MODID)
public class Resonance {
    public static final String MODID = "resonance";

    public Resonance(IEventBus modEventBus, ModContainer modContainer) {
        // Force ModBlocks to load (registers block items on ModItems.ITEMS)
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModWorldGen.BIOME_SOURCES.register(modEventBus);
        ModWorldGen.FEATURES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);

        ModBusEvents.register(modEventBus);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void place(BuildCreativeModeTabContentsEvent event, ItemStack after, DeferredItem<?> item) {
        var vis = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        var stack = item.toStack();
        // Remove if auto-added by vanilla, then re-insert in the correct position
        event.remove(stack, vis);
        event.insertAfter(after, stack, vis);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            place(event, new ItemStack(Items.RABBIT), ModItems.RAW_CRYSTAL_RABBIT);
            place(event, new ItemStack(Items.COOKED_RABBIT), ModItems.COOKED_CRYSTAL_RABBIT);
            place(event, new ItemStack(Items.RABBIT_STEW), ModItems.CRYSTAL_RABBIT_STEW);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            place(event, new ItemStack(Items.AMETHYST_SHARD), ModItems.AMETHYST_INGOT);
            place(event, ModItems.AMETHYST_INGOT.toStack(), ModItems.HARMONIC_FRAGMENT);
            place(event, ModItems.HARMONIC_FRAGMENT.toStack(), ModItems.WHISPER_FRAGMENT);
            place(event, ModItems.WHISPER_FRAGMENT.toStack(), ModItems.CRYSTAL_FRAGMENT);
            place(event, new ItemStack(Items.ARMADILLO_SCUTE), ModItems.CRYSTAL_SCUTE);
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            place(event, new ItemStack(Items.ARMADILLO_SPAWN_EGG), ModItems.CRYSTAL_ARMADILLO_SPAWN_EGG);
            place(event, new ItemStack(Items.RABBIT_SPAWN_EGG), ModItems.CRYSTAL_RABBIT_SPAWN_EGG);
            place(event, new ItemStack(Items.SKELETON_SPAWN_EGG), ModItems.SHATTERED_ECHO_SPAWN_EGG);
            place(event, new ItemStack(Items.PHANTOM_SPAWN_EGG), ModItems.CRYSTAL_WRAITH_SPAWN_EGG);
            place(event, new ItemStack(Items.ENDERMAN_SPAWN_EGG), ModItems.RESONANT_STALKER_SPAWN_EGG);
            place(event, new ItemStack(Items.SHULKER_SPAWN_EGG), ModItems.CRYSTAL_SENTINEL_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            // Keep the complete crystal family together, between cherry and pale oak.
            place(event, new ItemStack(Items.CHERRY_BUTTON), ModBlocks.CRYSTAL_LOG_ITEM);
            place(event, ModBlocks.CRYSTAL_LOG_ITEM.toStack(), ModBlocks.CRYSTAL_WOOD_ITEM);
            place(event, ModBlocks.CRYSTAL_WOOD_ITEM.toStack(), ModBlocks.STRIPPED_CRYSTAL_LOG_ITEM);
            place(event, ModBlocks.STRIPPED_CRYSTAL_LOG_ITEM.toStack(), ModBlocks.STRIPPED_CRYSTAL_WOOD_ITEM);
            place(event, ModBlocks.STRIPPED_CRYSTAL_WOOD_ITEM.toStack(), ModBlocks.CRYSTAL_PLANKS_ITEM);
            place(event, ModBlocks.CRYSTAL_PLANKS_ITEM.toStack(), ModBlocks.CRYSTAL_STAIRS_ITEM);
            place(event, ModBlocks.CRYSTAL_STAIRS_ITEM.toStack(), ModBlocks.CRYSTAL_SLAB_ITEM);
            place(event, ModBlocks.CRYSTAL_SLAB_ITEM.toStack(), ModBlocks.CRYSTAL_FENCE_ITEM);
            place(event, ModBlocks.CRYSTAL_FENCE_ITEM.toStack(), ModBlocks.CRYSTAL_FENCE_GATE_ITEM);
            place(event, ModBlocks.CRYSTAL_FENCE_GATE_ITEM.toStack(), ModBlocks.CRYSTAL_DOOR_ITEM);
            place(event, ModBlocks.CRYSTAL_DOOR_ITEM.toStack(), ModBlocks.CRYSTAL_TRAPDOOR_ITEM);
            place(event, ModBlocks.CRYSTAL_TRAPDOOR_ITEM.toStack(), ModBlocks.CRYSTAL_PRESSURE_PLATE_ITEM);
            place(event, ModBlocks.CRYSTAL_PRESSURE_PLATE_ITEM.toStack(), ModBlocks.CRYSTAL_BUTTON_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            place(event, new ItemStack(Items.GRASS_BLOCK), ModBlocks.CRYSTAL_GRASS_BLOCK_ITEM);
            place(event, ModBlocks.CRYSTAL_GRASS_BLOCK_ITEM.toStack(), ModBlocks.CRYSTAL_DIRT_ITEM);
            place(event, ModBlocks.CRYSTAL_DIRT_ITEM.toStack(), ModBlocks.COARSE_CRYSTAL_DIRT_ITEM);
            place(event, ModBlocks.COARSE_CRYSTAL_DIRT_ITEM.toStack(), ModBlocks.ROOTED_CRYSTAL_DIRT_ITEM);
            place(event, ModBlocks.ROOTED_CRYSTAL_DIRT_ITEM.toStack(), ModBlocks.CRYSTAL_DIRT_PATH_ITEM);
            place(event, ModBlocks.CRYSTAL_DIRT_PATH_ITEM.toStack(), ModBlocks.CRYSTAL_FARMLAND_ITEM);
            place(event, new ItemStack(Items.CHERRY_LOG), ModBlocks.CRYSTAL_LOG_ITEM);
            place(event, new ItemStack(Items.CHERRY_LEAVES), ModBlocks.CRYSTAL_LEAVES_ITEM);
            place(event, new ItemStack(Items.SHORT_GRASS), ModBlocks.CRYSTAL_GRASS_ITEM);
            place(event, new ItemStack(Items.ALLIUM), ModBlocks.CRYSTAL_BLOOM_ITEM);
            place(event, ModBlocks.CRYSTAL_BLOOM_ITEM.toStack(), ModBlocks.SHARD_BLOSSOM_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            place(event, new ItemStack(Items.SPECTRAL_ARROW), ModItems.RESONANT_ARROW);
            place(event, new ItemStack(Items.IRON_SWORD), ModItems.RESONANT_SWORD);
            place(event, new ItemStack(Items.IRON_SPEAR), ModItems.RESONANT_SPEAR);
            place(event, new ItemStack(Items.IRON_AXE), ModItems.RESONANT_AXE);
            place(event, new ItemStack(Items.IRON_BOOTS), ModItems.RESONANT_HELMET);
            place(event, ModItems.RESONANT_HELMET.toStack(), ModItems.RESONANT_CHESTPLATE);
            place(event, ModItems.RESONANT_CHESTPLATE.toStack(), ModItems.RESONANT_LEGGINGS);
            place(event, ModItems.RESONANT_LEGGINGS.toStack(), ModItems.RESONANT_BOOTS);
            place(event, new ItemStack(Items.IRON_HORSE_ARMOR), ModItems.RESONANT_HORSE_ARMOR);
            place(event, new ItemStack(Items.WOLF_ARMOR), ModItems.RESONANT_WOLF_ARMOR);
            place(event, new ItemStack(Items.IRON_NAUTILUS_ARMOR), ModItems.RESONANT_NAUTILUS_ARMOR);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            place(event, new ItemStack(Items.IRON_HOE), ModItems.RESONANT_SHOVEL);
            place(event, ModItems.RESONANT_SHOVEL.toStack(), ModItems.RESONANT_PICKAXE);
            place(event, ModItems.RESONANT_PICKAXE.toStack(), ModItems.RESONANT_AXE);
            place(event, ModItems.RESONANT_AXE.toStack(), ModItems.RESONANT_HOE);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            place(event, new ItemStack(Items.SOUL_LANTERN), ModBlocks.RESONANT_LANTERN_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            place(event, new ItemStack(Items.DAYLIGHT_DETECTOR), ModBlocks.FREQUENCY_RELAY_ITEM);
        }

        // Add totem to combat tab
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            place(event, new ItemStack(Items.TOTEM_OF_UNDYING), ModItems.RESONANT_TOTEM);
        }

    }
}
