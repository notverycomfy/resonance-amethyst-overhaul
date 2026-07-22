package com.resonance.client;

import com.resonance.Resonance;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.Identifier;

public class CrystalRabbitRenderer extends RabbitRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_rabbit.png");
    private static final Identifier BABY_TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_rabbit_baby.png");

    public CrystalRabbitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(RabbitRenderState state) {
        return state.isBaby ? BABY_TEXTURE : TEXTURE;
    }
}
