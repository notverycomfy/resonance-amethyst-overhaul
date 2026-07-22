package com.resonance.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.shulker.ShulkerModel;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;

public class CrystalSentinelModel extends ShulkerModel {

    private final ModelPart head;

    public CrystalSentinelModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

    @Override
    public void setupAnim(ShulkerRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks * 0.15F;
        this.head.yRot = t;
        this.head.xRot = (float) Math.sin(t * 0.7F) * 0.4F;
        this.head.zRot = (float) Math.cos(t * 0.5F) * 0.3F;
    }
}
