package com.resonance.event;

import com.resonance.registry.ModItems;
import com.resonance.registry.ModPotions;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class BrewingRecipeHandler {
    private BrewingRecipeHandler() {
    }

    public static void register() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.AWKWARD, ModItems.AMETHYST_INGOT.get(), ModPotions.RESONANCE.holder());
            builder.addMix(ModPotions.RESONANCE.holder(), Items.REDSTONE, ModPotions.LONG_RESONANCE.holder());
            builder.addMix(ModPotions.RESONANCE.holder(), Items.GLOWSTONE_DUST, ModPotions.STRONG_RESONANCE.holder());
            builder.addMix(ModPotions.STRONG_RESONANCE.holder(), ModItems.AMETHYST_INGOT.get(), ModPotions.STRONGEST_RESONANCE.holder());
        });
    }
}
