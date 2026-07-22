package com.resonance.client.model;

import com.resonance.client.CrystalWraithRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** A hollow-faced crystal horror built around an exposed heart and broken calcite ribs. */
public class CrystalWraithModel extends EntityModel<CrystalWraithRenderState> {
    private final ModelPart wraith;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart eye;
    private final ModelPart crown;
    private final ModelPart ribCage;
    private final ModelPart core;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart backSpikes;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart centerTail;
    private final ModelPart leftTail;
    private final ModelPart rightTail;

    public CrystalWraithModel(ModelPart root) {
        // The atlas uses binary transparency. The cutout pass writes depth for
        // intersecting ribs and shards instead of translucently sorting them.
        super(root);
        wraith = root.getChild("wraith");
        head = wraith.getChild("head");
        jaw = head.getChild("jaw");
        eye = head.getChild("eye");
        crown = head.getChild("crown");
        ribCage = wraith.getChild("rib_cage");
        core = wraith.getChild("core");
        leftShoulder = wraith.getChild("left_shoulder");
        rightShoulder = wraith.getChild("right_shoulder");
        backSpikes = wraith.getChild("back_spikes");
        leftArm = wraith.getChild("left_arm");
        rightArm = wraith.getChild("right_arm");
        leftForearm = leftArm.getChild("forearm");
        rightForearm = rightArm.getChild("forearm");
        centerTail = wraith.getChild("center_tail");
        leftTail = wraith.getChild("left_tail");
        rightTail = wraith.getChild("right_tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition wraith = root.addOrReplaceChild("wraith", CubeListBuilder.create(), PartPose.ZERO);

        // The hood is pulled behind the face. A near-black inset creates a real
        // cavity instead of another friendly square mask.
        PartDefinition head = wraith.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, -3.5F, -0.5F, 7.0F, 7.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -0.75F, 0.08F, 0.0F, 0.0F));
        head.addOrReplaceChild("void_face",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-2.5F, -2.5F, -0.25F, 5.0F, 5.0F, 0.5F),
                PartPose.offset(0.0F, 0.0F, -1.5F));
        head.addOrReplaceChild("brow",
                CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-3.5F, -0.5F, -1.0F, 7.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.15F, -1.3F, -0.12F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_cheek",
                CubeListBuilder.create().texOffs(54, 0)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(2.2F, 0.25F, -1.3F, 0.0F, 0.0F, -0.12F));
        head.addOrReplaceChild("right_cheek",
                CubeListBuilder.create().texOffs(62, 0)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-2.2F, 0.25F, -1.3F, 0.0F, 0.0F, 0.12F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(70, 0)
                        .addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 3.1F, -1.15F, 0.18F, 0.0F, 0.0F));
        head.addOrReplaceChild("eye",
                CubeListBuilder.create().texOffs(86, 0)
                        .addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -0.15F, -1.75F));

        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 1.25F));
        crown.addOrReplaceChild("center_horn",
                CubeListBuilder.create().texOffs(90, 0)
                        .addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.rotation(-0.28F, 0.0F, 0.0F));
        crown.addOrReplaceChild("left_horn",
                CubeListBuilder.create().texOffs(98, 0)
                        .addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, -0.38F, 0.0F, -0.35F));
        crown.addOrReplaceChild("right_horn",
                CubeListBuilder.create().texOffs(106, 0)
                        .addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, -0.38F, 0.0F, 0.35F));

        // A thin amethyst spine remains when the destructible calcite ribs shatter.
        wraith.addOrReplaceChild("spine",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.5F, -5.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 10.5F, 1.0F, 0.12F, 0.0F, 0.0F));
        wraith.addOrReplaceChild("rib_cage",
                CubeListBuilder.create()
                        .texOffs(12, 16).addBox(-5.0F, -3.5F, -1.5F, 10.0F, 1.0F, 3.0F)
                        .texOffs(40, 16).addBox(-4.0F, -0.5F, -1.5F, 8.0F, 1.0F, 3.0F)
                        .texOffs(64, 16).addBox(-3.0F, 2.5F, -1.5F, 6.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, -0.5F, 0.12F, 0.0F, 0.0F));
        wraith.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(84, 16).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F)
                        .texOffs(90, 16).addBox(-0.5F, 1.5F, -0.5F, 1.0F, 2.0F, 1.0F),
                // Match the rib rotation and sit 0.15 pixels behind their front face.
                PartPose.offsetAndRotation(0.0F, 9.5F, -1.35F, 0.12F, 0.0F, 0.0F));

        // Tall shoulder shards and swept-back spine crystals produce a hostile
        // silhouette from the front, profile, and rear.
        wraith.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F)
                        .texOffs(12, 32).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 7.5F, 0.25F, 0.0F, 0.0F, 0.3F));
        wraith.addOrReplaceChild("right_shoulder",
                CubeListBuilder.create()
                        .texOffs(16, 32).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F)
                        .texOffs(28, 32).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 7.5F, 0.25F, 0.0F, 0.0F, -0.3F));

        PartDefinition backSpikes = wraith.addOrReplaceChild("back_spikes", CubeListBuilder.create(), PartPose.ZERO);
        backSpikes.addOrReplaceChild("left_upper",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 7.0F, 2.25F, 0.0F, -0.3F, 0.48F));
        backSpikes.addOrReplaceChild("right_upper",
                CubeListBuilder.create().texOffs(18, 48)
                        .addBox(0.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 7.0F, 2.25F, 0.0F, 0.3F, -0.48F));
        backSpikes.addOrReplaceChild("left_lower",
                CubeListBuilder.create().texOffs(36, 48)
                        .addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, 12.0F, 2.0F, 0.0F, -0.2F, 0.32F));
        backSpikes.addOrReplaceChild("right_lower",
                CubeListBuilder.create().texOffs(50, 48)
                        .addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(1.5F, 12.0F, 2.0F, 0.0F, 0.2F, -0.32F));

        PartDefinition leftArm = wraith.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(5.0F, 8.0F, 0.0F, -0.16F, 0.0F, -0.28F));
        PartDefinition leftForearm = leftArm.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -0.25F, -0.18F, 0.0F, 0.16F));
        addClawedHand(leftForearm, "hand", 72, 32, false);

        PartDefinition rightArm = wraith.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(-5.0F, 8.0F, 0.0F, -0.08F, 0.0F, 0.28F));
        PartDefinition rightForearm = rightArm.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(60, 32)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.25F, -0.1F, 0.0F, -0.12F));
        addClawedHand(rightForearm, "hand", 84, 32, true);

        // The body ends in uneven strips instead of feet.
        wraith.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(94, 16)
                        .addBox(-2.5F, 0.0F, -1.5F, 5.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 16.0F, 0.5F));
        wraith.addOrReplaceChild("center_tail",
                CubeListBuilder.create().texOffs(110, 16)
                        .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, 19.05F, 0.5F));
        wraith.addOrReplaceChild("left_tail",
                CubeListBuilder.create().texOffs(100, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 18.5F, 0.5F, 0.0F, 0.0F, -0.28F));
        wraith.addOrReplaceChild("right_tail",
                CubeListBuilder.create().texOffs(108, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 19.5F, 0.5F, 0.0F, 0.0F, 0.22F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addClawedHand(PartDefinition forearm, String name, int u, int v, boolean mirrored) {
        PartDefinition hand = forearm.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, 6.05F, 0.0F));
        float side = mirrored ? -1.0F : 1.0F;
        hand.addOrReplaceChild("inner_claw",
                CubeListBuilder.create().texOffs(96, 32)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-side, 1.25F, -0.5F, -0.28F, 0.0F, side * 0.16F));
        hand.addOrReplaceChild("center_claw",
                CubeListBuilder.create().texOffs(96, 32)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.4F, -0.75F, -0.38F, 0.0F, 0.0F));
        hand.addOrReplaceChild("outer_claw",
                CubeListBuilder.create().texOffs(96, 32)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(side, 1.25F, -0.5F, -0.28F, 0.0F, -side * 0.16F));
    }

    @Override
    public void setupAnim(CrystalWraithRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float emerge = state.emergenceProgress;
        float walk = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;

        wraith.y = (1.0F - emerge) * 24.0F + Mth.sin(age * 0.07F) * 0.28F;
        wraith.xRot = emerge < 1.0F ? (1.0F - emerge) * 0.5F : 0.0F;
        head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.8F;
        head.xRot = 0.08F + state.xRot * Mth.DEG_TO_RAD * 0.55F;
        jaw.xRot = 0.18F + Mth.sin(age * 0.11F) * 0.08F;

        float twitch = Mth.sin(age * 0.17F) * 0.035F;
        float armSway = Mth.cos(walk * 0.65F) * 0.24F * speed;
        leftArm.xRot = -0.16F - armSway + twitch;
        rightArm.xRot = -0.08F + armSway - twitch;
        leftArm.zRot = -0.28F;
        rightArm.zRot = 0.28F;
        leftForearm.xRot = -0.18F;
        rightForearm.xRot = -0.1F;

        centerTail.zRot = Mth.sin(age * 0.075F) * 0.08F;
        leftTail.zRot = -0.28F + Mth.sin(age * 0.09F + 1.4F) * 0.12F;
        rightTail.zRot = 0.22F + Mth.sin(age * 0.085F + 3.6F) * 0.12F;

        if (emerge < 1.0F) {
            float claw = Mth.sin(emerge * Mth.PI * 3.0F);
            head.xRot = -0.42F + emerge * 0.5F;
            jaw.xRot = 0.5F - emerge * 0.32F;
            leftArm.xRot = -1.48F - claw * 0.2F;
            rightArm.xRot = -1.48F + claw * 0.2F;
            leftForearm.xRot = -0.62F;
            rightForearm.xRot = -0.62F;
        }

        boolean armored = !state.armorBroken;
        crown.visible = armored;
        ribCage.visible = armored;
        leftShoulder.visible = armored;
        rightShoulder.visible = armored;
        backSpikes.visible = armored;

        float corePulse = state.armorBroken ? 1.0F + Mth.sin(age * 0.25F) * 0.3F : 1.0F;
        core.xScale = corePulse;
        core.yScale = corePulse;
        core.zScale = corePulse;
        float eyePulse = 1.0F + Mth.sin(age * 0.2F) * 0.12F;
        eye.yScale = eyePulse;

        if (state.attackAnim > 0.0F && emerge >= 1.0F) {
            float attack = state.attackAnim;
            leftArm.xRot = -2.15F * attack;
            rightArm.xRot = -2.05F * attack;
            leftForearm.xRot = -0.72F * attack;
            rightForearm.xRot = -0.68F * attack;
            jaw.xRot = 0.18F + 0.58F * attack;
            head.xRot -= 0.18F * attack;
        }
    }
}
