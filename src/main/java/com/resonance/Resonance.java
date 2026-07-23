package com.resonance;

import com.resonance.event.BrewingRecipeHandler;
import com.resonance.event.CrystalForestEvents;
import com.resonance.event.ModBusEvents;
import com.resonance.event.ResonanceEvents;
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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;

public final class Resonance implements ModInitializer {
    public static final String MODID = "resonance";

    @Override
    public void onInitialize() {
        bootstrapRegistries();
        ModBusEvents.register();
        ResonanceEvents.register();
        CrystalForestEvents.register();
        BrewingRecipeHandler.register();
        ModLootModifiers.register();
        registerCreativeTabs();
    }

    private static void bootstrapRegistries() {
        // Fabric registrations are immediate; touching each holder class performs
        // registration in dependency order before events and data packs load.
        ModBlocks.CRYSTAL_LOG.get();
        ModItems.AMETHYST_INGOT.get();
        ModBlockEntities.CHORUS_RESONATOR.get();
        ModEffects.RESONANCE.get();
        ModEntities.SHATTERED_ECHO.get();
        ModSounds.RESONANCE_CHIME.get();
        ModPotions.RESONANCE.get();
        ModWorldGen.AMETHYST_SPIRE.get();
        ModStructures.HARMONIC_ARENA_TYPE.get();
    }

    private static void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike anchor, ItemLike... entries) {
        CreativeModeTabEvents.modifyOutputEvent(tab).register(output ->
                output.insertAfter(anchor, Arrays.stream(entries).map(ItemStack::new).toList()));
    }

    private static void registerCreativeTabs() {
        addAfter(CreativeModeTabs.FOOD_AND_DRINKS, Items.RABBIT,
                ModItems.RAW_CRYSTAL_RABBIT.get(), ModItems.COOKED_CRYSTAL_RABBIT.get(),
                ModItems.CRYSTAL_RABBIT_STEW.get());
        addAfter(CreativeModeTabs.INGREDIENTS, Items.AMETHYST_SHARD,
                ModItems.AMETHYST_INGOT.get(), ModItems.HARMONIC_FRAGMENT.get(),
                ModItems.WHISPER_FRAGMENT.get(), ModItems.CRYSTAL_FRAGMENT.get(),
                ModItems.CRYSTAL_SCUTE.get());
        addAfter(CreativeModeTabs.SPAWN_EGGS, Items.ARMADILLO_SPAWN_EGG,
                ModItems.CRYSTAL_ARMADILLO_SPAWN_EGG.get(), ModItems.CRYSTAL_RABBIT_SPAWN_EGG.get(),
                ModItems.SHATTERED_ECHO_SPAWN_EGG.get(), ModItems.CRYSTAL_WRAITH_SPAWN_EGG.get(),
                ModItems.RESONANT_STALKER_SPAWN_EGG.get(), ModItems.CRYSTAL_SENTINEL_SPAWN_EGG.get());
        addAfter(CreativeModeTabs.BUILDING_BLOCKS, Items.CHERRY_BUTTON,
                ModBlocks.CRYSTAL_LOG.get(), ModBlocks.CRYSTAL_WOOD.get(),
                ModBlocks.STRIPPED_CRYSTAL_LOG.get(), ModBlocks.STRIPPED_CRYSTAL_WOOD.get(),
                ModBlocks.CRYSTAL_PLANKS.get(), ModBlocks.CRYSTAL_STAIRS.get(),
                ModBlocks.CRYSTAL_SLAB.get(), ModBlocks.CRYSTAL_FENCE.get(),
                ModBlocks.CRYSTAL_FENCE_GATE.get(), ModBlocks.CRYSTAL_DOOR.get(),
                ModBlocks.CRYSTAL_TRAPDOOR.get(), ModBlocks.CRYSTAL_PRESSURE_PLATE.get(),
                ModBlocks.CRYSTAL_BUTTON.get());
        addAfter(CreativeModeTabs.NATURAL_BLOCKS, Items.GRASS_BLOCK,
                ModBlocks.CRYSTAL_GRASS_BLOCK.get(), ModBlocks.CRYSTAL_DIRT.get(),
                ModBlocks.COARSE_CRYSTAL_DIRT.get(), ModBlocks.ROOTED_CRYSTAL_DIRT.get(),
                ModBlocks.CRYSTAL_DIRT_PATH.get(), ModBlocks.CRYSTAL_FARMLAND.get(),
                ModBlocks.CRYSTAL_LOG.get(), ModBlocks.CRYSTAL_LEAVES.get(),
                ModBlocks.CRYSTAL_GRASS.get(), ModBlocks.CRYSTAL_BLOOM.get(),
                ModBlocks.SHARD_BLOSSOM.get());
        addAfter(CreativeModeTabs.COMBAT, Items.SPECTRAL_ARROW,
                ModItems.RESONANT_ARROW.get(), ModItems.RESONANT_SWORD.get(),
                ModItems.RESONANT_SPEAR.get(), ModItems.RESONANT_AXE.get(),
                ModItems.RESONANT_HELMET.get(), ModItems.RESONANT_CHESTPLATE.get(),
                ModItems.RESONANT_LEGGINGS.get(), ModItems.RESONANT_BOOTS.get(),
                ModItems.RESONANT_HORSE_ARMOR.get(), ModItems.RESONANT_WOLF_ARMOR.get(),
                ModItems.RESONANT_NAUTILUS_ARMOR.get(), ModItems.RESONANT_TOTEM.get());
        addAfter(CreativeModeTabs.TOOLS_AND_UTILITIES, Items.IRON_HOE,
                ModItems.RESONANT_SHOVEL.get(), ModItems.RESONANT_PICKAXE.get(),
                ModItems.RESONANT_AXE.get(), ModItems.RESONANT_HOE.get());
        addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.SOUL_LANTERN,
                ModBlocks.RESONANT_LANTERN.get());
        addAfter(CreativeModeTabs.REDSTONE_BLOCKS, Items.DAYLIGHT_DETECTOR,
                ModBlocks.FREQUENCY_RELAY.get());
    }
}
