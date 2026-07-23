package com.resonance.fabric.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    DeferredItem(Identifier id) {
        super(id);
    }

    public ItemStack toStack() {
        return new ItemStack(get());
    }
}
