package com.resonance;

/**
 * Loader-neutral configuration values for the Fabric builds.
 *
 * Fabric keeps the same balanced defaults as NeoForge. A future config-screen
 * integration can persist overrides without changing gameplay call sites.
 */
public final class Config {
    public static final DoubleValue RESONANCE_DAMAGE_BONUS = new DoubleValue(0.2);
    public static final IntValue RESONANCE_DURATION = new IntValue(100);
    public static final IntValue HARMONIC_SHIELD_COOLDOWN = new IntValue(600);
    public static final BooleanValue SHOW_PARTICLES = new BooleanValue(true);

    private Config() {
    }

    public record DoubleValue(double value) {
        public double getAsDouble() {
            return value;
        }
    }

    public record IntValue(int value) {
        public int getAsInt() {
            return value;
        }
    }

    public record BooleanValue(boolean value) {
        public boolean getAsBoolean() {
            return value;
        }
    }
}
