package com.resonance;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RESONANCE_DAMAGE_BONUS = BUILDER
            .comment("Extra damage multiplier per Resonance amplifier level (0.2 = +20% damage taken).")
            .defineInRange("resonanceDamageBonus", 0.2, 0.0, 2.0);

    public static final ModConfigSpec.IntValue RESONANCE_DURATION = BUILDER
            .comment("Duration of the Resonance effect in ticks (100 = 5 seconds).")
            .defineInRange("resonanceDuration", 100, 20, 600);

    public static final ModConfigSpec.IntValue HARMONIC_SHIELD_COOLDOWN = BUILDER
            .comment("Cooldown in ticks for Harmonic Shield regeneration (600 = 30 seconds).")
            .defineInRange("harmonicShieldCooldown", 600, 100, 2400);

    public static final ModConfigSpec.BooleanValue SHOW_PARTICLES = BUILDER
            .comment("Whether to show particles on entities afflicted with Resonance.")
            .define("showParticles", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
