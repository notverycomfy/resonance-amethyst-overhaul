package com.resonance.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * A being of broken geode fragments held together by resonance.
 * Every piece is physically separated — the fracture gaps between
 * them widen and close as it "breathes".
 */
public class ShatteredEchoModel extends EntityModel<LivingEntityRenderState> {

    private final ModelPart headLeft;
    private final ModelPart headRight;
    private final ModelPart crown;
    private final ModelPart torsoTop;
    private final ModelPart torsoMid;
    private final ModelPart torsoLow;
    private final ModelPart bigArm;
    private final ModelPart bigArmLower;
    private final ModelPart shardArm;
    private final ModelPart frag1;
    private final ModelPart frag2;
    private final ModelPart frag3;
    private final ModelPart frag4;
    private final ModelPart spikeLarge;
    private final ModelPart spikeSmall;

    public ShatteredEchoModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.headLeft = root.getChild("head_left");
        this.headRight = root.getChild("head_right");
        this.crown = root.getChild("crown");
        this.torsoTop = root.getChild("torso_top");
        this.torsoMid = root.getChild("torso_mid");
        this.torsoLow = root.getChild("torso_low");
        this.bigArm = root.getChild("big_arm");
        this.bigArmLower = this.bigArm.getChild("big_arm_lower");
        this.shardArm = root.getChild("shard_arm");
        this.frag1 = root.getChild("frag1");
        this.frag2 = root.getChild("frag2");
        this.frag3 = root.getChild("frag3");
        this.frag4 = root.getChild("frag4");
        this.spikeLarge = root.getChild("spike_large");
        this.spikeSmall = root.getChild("spike_small");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head_left",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.3F, -6.0F, -3.0F, 3.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("head_right",
                CubeListBuilder.create()
                        .texOffs(18, 0).addBox(-3.3F, -6.5F, -3.0F, 3.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.1F, 0.0F, 0.15F));

        root.addOrReplaceChild("torso_top",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        root.addOrReplaceChild("torso_mid",
                CubeListBuilder.create()
                        .texOffs(24, 12).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.5F, 9.0F, 0.2F, 0.0F, 0.08F, 0.0F));

        root.addOrReplaceChild("torso_low",
                CubeListBuilder.create()
                        .texOffs(46, 12).addBox(-3.0F, 0.0F, -1.5F, 6.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-0.4F, 14.0F, 0.0F, 0.0F, -0.1F, 0.0F));

        PartDefinition bigArmPart = root.addOrReplaceChild("big_arm",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-3.0F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(-4.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.15F));

        bigArmPart.addOrReplaceChild("big_arm_lower",
                CubeListBuilder.create()
                        .texOffs(12, 20).addBox(-1.0F, 0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, 5.5F, 0.0F, 0.0F, 0.0F, 0.1F));

        root.addOrReplaceChild("shard_arm",
                CubeListBuilder.create()
                        .texOffs(20, 20).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(6.0F, 8.0F, 0.0F, 0.0F, 0.0F, -0.3F));

        root.addOrReplaceChild("frag1",
                CubeListBuilder.create()
                        .texOffs(28, 20).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(5.0F, 14.0F, 2.0F));
        root.addOrReplaceChild("frag2",
                CubeListBuilder.create()
                        .texOffs(36, 20).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-5.0F, 11.0F, -2.0F));
        root.addOrReplaceChild("frag3",
                CubeListBuilder.create()
                        .texOffs(44, 20).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(3.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("frag4",
                CubeListBuilder.create()
                        .texOffs(52, 20).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-3.5F, 16.5F, 3.0F));

        root.addOrReplaceChild("spike_large",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.3F, 17.5F, 0.0F, 0.05F, 0.0F, 0.08F));

        root.addOrReplaceChild("spike_small",
                CubeListBuilder.create()
                        .texOffs(16, 30).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-1.2F, 19.0F, 0.8F, -0.06F, 0.0F, -0.12F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;

        float bob = Mth.sin(age * 0.08F) * 0.6F;
        float breathe = (Mth.sin(age * 0.05F) + 1.0F) * 0.5F; // 0..1

        float headYaw = state.yRot * Mth.DEG_TO_RAD;
        float headPitch = state.xRot * Mth.DEG_TO_RAD;

        headLeft.y = 3.0F + bob;
        headRight.y = 3.0F + bob - breathe * 0.4F;
        headLeft.x = breathe * 0.5F;
        headRight.x = -breathe * 0.5F;
        headLeft.yRot = headYaw + breathe * 0.06F;
        headRight.yRot = headYaw - breathe * 0.06F;
        headLeft.xRot = headPitch;
        headRight.xRot = headPitch;

        crown.y = -4.5F + bob - breathe * 0.8F;
        crown.yRot = age * 0.04F;

        torsoTop.y = 4.0F + bob;
        torsoMid.y = 9.0F + bob * 0.8F + breathe * 0.3F;
        torsoLow.y = 14.0F + bob * 0.6F + breathe * 0.6F;
        torsoMid.yRot = 0.08F + Mth.sin(age * 0.03F) * 0.05F;
        torsoLow.yRot = -0.1F - Mth.sin(age * 0.03F) * 0.05F;

        bigArm.y = 5.0F + bob;
        bigArm.zRot = 0.15F + Mth.sin(age * 0.06F) * 0.1F;
        bigArm.xRot = Mth.cos(state.walkAnimationPos * 0.6F) * 0.5F * state.walkAnimationSpeed
                + Mth.cos(age * 0.07F) * 0.08F;
        bigArmLower.zRot = 0.1F + breathe * 0.15F;

        shardArm.y = 8.0F + bob + Mth.sin(age * 0.09F) * 0.8F;
        shardArm.x = 6.0F + breathe * 1.0F;
        shardArm.zRot = -0.3F - breathe * 0.2F;
        shardArm.xRot = -Mth.cos(state.walkAnimationPos * 0.6F) * 0.4F * state.walkAnimationSpeed;

        orbit(frag1, age, 0.045F, 0.0F, 5.5F, 14.0F, bob);
        orbit(frag2, age, -0.035F, 2.1F, 5.0F, 11.0F, bob);
        orbit(frag3, age, 0.055F, 4.2F, 4.0F, 18.0F, bob);
        orbit(frag4, age, -0.05F, 1.0F, 4.5F, 16.5F, bob);

        spikeLarge.y = 17.5F + bob * 0.5F + breathe * 0.8F;
        spikeLarge.zRot = 0.08F + Mth.sin(age * 0.04F) * 0.06F;
        spikeSmall.y = 19.0F + bob * 0.4F + breathe * 1.2F;
        spikeSmall.zRot = -0.12F - Mth.sin(age * 0.04F + 1.0F) * 0.08F;
    }

    private static void orbit(ModelPart part, float age, float speed, float phase,
                              float radius, float baseY, float bob) {
        float angle = age * speed + phase;
        part.x = Mth.cos(angle) * radius;
        part.z = Mth.sin(angle) * radius;
        part.y = baseY + bob + Mth.sin(age * 0.07F + phase) * 1.0F;
        part.yRot = angle;
        part.xRot = Mth.sin(age * 0.05F + phase) * 0.3F;
    }
}
