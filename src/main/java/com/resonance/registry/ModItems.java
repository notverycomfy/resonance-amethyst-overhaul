package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.item.ResonantArrowItem;
import com.resonance.item.ResonantAxeItem;
import com.resonance.item.ResonantHoeItem;
import com.resonance.item.ResonantPickaxeItem;
import com.resonance.item.ResonantShovelItem;
import com.resonance.item.ResonantSpearItem;
import com.resonance.item.ResonantSwordItem;
import com.resonance.item.ResonantArmorItem;
import com.resonance.item.TooltipItem;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import com.resonance.fabric.registry.DeferredItem;
import com.resonance.fabric.registry.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Resonance.MODID);

    public static final TagKey<Item> REPAIRS_RESONANT_TOOLS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Resonance.MODID, "repairs_resonant_tools"));

    public static final ResourceKey<EquipmentAsset> RESONANT_ARMOR_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Resonance.MODID, "resonant"));

    public static final ArmorMaterial RESONANT_ARMOR_MATERIAL = new ArmorMaterial(
            15,
            Map.of(
                    ArmorType.HELMET, 3,
                    ArmorType.CHESTPLATE, 7,
                    ArmorType.LEGGINGS, 6,
                    ArmorType.BOOTS, 3
            ),
            20,
            Holder.direct(SoundEvents.AMETHYST_BLOCK_CHIME),
            1.0F,
            0.0F,
            REPAIRS_RESONANT_TOOLS,
            RESONANT_ARMOR_ASSET
    );

    public static final ArmorMaterial RESONANT_MOUNT_MATERIAL = new ArmorMaterial(
            15,
            Map.of(ArmorType.BODY, 15),
            20,
            Holder.direct(SoundEvents.AMETHYST_BLOCK_CHIME),
            2.0F,
            0.0F,
            REPAIRS_RESONANT_TOOLS,
            RESONANT_ARMOR_ASSET
    );

    public static final ToolMaterial RESONANT_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 780, 7.0F, 2.5F, 20, REPAIRS_RESONANT_TOOLS);

    private static final FoodProperties RAW_CRYSTAL_RABBIT_FOOD = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.4F).build();
    private static final FoodProperties COOKED_CRYSTAL_RABBIT_FOOD = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();
    private static final FoodProperties CRYSTAL_RABBIT_STEW_FOOD = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F).build();

    public static final DeferredItem<Item> AMETHYST_INGOT = ITEMS.registerSimpleItem("amethyst_ingot",
            () -> new Item.Properties());

    // Tools
    public static final DeferredItem<Item> RESONANT_SWORD = ITEMS.registerItem("resonant_sword",
            ResonantSwordItem::new,
            () -> new Item.Properties().sword(RESONANT_MATERIAL, 3.0F, -2.4F));

    public static final DeferredItem<Item> RESONANT_PICKAXE = ITEMS.registerItem("resonant_pickaxe",
            ResonantPickaxeItem::new,
            () -> new Item.Properties().pickaxe(RESONANT_MATERIAL, 1.0F, -2.8F));

    public static final DeferredItem<Item> RESONANT_AXE = ITEMS.registerItem("resonant_axe",
            p -> new ResonantAxeItem(RESONANT_MATERIAL, 6.0F, -3.1F, p),
            () -> new Item.Properties());

    public static final DeferredItem<Item> RESONANT_SHOVEL = ITEMS.registerItem("resonant_shovel",
            p -> new ResonantShovelItem(RESONANT_MATERIAL, 1.5F, -3.0F, p),
            () -> new Item.Properties());

    public static final DeferredItem<Item> RESONANT_HOE = ITEMS.registerItem("resonant_hoe",
            p -> new ResonantHoeItem(RESONANT_MATERIAL, -2.0F, -1.0F, p),
            () -> new Item.Properties());

    public static final DeferredItem<Item> RESONANT_SPEAR = ITEMS.registerItem("resonant_spear",
            ResonantSpearItem::new,
            () -> new Item.Properties().spear(RESONANT_MATERIAL,
                    0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F));

    // Horse, Nautilus & Wolf Armor
    public static final DeferredItem<Item> RESONANT_HORSE_ARMOR = ITEMS.registerItem("resonant_horse_armor",
            TooltipItem::new,
            () -> new Item.Properties().horseArmor(RESONANT_MOUNT_MATERIAL)
                    .rarity(net.minecraft.world.item.Rarity.EPIC));

    public static final DeferredItem<Item> RESONANT_NAUTILUS_ARMOR = ITEMS.registerItem("resonant_nautilus_armor",
            TooltipItem::new,
            () -> new Item.Properties().nautilusArmor(RESONANT_MOUNT_MATERIAL)
                    .rarity(net.minecraft.world.item.Rarity.EPIC));

    public static final DeferredItem<Item> RESONANT_WOLF_ARMOR = ITEMS.registerItem("resonant_wolf_armor",
            TooltipItem::new,
            () -> new Item.Properties().wolfArmor(RESONANT_MOUNT_MATERIAL));

    // Totem
    public static final DeferredItem<Item> RESONANT_TOTEM = ITEMS.registerItem("resonant_totem",
            com.resonance.item.ResonantTotemItem::new,
            () -> new Item.Properties()
                    .stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .component(net.minecraft.core.component.DataComponents.DEATH_PROTECTION,
                            net.minecraft.world.item.component.DeathProtection.TOTEM_OF_UNDYING));

    // Arrow
    public static final DeferredItem<Item> RESONANT_ARROW = ITEMS.registerItem("resonant_arrow",
            ResonantArrowItem::new,
            () -> new Item.Properties());

    // Armor
    public static final DeferredItem<Item> RESONANT_HELMET = ITEMS.registerItem("resonant_helmet",
            ResonantArmorItem::new,
            () -> new Item.Properties().humanoidArmor(RESONANT_ARMOR_MATERIAL, ArmorType.HELMET));

    public static final DeferredItem<Item> RESONANT_CHESTPLATE = ITEMS.registerItem("resonant_chestplate",
            ResonantArmorItem::new,
            () -> new Item.Properties().humanoidArmor(RESONANT_ARMOR_MATERIAL, ArmorType.CHESTPLATE));

    public static final DeferredItem<Item> RESONANT_LEGGINGS = ITEMS.registerItem("resonant_leggings",
            ResonantArmorItem::new,
            () -> new Item.Properties().humanoidArmor(RESONANT_ARMOR_MATERIAL, ArmorType.LEGGINGS));

    public static final DeferredItem<Item> RESONANT_BOOTS = ITEMS.registerItem("resonant_boots",
            ResonantArmorItem::new,
            () -> new Item.Properties().humanoidArmor(RESONANT_ARMOR_MATERIAL, ArmorType.BOOTS));

    // Mob drops
    public static final DeferredItem<Item> HARMONIC_FRAGMENT = ITEMS.registerItem("harmonic_fragment",
            TooltipItem::new,
            () -> new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON));

    public static final DeferredItem<Item> WHISPER_FRAGMENT = ITEMS.registerItem("whisper_fragment",
            TooltipItem::new,
            () -> new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON));

    public static final DeferredItem<Item> CRYSTAL_FRAGMENT = ITEMS.registerItem("crystal_fragment",
            TooltipItem::new,
            () -> new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE));

    // Render-only item for the Harmonic's crystal shard projectile; not in any creative tab
    public static final DeferredItem<Item> CRYSTAL_SHARD_PROJECTILE = ITEMS.registerItem("crystal_shard_projectile",
            Item::new, Item.Properties::new);

    // Spawn eggs
    public static final DeferredItem<Item> SHATTERED_ECHO_SPAWN_EGG = ITEMS.registerItem("shattered_echo_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.SHATTERED_ECHO.get()));

    public static final DeferredItem<Item> RESONANT_STALKER_SPAWN_EGG = ITEMS.registerItem("resonant_stalker_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.RESONANT_STALKER.get()));

    public static final DeferredItem<Item> CRYSTAL_WRAITH_SPAWN_EGG = ITEMS.registerItem("crystal_wraith_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRYSTAL_WRAITH.get()));

    public static final DeferredItem<Item> CRYSTAL_RABBIT_SPAWN_EGG = ITEMS.registerItem("crystal_rabbit_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRYSTAL_RABBIT.get()));

    public static final DeferredItem<Item> CRYSTAL_ARMADILLO_SPAWN_EGG = ITEMS.registerItem("crystal_armadillo_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRYSTAL_ARMADILLO.get()));

    public static final DeferredItem<Item> CRYSTAL_SCUTE = ITEMS.registerItem("crystal_scute",
            TooltipItem::new,
            () -> new Item.Properties());

    // Crystal rabbit food
    public static final DeferredItem<Item> RAW_CRYSTAL_RABBIT = ITEMS.registerSimpleItem("raw_crystal_rabbit",
            () -> new Item.Properties().food(RAW_CRYSTAL_RABBIT_FOOD));

    public static final DeferredItem<Item> COOKED_CRYSTAL_RABBIT = ITEMS.registerSimpleItem("cooked_crystal_rabbit",
            () -> new Item.Properties().food(COOKED_CRYSTAL_RABBIT_FOOD));

    public static final DeferredItem<Item> CRYSTAL_RABBIT_STEW = ITEMS.registerSimpleItem("crystal_rabbit_stew",
            () -> new Item.Properties().stacksTo(1).food(CRYSTAL_RABBIT_STEW_FOOD).usingConvertsTo(Items.BOWL));

    public static final DeferredItem<Item> CRYSTAL_SENTINEL_SPAWN_EGG = ITEMS.registerItem("crystal_sentinel_spawn_egg",
            net.minecraft.world.item.SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRYSTAL_SENTINEL.get()));
}
