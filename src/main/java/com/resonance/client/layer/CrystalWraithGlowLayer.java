package com.resonance.client.layer;

import com.resonance.Resonance;
import com.resonance.client.CrystalWraithRenderState;
import com.resonance.client.model.CrystalWraithModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Full-bright pass restricted to the Wraith's buried eye and exposed heart. */
public class CrystalWraithGlowLayer extends EyesLayer<CrystalWraithRenderState, CrystalWraithModel> {
    private static final RenderType GLOW = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_wraith_glow.png"));

    public CrystalWraithGlowLayer(RenderLayerParent<CrystalWraithRenderState, CrystalWraithModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
