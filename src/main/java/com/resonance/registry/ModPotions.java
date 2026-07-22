package com.resonance.registry;

import com.resonance.Resonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Resonance.MODID);

    public static final DeferredHolder<Potion, Potion> RESONANCE = POTIONS.register("resonance",
            () -> new Potion("resonance", new MobEffectInstance(ModEffects.RESONANCE, 100, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_RESONANCE = POTIONS.register("long_resonance",
            () -> new Potion("resonance", new MobEffectInstance(ModEffects.RESONANCE, 200, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_RESONANCE = POTIONS.register("strong_resonance",
            () -> new Potion("resonance", new MobEffectInstance(ModEffects.RESONANCE, 100, 1)));

    public static final DeferredHolder<Potion, Potion> STRONGEST_RESONANCE = POTIONS.register("strongest_resonance",
            () -> new Potion("resonance", new MobEffectInstance(ModEffects.RESONANCE, 100, 2)));
}
