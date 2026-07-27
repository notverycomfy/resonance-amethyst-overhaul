package com.resonance.client;

import com.resonance.client.model.CrystalWraithModel;
import com.resonance.client.model.ShatteredEchoModel;
import com.resonance.client.model.TheHarmonicModel;
import com.resonance.registry.ModBlockEntities;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModSounds;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.sounds.Music;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class ClientEvents {
    public static final Music HARMONIC_MUSIC =
            new Music(ModSounds.HARMONIC_BOSS_MUSIC, 0, 0, true);

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

    }
}
