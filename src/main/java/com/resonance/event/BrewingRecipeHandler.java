package com.resonance.event;

import com.resonance.Resonance;
import com.resonance.registry.ModItems;
import com.resonance.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = Resonance.MODID)
public class BrewingRecipeHandler {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        // Awkward + Amethyst Ingot = Resonance I
        builder.addMix(Potions.AWKWARD, ModItems.AMETHYST_INGOT.get(), ModPotions.RESONANCE);
        // Resonance I + Redstone = Long Resonance
        builder.addMix(ModPotions.RESONANCE, Items.REDSTONE, ModPotions.LONG_RESONANCE);
        // Resonance I + Glowstone = Strong Resonance (II)
        builder.addMix(ModPotions.RESONANCE, Items.GLOWSTONE_DUST, ModPotions.STRONG_RESONANCE);
        // Strong Resonance (II) + Amethyst Ingot = Strongest (III)
        builder.addMix(ModPotions.STRONG_RESONANCE, ModItems.AMETHYST_INGOT.get(), ModPotions.STRONGEST_RESONANCE);
    }
}
