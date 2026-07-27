package com.resonance.mixin.client;

import com.resonance.client.ClientEvents;
import com.resonance.entity.TheHarmonicEntity;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the Resonant Helmet outline only for the local wearer. Applying
 * vanilla Glowing on the server would broadcast the outline to every player.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Unique
    private static final double resonance$helmetRevealRangeSquared = 16.0 * 16.0;
    @Unique
    private static final double resonance$harmonicMusicRangeSquared = 128.0 * 128.0;

    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void resonance$selectHarmonicMusic(CallbackInfoReturnable<Music> callback) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof TheHarmonicEntity boss && boss.isAlive()
                    && boss.distanceToSqr(minecraft.player) <= resonance$harmonicMusicRangeSquared) {
                callback.setReturnValue(ClientEvents.HARMONIC_MUSIC);
                return;
            }
        }
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void resonance$showResonatingEntityToHelmetWearer(
            Entity entity, CallbackInfoReturnable<Boolean> callback) {
        LocalPlayer viewer = ((Minecraft) (Object) this).player;
        if (viewer != null
                && entity instanceof LivingEntity living
                && living != viewer
                && viewer.distanceToSqr(living) <= resonance$helmetRevealRangeSquared
                && viewer.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.RESONANT_HELMET.get())
                && living.hasEffect(ModEffects.RESONANCE.holder())) {
            callback.setReturnValue(true);
        }
    }
}
