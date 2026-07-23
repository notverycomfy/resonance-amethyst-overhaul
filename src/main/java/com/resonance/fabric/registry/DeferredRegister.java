package com.resonance.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredRegister<T> {
    protected final Registry<T> registry;
    protected final String namespace;

    @SuppressWarnings("unchecked")
    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        DeferredHolder<T, I> holder = new DeferredHolder<>(id);
        holder.bind(Registry.register(registry, id, supplier.get()));
        return holder;
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        public <B extends Block> DeferredBlock<B> registerBlock(
                String name, Function<BlockBehaviour.Properties, B> factory,
                Supplier<BlockBehaviour.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
            B block = factory.apply(properties.get().setId(key));
            DeferredBlock<B> holder = new DeferredBlock<>(id);
            holder.bind(Registry.register(registry, id, block));
            return holder;
        }
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        public <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, I> factory,
                Supplier<Item.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            I item = factory.apply(properties.get().setId(key));
            DeferredItem<I> holder = new DeferredItem<>(id);
            holder.bind(Registry.register(registry, id, item));
            return holder;
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, I> factory) {
            return registerItem(name, factory, Item.Properties::new);
        }

        public DeferredItem<Item> registerSimpleItem(String name, Supplier<Item.Properties> properties) {
            return registerItem(name, Item::new, properties);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredBlock<? extends Block> block) {
            String name = block.id().getPath();
            return registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
        }
    }
}
