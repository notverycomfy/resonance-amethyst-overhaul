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
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;

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

    private static void place(FabricCreativeModeTabOutput output, ItemLike anchor, ItemLike entry) {
        output.getDisplayStacks().removeIf(stack -> stack.is(entry.asItem()));
        output.getSearchTabStacks().removeIf(stack -> stack.is(entry.asItem()));
        output.insertAfter(anchor, List.of(new ItemStack(entry)));
    }

    private static void registerCreativeTabs() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
            place(output, Items.RABBIT, ModItems.RAW_CRYSTAL_RABBIT.get());
            place(output, Items.COOKED_RABBIT, ModItems.COOKED_CRYSTAL_RABBIT.get());
            place(output, Items.RABBIT_STEW, ModItems.CRYSTAL_RABBIT_STEW.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            place(output, Items.AMETHYST_SHARD, ModItems.AMETHYST_INGOT.get());
            place(output, ModItems.AMETHYST_INGOT.get(), ModItems.HARMONIC_FRAGMENT.get());
            place(output, ModItems.HARMONIC_FRAGMENT.get(), ModItems.WHISPER_FRAGMENT.get());
            place(output, ModItems.WHISPER_FRAGMENT.get(), ModItems.CRYSTAL_FRAGMENT.get());
            place(output, Items.ARMADILLO_SCUTE, ModItems.CRYSTAL_SCUTE.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> {
            place(output, Items.ARMADILLO_SPAWN_EGG, ModItems.CRYSTAL_ARMADILLO_SPAWN_EGG.get());
            place(output, Items.RABBIT_SPAWN_EGG, ModItems.CRYSTAL_RABBIT_SPAWN_EGG.get());
            place(output, Items.SKELETON_SPAWN_EGG, ModItems.SHATTERED_ECHO_SPAWN_EGG.get());
            place(output, Items.PHANTOM_SPAWN_EGG, ModItems.CRYSTAL_WRAITH_SPAWN_EGG.get());
            place(output, Items.ENDERMAN_SPAWN_EGG, ModItems.RESONANT_STALKER_SPAWN_EGG.get());
            place(output, Items.SHULKER_SPAWN_EGG, ModItems.CRYSTAL_SENTINEL_SPAWN_EGG.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            place(output, Items.CHERRY_BUTTON, ModBlocks.CRYSTAL_LOG.get());
            place(output, ModBlocks.CRYSTAL_LOG.get(), ModBlocks.CRYSTAL_WOOD.get());
            place(output, ModBlocks.CRYSTAL_WOOD.get(), ModBlocks.STRIPPED_CRYSTAL_LOG.get());
            place(output, ModBlocks.STRIPPED_CRYSTAL_LOG.get(), ModBlocks.STRIPPED_CRYSTAL_WOOD.get());
            place(output, ModBlocks.STRIPPED_CRYSTAL_WOOD.get(), ModBlocks.CRYSTAL_PLANKS.get());
            place(output, ModBlocks.CRYSTAL_PLANKS.get(), ModBlocks.CRYSTAL_STAIRS.get());
            place(output, ModBlocks.CRYSTAL_STAIRS.get(), ModBlocks.CRYSTAL_SLAB.get());
            place(output, ModBlocks.CRYSTAL_SLAB.get(), ModBlocks.CRYSTAL_FENCE.get());
            place(output, ModBlocks.CRYSTAL_FENCE.get(), ModBlocks.CRYSTAL_FENCE_GATE.get());
            place(output, ModBlocks.CRYSTAL_FENCE_GATE.get(), ModBlocks.CRYSTAL_DOOR.get());
            place(output, ModBlocks.CRYSTAL_DOOR.get(), ModBlocks.CRYSTAL_TRAPDOOR.get());
            place(output, ModBlocks.CRYSTAL_TRAPDOOR.get(), ModBlocks.CRYSTAL_PRESSURE_PLATE.get());
            place(output, ModBlocks.CRYSTAL_PRESSURE_PLATE.get(), ModBlocks.CRYSTAL_BUTTON.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> {
            place(output, Items.GRASS_BLOCK, ModBlocks.CRYSTAL_GRASS_BLOCK.get());
            place(output, ModBlocks.CRYSTAL_GRASS_BLOCK.get(), ModBlocks.CRYSTAL_DIRT.get());
            place(output, ModBlocks.CRYSTAL_DIRT.get(), ModBlocks.COARSE_CRYSTAL_DIRT.get());
            place(output, ModBlocks.COARSE_CRYSTAL_DIRT.get(), ModBlocks.ROOTED_CRYSTAL_DIRT.get());
            place(output, ModBlocks.ROOTED_CRYSTAL_DIRT.get(), ModBlocks.CRYSTAL_DIRT_PATH.get());
            place(output, ModBlocks.CRYSTAL_DIRT_PATH.get(), ModBlocks.CRYSTAL_FARMLAND.get());
            place(output, Items.CHERRY_LOG, ModBlocks.CRYSTAL_LOG.get());
            place(output, Items.CHERRY_LEAVES, ModBlocks.CRYSTAL_LEAVES.get());
            place(output, Items.SHORT_GRASS, ModBlocks.CRYSTAL_GRASS.get());
            place(output, Items.ALLIUM, ModBlocks.CRYSTAL_BLOOM.get());
            place(output, ModBlocks.CRYSTAL_BLOOM.get(), ModBlocks.SHARD_BLOSSOM.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
            place(output, Items.SPECTRAL_ARROW, ModItems.RESONANT_ARROW.get());
            place(output, Items.IRON_SWORD, ModItems.RESONANT_SWORD.get());
            place(output, Items.IRON_SPEAR, ModItems.RESONANT_SPEAR.get());
            place(output, Items.IRON_AXE, ModItems.RESONANT_AXE.get());
            place(output, Items.IRON_BOOTS, ModItems.RESONANT_HELMET.get());
            place(output, ModItems.RESONANT_HELMET.get(), ModItems.RESONANT_CHESTPLATE.get());
            place(output, ModItems.RESONANT_CHESTPLATE.get(), ModItems.RESONANT_LEGGINGS.get());
            place(output, ModItems.RESONANT_LEGGINGS.get(), ModItems.RESONANT_BOOTS.get());
            place(output, Items.IRON_HORSE_ARMOR, ModItems.RESONANT_HORSE_ARMOR.get());
            place(output, Items.WOLF_ARMOR, ModItems.RESONANT_WOLF_ARMOR.get());
            place(output, Items.IRON_NAUTILUS_ARMOR, ModItems.RESONANT_NAUTILUS_ARMOR.get());
            place(output, Items.TOTEM_OF_UNDYING, ModItems.RESONANT_TOTEM.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            place(output, Items.IRON_HOE, ModItems.RESONANT_SHOVEL.get());
            place(output, ModItems.RESONANT_SHOVEL.get(), ModItems.RESONANT_PICKAXE.get());
            place(output, ModItems.RESONANT_PICKAXE.get(), ModItems.RESONANT_AXE.get());
            place(output, ModItems.RESONANT_AXE.get(), ModItems.RESONANT_HOE.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output ->
                place(output, Items.SOUL_LANTERN, ModBlocks.RESONANT_LANTERN.get()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output ->
                place(output, Items.DAYLIGHT_DETECTOR, ModBlocks.FREQUENCY_RELAY.get()));
    }
}
