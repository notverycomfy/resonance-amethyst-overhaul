package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.block.ChorusResonatorBlock;
import com.resonance.block.CrystalLogBlock;
import com.resonance.block.CrystalPlantBlock;
import com.resonance.block.CrystalGrassBlock;
import com.resonance.block.CrystalDirtPathBlock;
import com.resonance.block.CrystalFarmlandBlock;
import com.resonance.block.ResonantLanternBlock;
import com.resonance.block.FrequencyRelayBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Resonance.MODID);

    /** Crystal controls keep the wooden behavior while using one sound palette. */
    private static final BlockSetType CRYSTAL_BLOCK_SET = BlockSetType.register(new BlockSetType(
            "resonance:crystal",
            true,
            true,
            true,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
            SoundType.AMETHYST,
            SoundType.AMETHYST.getBreakSound(),
            SoundType.AMETHYST.getPlaceSound(),
            SoundType.AMETHYST.getBreakSound(),
            SoundType.AMETHYST.getPlaceSound(),
            SoundType.AMETHYST.getHitSound(),
            SoundType.AMETHYST.getPlaceSound(),
            SoundType.AMETHYST.getHitSound(),
            SoundType.AMETHYST.getPlaceSound()));

    private static final WoodType CRYSTAL_WOOD_TYPE = WoodType.register(new WoodType(
            "resonance:crystal",
            CRYSTAL_BLOCK_SET,
            SoundType.AMETHYST,
            SoundType.AMETHYST,
            SoundType.AMETHYST.getBreakSound(),
            SoundType.AMETHYST.getPlaceSound()));

    public static final DeferredBlock<Block> RESONANT_LANTERN = BLOCKS.registerBlock("resonant_lantern",
            ResonantLanternBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .strength(3.5F)
                    .sound(SoundType.LANTERN)
                    .lightLevel(state -> 15)
                    .noOcclusion());

    public static final DeferredBlock<Block> FREQUENCY_RELAY = BLOCKS.registerBlock("frequency_relay",
            FrequencyRelayBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .strength(3.5F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 4)
                    .noOcclusion());

    public static final DeferredBlock<Block> CHORUS_RESONATOR = BLOCKS.registerBlock("chorus_resonator",
            ChorusResonatorBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(ChorusResonatorBlock.ACTIVE) ? 12 : 4)
                    .noOcclusion());

    // --- Crystal wood set ---
    private static BlockBehaviour.Properties woodProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .instrument(NoteBlockInstrument.CHIME)
                .strength(2.0F)
                .sound(SoundType.AMETHYST);
    }

    public static final DeferredBlock<Block> STRIPPED_CRYSTAL_LOG = BLOCKS.registerBlock("stripped_crystal_log",
            RotatedPillarBlock::new, ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_LOG = BLOCKS.registerBlock("crystal_log",
            props -> new CrystalLogBlock(STRIPPED_CRYSTAL_LOG::get, props), ModBlocks::woodProps);

    public static final DeferredBlock<Block> STRIPPED_CRYSTAL_WOOD = BLOCKS.registerBlock("stripped_crystal_wood",
            RotatedPillarBlock::new, ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_WOOD = BLOCKS.registerBlock("crystal_wood",
            props -> new CrystalLogBlock(STRIPPED_CRYSTAL_WOOD::get, props), ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_PLANKS = BLOCKS.registerBlock("crystal_planks",
            Block::new, ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_STAIRS = BLOCKS.registerBlock("crystal_stairs",
            props -> new StairBlock(CRYSTAL_PLANKS.get().defaultBlockState(), props), ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_SLAB = BLOCKS.registerBlock("crystal_slab",
            SlabBlock::new, ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_FENCE = BLOCKS.registerBlock("crystal_fence",
            FenceBlock::new, ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_FENCE_GATE = BLOCKS.registerBlock("crystal_fence_gate",
            props -> new FenceGateBlock(CRYSTAL_WOOD_TYPE, props), ModBlocks::woodProps);

    public static final DeferredBlock<Block> CRYSTAL_DOOR = BLOCKS.registerBlock("crystal_door",
            props -> new DoorBlock(CRYSTAL_BLOCK_SET, props),
            () -> woodProps().noOcclusion().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> CRYSTAL_TRAPDOOR = BLOCKS.registerBlock("crystal_trapdoor",
            props -> new TrapDoorBlock(CRYSTAL_BLOCK_SET, props),
            () -> woodProps().noOcclusion());

    public static final DeferredBlock<Block> CRYSTAL_BUTTON = BLOCKS.registerBlock("crystal_button",
            props -> new ButtonBlock(CRYSTAL_BLOCK_SET, 30, props),
            () -> BlockBehaviour.Properties.of().noCollision().strength(0.5F).sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> CRYSTAL_PRESSURE_PLATE = BLOCKS.registerBlock("crystal_pressure_plate",
            props -> new PressurePlateBlock(CRYSTAL_BLOCK_SET, props),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollision()
                    .strength(0.5F).sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> CRYSTAL_LEAVES = BLOCKS.registerBlock("crystal_leaves",
            props -> new net.minecraft.world.level.block.UntintedParticleLeavesBlock(0.02F, net.minecraft.core.particles.ParticleTypes.END_ROD, props),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .noOcclusion()
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)
                    .pushReaction(PushReaction.DESTROY));

    // --- Crystal ground and flora ---
    public static final DeferredBlock<Block> CRYSTAL_DIRT = BLOCKS.registerBlock("crystal_dirt", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DIRT).sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> COARSE_CRYSTAL_DIRT = BLOCKS.registerBlock("coarse_crystal_dirt", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.COARSE_DIRT).sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> ROOTED_CRYSTAL_DIRT = BLOCKS.registerBlock("rooted_crystal_dirt",
            net.minecraft.world.level.block.RootedDirtBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.ROOTED_DIRT).sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> CRYSTAL_DIRT_PATH = BLOCKS.registerBlock("crystal_dirt_path", CrystalDirtPathBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DIRT_PATH).sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> CRYSTAL_FARMLAND = BLOCKS.registerBlock("crystal_farmland", CrystalFarmlandBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.FARMLAND).sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> CRYSTAL_GRASS_BLOCK = BLOCKS.registerBlock("crystal_grass_block",
            CrystalGrassBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.AMETHYST));

    private static BlockBehaviour.Properties plantProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PINK)
                .noCollision()
                .instabreak()
                .sound(SoundType.AMETHYST_CLUSTER)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY);
    }

    public static final DeferredBlock<Block> CRYSTAL_GRASS = BLOCKS.registerBlock("crystal_grass",
            CrystalPlantBlock::new, ModBlocks::plantProps);

    public static final DeferredBlock<Block> CRYSTAL_BLOOM = BLOCKS.registerBlock("crystal_bloom",
            CrystalPlantBlock::new, () -> plantProps().lightLevel(state -> 3));

    public static final DeferredBlock<Block> SHARD_BLOSSOM = BLOCKS.registerBlock("shard_blossom",
            CrystalPlantBlock::new, () -> plantProps().lightLevel(state -> 5));

    // Block items registered through ModItems.ITEMS
    public static final DeferredItem<BlockItem> RESONANT_LANTERN_ITEM = ModItems.ITEMS.registerSimpleBlockItem(RESONANT_LANTERN);
    public static final DeferredItem<BlockItem> FREQUENCY_RELAY_ITEM = ModItems.ITEMS.registerSimpleBlockItem(FREQUENCY_RELAY);
    public static final DeferredItem<BlockItem> CHORUS_RESONATOR_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CHORUS_RESONATOR);

    public static final DeferredItem<BlockItem> CRYSTAL_LOG_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_LOG);
    public static final DeferredItem<BlockItem> STRIPPED_CRYSTAL_LOG_ITEM = ModItems.ITEMS.registerSimpleBlockItem(STRIPPED_CRYSTAL_LOG);
    public static final DeferredItem<BlockItem> CRYSTAL_WOOD_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_WOOD);
    public static final DeferredItem<BlockItem> STRIPPED_CRYSTAL_WOOD_ITEM = ModItems.ITEMS.registerSimpleBlockItem(STRIPPED_CRYSTAL_WOOD);
    public static final DeferredItem<BlockItem> CRYSTAL_PLANKS_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_PLANKS);
    public static final DeferredItem<BlockItem> CRYSTAL_STAIRS_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_STAIRS);
    public static final DeferredItem<BlockItem> CRYSTAL_SLAB_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_SLAB);
    public static final DeferredItem<BlockItem> CRYSTAL_FENCE_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_FENCE);
    public static final DeferredItem<BlockItem> CRYSTAL_FENCE_GATE_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_FENCE_GATE);
    public static final DeferredItem<Item> CRYSTAL_DOOR_ITEM = ModItems.ITEMS.registerItem("crystal_door",
            props -> new DoubleHighBlockItem(CRYSTAL_DOOR.get(), props.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CRYSTAL_TRAPDOOR_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_TRAPDOOR);
    public static final DeferredItem<BlockItem> CRYSTAL_BUTTON_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_BUTTON);
    public static final DeferredItem<BlockItem> CRYSTAL_PRESSURE_PLATE_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> CRYSTAL_LEAVES_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_LEAVES);
    public static final DeferredItem<BlockItem> CRYSTAL_DIRT_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_DIRT);
    public static final DeferredItem<BlockItem> COARSE_CRYSTAL_DIRT_ITEM = ModItems.ITEMS.registerSimpleBlockItem(COARSE_CRYSTAL_DIRT);
    public static final DeferredItem<BlockItem> ROOTED_CRYSTAL_DIRT_ITEM = ModItems.ITEMS.registerSimpleBlockItem(ROOTED_CRYSTAL_DIRT);
    public static final DeferredItem<BlockItem> CRYSTAL_DIRT_PATH_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_DIRT_PATH);
    public static final DeferredItem<BlockItem> CRYSTAL_FARMLAND_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_FARMLAND);
    public static final DeferredItem<BlockItem> CRYSTAL_GRASS_BLOCK_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_GRASS_BLOCK);
    public static final DeferredItem<BlockItem> CRYSTAL_GRASS_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_GRASS);
    public static final DeferredItem<BlockItem> CRYSTAL_BLOOM_ITEM = ModItems.ITEMS.registerSimpleBlockItem(CRYSTAL_BLOOM);
    public static final DeferredItem<BlockItem> SHARD_BLOSSOM_ITEM = ModItems.ITEMS.registerSimpleBlockItem(SHARD_BLOSSOM);
}
