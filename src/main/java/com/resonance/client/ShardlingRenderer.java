package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.client.model.ShardlingModel;
import com.resonance.entity.ShardlingEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class ShardlingRenderer extends MobRenderer<ShardlingEntity, LivingEntityRenderState, ShardlingModel> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/shardling.png");
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Resonance.MODID, "shardling"), "main");

    public ShardlingRenderer(EntityRendererProvider.Context context) {
        super(context, new ShardlingModel(context.bakeLayer(LAYER)), 0.2F);
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
