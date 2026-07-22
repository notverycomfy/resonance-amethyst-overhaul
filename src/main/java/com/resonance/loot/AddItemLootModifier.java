package com.resonance.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemLootModifier extends LootModifier {

    public static final MapCodec<AddItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).and(
                    instance.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item),
                            Codec.INT.fieldOf("min").forGetter(m -> m.min),
                            Codec.INT.fieldOf("max").forGetter(m -> m.max),
                            Codec.INT.optionalFieldOf("enchant_level", 0).forGetter(m -> m.enchantLevel)
                    )
            ).apply(instance, AddItemLootModifier::new)
    );

    private final Item item;
    private final int min;
    private final int max;
    private final int enchantLevel;

    public AddItemLootModifier(LootItemCondition[] conditions, int priority, Item item, int min, int max, int enchantLevel) {
        super(conditions, priority);
        this.item = item;
        this.min = min;
        this.max = max;
        this.enchantLevel = enchantLevel;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        int actualMin = Math.min(min, max);
        int actualMax = Math.max(min, max);
        int count = actualMin + context.getRandom().nextInt(actualMax - actualMin + 1);
        ItemStack stack = new ItemStack(item, count);
        if (enchantLevel > 0) {
            RandomSource random = context.getRandom();
            int level = enchantLevel / 2 + random.nextInt(enchantLevel / 2 + 1);
            stack = EnchantmentHelper.enchantItem(random, stack, level, context.getLevel().registryAccess(), java.util.Optional.empty());
        }
        generatedLoot.add(stack);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
