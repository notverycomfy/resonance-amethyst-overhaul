package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.client.model.CrystalWraithModel;
import com.resonance.client.model.ShatteredEchoModel;
import com.resonance.client.model.TheHarmonicModel;
import com.resonance.entity.TheHarmonicEntity;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

@EventBusSubscriber(modid = Resonance.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static final double HARMONIC_MUSIC_RANGE_SQR = 128.0 * 128.0;
    private static final Identifier HARMONIC_MUSIC_ID =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "music.harmonic");
    private static final Music HARMONIC_MUSIC =
            new Music(ModSounds.HARMONIC_BOSS_MUSIC, 0, 0, true);

    @SubscribeEvent
    public static void selectBossMusic(SelectMusicEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        boolean bossNearby = false;
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof TheHarmonicEntity boss
                    && boss.isAlive()
                    && boss.distanceToSqr(minecraft.player) <= HARMONIC_MUSIC_RANGE_SQR) {
                bossNearby = true;
                break;
            }
        }

        if (bossNearby) {
            event.overrideMusic(HARMONIC_MUSIC);
        } else if (event.getPlayingMusic() != null
                && HARMONIC_MUSIC_ID.equals(event.getPlayingMusic().getIdentifier())) {
            event.overrideMusic(null);
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RESONANT_ARROW.get(), ResonantArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.SHATTERED_ECHO.get(), ShatteredEchoRenderer::new);
        event.registerEntityRenderer(ModEntities.RESONANT_STALKER.get(), ResonantStalkerRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_WRAITH.get(), CrystalWraithRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_SENTINEL.get(), CrystalSentinelRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_HARMONIC.get(), TheHarmonicRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_SHARD.get(), CrystalShardRenderer::new);
        event.registerEntityRenderer(ModEntities.HARMONIC_ANCHOR.get(), HarmonicAnchorRenderer::new);
        event.registerBlockEntityRenderer(com.resonance.registry.ModBlockEntities.CHORUS_RESONATOR.get(), ChorusResonatorRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_RABBIT.get(), CrystalRabbitRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_ARMADILLO.get(), CrystalArmadilloRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShatteredEchoRenderer.LAYER, ShatteredEchoModel::createBodyLayer);
        event.registerLayerDefinition(ResonantStalkerRenderer.LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(CrystalWraithRenderer.LAYER, CrystalWraithModel::createBodyLayer);
        event.registerLayerDefinition(TheHarmonicRenderer.LAYER, TheHarmonicModel::createBodyLayer);
    }
}
