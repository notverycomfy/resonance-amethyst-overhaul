package com.resonance.registry;

import com.resonance.Resonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import com.resonance.fabric.registry.DeferredHolder;
import com.resonance.fabric.registry.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Resonance.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RESONANCE_CHIME = SOUND_EVENTS.register("resonance_chime",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, "resonance_chime")));

    public static final DeferredHolder<SoundEvent, SoundEvent> RESONANCE_PULSE = SOUND_EVENTS.register("resonance_pulse",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, "resonance_pulse")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HARMONIC_SHIELD_ABSORB = SOUND_EVENTS.register("harmonic_shield_absorb",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, "harmonic_shield_absorb")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HARMONIC_SHIELD_RECHARGE = SOUND_EVENTS.register("harmonic_shield_recharge",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, "harmonic_shield_recharge")));

    public static final DeferredHolder<SoundEvent, SoundEvent> STALKER_LAUGH = SOUND_EVENTS.register("stalker_laugh",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, "stalker_laugh")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_AMBIENT = registerVariable("entity.crystal_wraith.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_HURT = registerVariable("entity.crystal_wraith.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_DEATH = registerVariable("entity.crystal_wraith.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_EMERGE = registerVariable("entity.crystal_wraith.emerge");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_ARMOR_BREAK = registerVariable("entity.crystal_wraith.armor_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_WRAITH_ATTACK = registerVariable("entity.crystal_wraith.attack");

    private static DeferredHolder<SoundEvent, SoundEvent> registerVariable(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Resonance.MODID, name)));
    }
}
