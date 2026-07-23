package com.resonance.fabric.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DeferredHolder<R, T extends R> implements Supplier<T>, Holder<T> {
    private final Identifier id;
    private T value;

    DeferredHolder(Identifier id) {
        this.id = id;
    }

    void bind(T value) {
        this.value = value;
    }

    public Identifier id() {
        return id;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value, "Registry value not bound: " + id);
    }

    private Holder<T> delegate() {
        return Holder.direct(get());
    }

    @Override public T value() { return get(); }
    @Override public boolean isBound() { return value != null; }
    @Override public boolean areComponentsBound() { return delegate().areComponentsBound(); }
    @Override public boolean is(Identifier identifier) { return id.equals(identifier); }
    @Override public boolean is(ResourceKey<T> key) { return id.equals(key.identifier()); }
    @Override public boolean is(Predicate<ResourceKey<T>> predicate) { return false; }
    @Override public boolean is(TagKey<T> tag) { return false; }
    @Override public boolean is(Holder<T> holder) { return holder.value() == get(); }
    @Override public Stream<TagKey<T>> tags() { return Stream.empty(); }
    @Override public DataComponentMap components() { return delegate().components(); }
    @Override public Either<ResourceKey<T>, T> unwrap() { return Either.right(get()); }
    @Override public Optional<ResourceKey<T>> unwrapKey() { return Optional.empty(); }
    @Override public Kind kind() { return Kind.DIRECT; }
    @Override public boolean canSerializeIn(HolderOwner<T> owner) { return true; }
}
