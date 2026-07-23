package com.resonance.fabric.registry;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
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
}
