package com.resonance.fabric.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final Identifier id;
    private T value;
    private Holder<T> holder;

    DeferredHolder(Identifier id) {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    void bind(T value, Holder<?> holder) {
        this.value = value;
        this.holder = (Holder<T>) holder;
    }

    public Identifier id() {
        return id;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value, "Registry value not bound: " + id);
    }

    public Holder<T> holder() {
        return Objects.requireNonNull(holder, "Registry holder not bound: " + id);
    }
}
