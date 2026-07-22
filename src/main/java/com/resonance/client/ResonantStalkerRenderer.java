package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.entity.ResonantStalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class ResonantStalkerRenderer extends HumanoidMobRenderer<ResonantStalkerEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/resonant_stalker.png");
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Resonance.MODID, "resonant_stalker"), "main");

    public ResonantStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER), RenderTypes::entityTranslucent), 0.0F);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}
