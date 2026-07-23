package com.resonance.fabric.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    DeferredBlock(Identifier id) {
        super(id);
    }
}
