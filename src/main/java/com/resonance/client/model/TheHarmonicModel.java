package com.resonance.client.model;

import com.resonance.client.TheHarmonicRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A floating crystal heart surrounded by two counter-rotating harmonic halos.
 * The silhouette is deliberately symmetrical so it reads clearly from every
 * side while the smaller shield facets preserve the boss's protected state.
 */
public class TheHarmonicModel extends EntityModel<TheHarmonicRenderState> {

    private final ModelPart body;
    private final ModelPart nucleus;
    private final ModelPart innerHalo;
    private final ModelPart outerHalo;
    private final ModelPart shieldFacets;

    public TheHarmonicModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.nucleus = body.getChild("nucleus");
        this.innerHalo = root.getChild("inner_halo");
        this.outerHalo = root.getChild("outer_halo");
        this.shieldFacets = root.getChild("shield_facets");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        // Broad middle with stepped crystal shoulders.
                        .texOffs(0, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 14.0F, 12.0F)
                        .texOffs(0, 28).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 4.0F, 8.0F)
                        .texOffs(32, 28).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 4.0F, 8.0F)
                        .texOffs(64, 0).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(80, 0).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        // Four matching resonant windows keep the core readable from all sides.
        body.addOrReplaceChild("nucleus",
                CubeListBuilder.create()
                        .texOffs(64, 10).addBox(-3.0F, -3.0F, -6.5F, 6.0F, 6.0F, 1.0F)
                        .texOffs(78, 10).addBox(-3.0F, -3.0F, 5.5F, 6.0F, 6.0F, 1.0F)
                        .texOffs(92, 10).addBox(-6.5F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F)
                        .texOffs(106, 10).addBox(5.5F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F),
                PartPose.ZERO);

        // Mirrored spear points give the body one clean vertical axis.
        body.addOrReplaceChild("upper_spire",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                        .texOffs(24, 42).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(40, 42).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -14.0F, 0.0F));

        body.addOrReplaceChild("lower_spire",
                CubeListBuilder.create()
                        .texOffs(48, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                        .texOffs(72, 42).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(88, 42).addBox(-1.0F, 13.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // A horizontal ring of evenly spaced crystal notes.
        root.addOrReplaceChild("inner_halo",
                CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-13.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F)
                        .texOffs(18, 60).addBox(8.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F)
                        .texOffs(36, 60).addBox(-2.0F, -2.0F, -13.0F, 4.0F, 4.0F, 5.0F)
                        .texOffs(54, 60).addBox(-2.0F, -2.0F, 8.0F, 4.0F, 4.0F, 5.0F)
                        .texOffs(72, 60).addBox(-10.0F, -1.5F, -10.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(84, 60).addBox(7.0F, -1.5F, -10.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(96, 60).addBox(-10.0F, -1.5F, 7.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(108, 60).addBox(7.0F, -1.5F, 7.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        // The second halo crosses the first vertically, breaking up the old
        // stack-of-cubes appearance without making the boss visually noisy.
        root.addOrReplaceChild("outer_halo",
                CubeListBuilder.create()
                        .texOffs(0, 72).addBox(-2.0F, -15.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(18, 72).addBox(-2.0F, 10.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(36, 72).addBox(-15.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F)
                        .texOffs(54, 72).addBox(10.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F)
                        .texOffs(72, 72).addBox(-10.0F, -10.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                        .texOffs(84, 72).addBox(7.0F, -10.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                        .texOffs(96, 72).addBox(-10.0F, 7.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                        .texOffs(108, 72).addBox(7.0F, 7.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        // Four restrained shield facets replace the former oversized solid cage.
        root.addOrReplaceChild("shield_facets",
                CubeListBuilder.create()
                        .texOffs(0, 84).addBox(-4.0F, -8.0F, -15.0F, 8.0F, 16.0F, 1.0F)
                        .texOffs(18, 84).addBox(-4.0F, -8.0F, 14.0F, 8.0F, 16.0F, 1.0F)
                        .texOffs(36, 84).addBox(-15.0F, -8.0F, -4.0F, 1.0F, 16.0F, 8.0F)
                        .texOffs(54, 84).addBox(14.0F, -8.0F, -4.0F, 1.0F, 16.0F, 8.0F),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(TheHarmonicRenderState state) {
        super.setupAnim(state);

        float time = state.ageInTicks;
        float phaseSpeed = 1.0F + Math.max(0, state.phase - 1) * 0.18F;
        float bob = Mth.sin(time * 0.055F * phaseSpeed) * 0.45F;

        body.y = -32.0F + bob;
        body.yRot = time * 0.006F * phaseSpeed;

        float corePulse = 1.0F
                + Mth.sin(time * 0.11F * phaseSpeed) * 0.055F
                + Math.min(state.activeBeams, 4) * 0.012F;
        nucleus.xScale = corePulse;
        nucleus.yScale = corePulse;
        nucleus.zScale = corePulse;

        innerHalo.y = -32.0F + bob;
        innerHalo.yRot = time * 0.035F * phaseSpeed;

        outerHalo.y = -32.0F + bob;
        outerHalo.yRot = 0.52F + Mth.sin(time * 0.018F) * 0.12F;
        outerHalo.zRot = -time * 0.026F * phaseSpeed;

        shieldFacets.visible = state.shielded;
        if (state.shielded) {
            float shieldPulse = 1.0F + Mth.sin(time * 0.09F) * 0.035F;
            shieldFacets.y = -32.0F + bob;
            shieldFacets.yRot = -time * 0.022F * phaseSpeed;
            shieldFacets.xScale = shieldPulse;
            shieldFacets.yScale = shieldPulse;
            shieldFacets.zScale = shieldPulse;
        }
    }
}
