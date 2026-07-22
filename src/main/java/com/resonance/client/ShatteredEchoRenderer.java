package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.client.model.ShatteredEchoModel;
import com.resonance.entity.ShatteredEchoEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class ShatteredEchoRenderer extends MobRenderer<ShatteredEchoEntity, LivingEntityRenderState, ShatteredEchoModel> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/shattered_echo.png");
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Resonance.MODID, "shattered_echo"), "main");

    public ShatteredEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new ShatteredEchoModel(context.bakeLayer(LAYER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
