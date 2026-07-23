package com.resonance.client;

import com.resonance.client.model.CrystalWraithModel;
import com.resonance.client.model.ShatteredEchoModel;
import com.resonance.client.model.TheHarmonicModel;
import com.resonance.registry.ModBlockEntities;
import com.resonance.registry.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.resonance.entity.TheHarmonicEntity;
import net.minecraft.sounds.Musics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void register() {
        EntityRendererRegistry.register(ModEntities.RESONANT_ARROW.get(), ResonantArrowRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHATTERED_ECHO.get(), ShatteredEchoRenderer::new);
        EntityRendererRegistry.register(ModEntities.RESONANT_STALKER.get(), ResonantStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_WRAITH.get(), CrystalWraithRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_SENTINEL.get(), CrystalSentinelRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_HARMONIC.get(), TheHarmonicRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_SHARD.get(), CrystalShardRenderer::new);
        EntityRendererRegistry.register(ModEntities.HARMONIC_ANCHOR.get(), HarmonicAnchorRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_RABBIT.get(), CrystalRabbitRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_ARMADILLO.get(), CrystalArmadilloRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.CHORUS_RESONATOR.get(), ChorusResonatorRenderer::new);

        ModelLayerRegistry.registerModelLayer(ShatteredEchoRenderer.LAYER, ShatteredEchoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ResonantStalkerRenderer.LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        ModelLayerRegistry.registerModelLayer(CrystalWraithRenderer.LAYER, CrystalWraithModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TheHarmonicRenderer.LAYER, TheHarmonicModel::createBodyLayer);

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.level == null || minecraft.player == null) {
                return;
            }
            boolean harmonicNearby = false;
            for (var entity : minecraft.level.entitiesForRendering()) {
                if (entity instanceof TheHarmonicEntity boss && boss.isAlive()
                        && boss.distanceToSqr(minecraft.player) <= 128.0 * 128.0) {
                    harmonicNearby = true;
                    break;
                }
            }
            if (harmonicNearby) {
                if (!minecraft.getMusicManager().isPlayingMusic(Musics.END_BOSS)) {
                    minecraft.getMusicManager().startPlaying(Musics.END_BOSS);
                }
            } else if (minecraft.getMusicManager().isPlayingMusic(Musics.END_BOSS)) {
                minecraft.getMusicManager().stopPlaying(Musics.END_BOSS);
            }
        });
    }
}
