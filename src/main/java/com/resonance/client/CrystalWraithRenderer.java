package com.resonance.client;

import com.resonance.Resonance;
import com.resonance.client.layer.CrystalWraithGlowLayer;
import com.resonance.client.model.CrystalWraithModel;
import com.resonance.entity.CrystalWraithEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CrystalWraithRenderer extends MobRenderer<CrystalWraithEntity, CrystalWraithRenderState, CrystalWraithModel> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_wraith.png");
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Resonance.MODID, "crystal_wraith"), "main");

    public CrystalWraithRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalWraithModel(context.bakeLayer(LAYER)), 0.35F);
        this.addLayer(new CrystalWraithGlowLayer(this));
    }

    @Override
    public Identifier getTextureLocation(CrystalWraithRenderState state) {
        return TEXTURE;
    }

    @Override
    public CrystalWraithRenderState createRenderState() {
        return new CrystalWraithRenderState();
    }

    @Override
    public void extractRenderState(CrystalWraithEntity entity, CrystalWraithRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armorBroken = entity.isArmorBroken();
        state.attackAnim = entity.getAttackAnim(partialTicks);
        state.emergenceProgress = entity.getEmergenceProgress(partialTicks);
    }
}
