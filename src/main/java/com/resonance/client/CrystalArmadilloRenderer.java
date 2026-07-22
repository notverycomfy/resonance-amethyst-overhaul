package com.resonance.client;

import com.resonance.Resonance;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;
import net.minecraft.resources.Identifier;

public class CrystalArmadilloRenderer extends ArmadilloRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_armadillo.png");
    private static final Identifier BABY_TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_armadillo_baby.png");

    public CrystalArmadilloRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ArmadilloRenderState state) {
        return state.isBaby ? BABY_TEXTURE : TEXTURE;
    }
}
