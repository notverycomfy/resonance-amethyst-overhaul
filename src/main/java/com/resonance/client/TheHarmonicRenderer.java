package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.client.model.TheHarmonicModel;
import com.resonance.entity.TheHarmonicEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class TheHarmonicRenderer extends MobRenderer<TheHarmonicEntity, TheHarmonicRenderState, TheHarmonicModel> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/the_harmonic.png");
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Resonance.MODID, "the_harmonic"), "main");

    public TheHarmonicRenderer(EntityRendererProvider.Context context) {
        super(context, new TheHarmonicModel(context.bakeLayer(LAYER)), 0.0F);
    }

    @Override
    public Identifier getTextureLocation(TheHarmonicRenderState state) {
        return TEXTURE;
    }

    @Override
    public TheHarmonicRenderState createRenderState() {
        return new TheHarmonicRenderState();
    }

    @Override
    public void extractRenderState(TheHarmonicEntity entity, TheHarmonicRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.shielded = entity.isShielded();
        state.phase = entity.getPhase();
        state.activeBeams = entity.getActiveBeams();
    }
}
