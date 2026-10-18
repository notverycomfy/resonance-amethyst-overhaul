package com.resonance.registry;

import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class ModBlockTransformations {
    private ModBlockTransformations() {
    }

    public static void register() {
        BlockTransformerHelper.registerStripping(ModBlocks.CRYSTAL_LOG.get(),
                new CopyPropertiesProvider(ModBlocks.STRIPPED_CRYSTAL_LOG.get()));
        BlockTransformerHelper.registerStripping(ModBlocks.CRYSTAL_WOOD.get(),
                new CopyPropertiesProvider(ModBlocks.STRIPPED_CRYSTAL_WOOD.get()));
        BlockTransformerHelper.registerFlattening(new Block[] {
                ModBlocks.CRYSTAL_DIRT.get(), ModBlocks.CRYSTAL_GRASS_BLOCK.get(),
                ModBlocks.COARSE_CRYSTAL_DIRT.get(), ModBlocks.ROOTED_CRYSTAL_DIRT.get()
        }, ModBlocks.CRYSTAL_DIRT_PATH.get());
        BlockTransformerHelper.registerTilling(new Block[] {
                ModBlocks.CRYSTAL_DIRT.get(), ModBlocks.CRYSTAL_GRASS_BLOCK.get()
        }, ModBlocks.CRYSTAL_FARMLAND.get());
        BlockTransformerHelper.registerTilling(ModBlocks.COARSE_CRYSTAL_DIRT.get(), ModBlocks.CRYSTAL_DIRT.get());
        BlockTransformerHelper.registerHoe(BlockTransformer.BlockTransformData.builder(
                        BlockPredicate.matchesBlocks(ModBlocks.ROOTED_CRYSTAL_DIRT.get()), ModBlocks.CRYSTAL_DIRT.get())
                .sound(SoundEvents.HOE_TILL)
                .loot(BuiltInLootTables.TILL_ROOTED_DIRT)
                .dropStrategy(BlockTransformer.DropStrategy.CLICKED_FACE)
                .build());
    }
}
