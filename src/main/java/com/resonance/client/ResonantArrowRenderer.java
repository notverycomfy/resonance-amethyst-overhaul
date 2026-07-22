package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.entity.ResonantArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
public class ResonantArrowRenderer extends ArrowRenderer<ResonantArrowEntity, ArrowRenderState> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/projectiles/resonant_arrow.png");

    public ResonantArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
