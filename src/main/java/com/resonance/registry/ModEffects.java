package com.resonance.registry;

import com.resonance.Resonance;
import com.resonance.effect.ResonanceEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import com.resonance.fabric.registry.DeferredHolder;
import com.resonance.fabric.registry.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Resonance.MODID);

    /** Entities afflicted with Resonance take increased damage from all sources. */
    public static final DeferredHolder<MobEffect, MobEffect> RESONANCE = MOB_EFFECTS.register("resonance",
            () -> new ResonanceEffect(MobEffectCategory.HARMFUL, 0xB07EE0));
}
