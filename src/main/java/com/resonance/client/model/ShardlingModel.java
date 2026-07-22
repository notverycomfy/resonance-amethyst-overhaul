package com.resonance.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** Tiny four-legged crystal critter with a shard growing from its back. */
public class ShardlingModel extends EntityModel<LivingEntityRenderState> {

    private final ModelPart body;
    private final ModelPart shard;
    private final ModelPart legFL;
    private final ModelPart legFR;
    private final ModelPart legBL;
    private final ModelPart legBR;

    public ShardlingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.shard = this.body.getChild("shard");
        this.legFL = root.getChild("leg_fl");
        this.legFR = root.getChild("leg_fr");
        this.legBL = root.getChild("leg_bl");
        this.legBR = root.getChild("leg_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bodyPart = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -2.5F, -3.0F, 5.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 20.5F, 0.0F));

        bodyPart.addOrReplaceChild("shard",
                CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(8, 9).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.5F, -0.15F, 0.0F, 0.1F));

        bodyPart.addOrReplaceChild("eyes",
                CubeListBuilder.create()
                        .texOffs(12, 9).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("leg_fl",
                CubeListBuilder.create().texOffs(22, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(1.8F, 22.0F, -2.0F));
        root.addOrReplaceChild("leg_fr",
                CubeListBuilder.create().texOffs(22, 3).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.8F, 22.0F, -2.0F));
        root.addOrReplaceChild("leg_bl",
                CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(1.8F, 22.0F, 2.0F));
        root.addOrReplaceChild("leg_br",
                CubeListBuilder.create().texOffs(26, 3).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.8F, 22.0F, 2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;

        legFL.xRot = Mth.cos(walkPos * 1.2F) * 1.2F * walkSpeed;
        legBR.xRot = Mth.cos(walkPos * 1.2F) * 1.2F * walkSpeed;
        legFR.xRot = -Mth.cos(walkPos * 1.2F) * 1.2F * walkSpeed;
        legBL.xRot = -Mth.cos(walkPos * 1.2F) * 1.2F * walkSpeed;

        body.yRot = state.yRot * Mth.DEG_TO_RAD * 0.1F;
        shard.zRot = 0.1F + Mth.sin(age * 0.1F) * 0.05F;
    }
}
