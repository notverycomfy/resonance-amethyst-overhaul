package com.resonance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resonance.Resonance;
import com.resonance.entity.CrystalSentinelEntity;
import com.resonance.client.model.CrystalSentinelModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.shulker.ShulkerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.Objects;

public class CrystalSentinelRenderer extends MobRenderer<CrystalSentinelEntity, CrystalSentinelRenderState, ShulkerModel> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_sentinel.png");

    private static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath(Resonance.MODID, "textures/entity/crystal_sentinel_beam.png");

    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityCutout(BEAM_TEXTURE);

    public CrystalSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalSentinelModel(context.bakeLayer(ModelLayers.SHULKER)), 0.0F);
    }

    @Override
    public Identifier getTextureLocation(CrystalSentinelRenderState state) {
        return TEXTURE;
    }

    @Override
    public CrystalSentinelRenderState createRenderState() {
        return new CrystalSentinelRenderState();
    }

    @Override
    public boolean shouldRender(CrystalSentinelEntity entity, Frustum culler,
                                double camX, double camY, double camZ) {
        if (super.shouldRender(entity, culler, camX, camY, camZ)) {
            return true;
        }
        LivingEntity target = entity.getBeamTarget();
        if (target == null) {
            return false;
        }
        Vec3 start = entity.getEyePosition();
        Vec3 end = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        return culler.isVisible(new AABB(start, end).inflate(0.35));
    }

    @Override
    public void extractRenderState(CrystalSentinelEntity entity, CrystalSentinelRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderOffset = Objects.requireNonNullElse(entity.getRenderPosition(partialTicks), Vec3.ZERO);
        state.peekAmount = entity.getClientPeekAmount(partialTicks);
        state.yHeadRot = entity.yHeadRot;
        state.yBodyRot = entity.yBodyRot;
        state.attachFace = entity.getAttachFace();

        state.eyePosition = entity.getEyePosition(partialTicks);
        LivingEntity beamTarget = entity.getBeamTarget();
        if (beamTarget != null) {
            state.beamScale = entity.getAttackAnimationScale(partialTicks);
            state.beamTime = entity.getClientBeamTime() + partialTicks;
            state.beamTargetPosition = beamTarget.position().add(0, beamTarget.getBbHeight() * 0.5, 0);
        } else {
            state.beamTargetPosition = null;
        }
    }

    @Override
    public void submit(CrystalSentinelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        Vec3 targetPos = state.beamTargetPosition;
        if (targetPos != null) {
            poseStack.pushPose();
            poseStack.translate(0.0F, state.eyeHeight, 0.0F);
            Vec3 beamVec = targetPos.subtract(state.eyePosition);
            float texVOff = state.beamTime * 0.5F % 1.0F;
            renderBeam(poseStack, submitNodeCollector, beamVec, state.beamTime, state.beamScale, texVOff);
            poseStack.popPose();
        }
    }

    private static void renderBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 beamVector,
                                    float timeInTicks, float scale, float texVOff) {
        // Stop at the target rather than passing a full block through it.
        float length = (float)beamVector.length();
        beamVector = beamVector.normalize();
        float xRot = (float)Math.acos(beamVector.y);
        float yRot = (float)(Math.PI / 2) - (float)Math.atan2(beamVector.z, beamVector.x);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot * (180.0F / (float)Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot * (180.0F / (float)Math.PI)));
        float rot = timeInTicks * 0.05F * -1.5F;
        float colorScale = scale * scale;

        int red = 128 + (int)(colorScale * 80.0F);
        int green = 40 + (int)(colorScale * 80.0F);
        int blue = 180 + (int)(colorScale * 75.0F);

        float wnx = Mth.cos(rot + (float)(Math.PI * 3.0 / 4.0)) * 0.282F;
        float wnz = Mth.sin(rot + (float)(Math.PI * 3.0 / 4.0)) * 0.282F;
        float enx = Mth.cos(rot + (float)(Math.PI / 4)) * 0.282F;
        float enz = Mth.sin(rot + (float)(Math.PI / 4)) * 0.282F;
        float wsx = Mth.cos(rot + ((float)Math.PI * 5.0F / 4.0F)) * 0.282F;
        float wsz = Mth.sin(rot + ((float)Math.PI * 5.0F / 4.0F)) * 0.282F;
        float esx = Mth.cos(rot + ((float)Math.PI * 7.0F / 4.0F)) * 0.282F;
        float esz = Mth.sin(rot + ((float)Math.PI * 7.0F / 4.0F)) * 0.282F;
        float wx = Mth.cos(rot + (float)Math.PI) * 0.2F;
        float wz = Mth.sin(rot + (float)Math.PI) * 0.2F;
        float ex = Mth.cos(rot + 0.0F) * 0.2F;
        float ez = Mth.sin(rot + 0.0F) * 0.2F;
        float nx = Mth.cos(rot + (float)(Math.PI / 2)) * 0.2F;
        float nz = Mth.sin(rot + (float)(Math.PI / 2)) * 0.2F;
        float sx = Mth.cos(rot + (float)(Math.PI * 3.0 / 2.0)) * 0.2F;
        float sz = Mth.sin(rot + (float)(Math.PI * 3.0 / 2.0)) * 0.2F;
        float minV = -1.0F + texVOff;
        float maxV = minV + length * 2.5F;
        submitNodeCollector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, buffer) -> {
            vertex(buffer, pose, wx, length, wz, red, green, blue, 0.4999F, maxV);
            vertex(buffer, pose, wx, 0.0F, wz, red, green, blue, 0.4999F, minV);
            vertex(buffer, pose, ex, 0.0F, ez, red, green, blue, 0.0F, minV);
            vertex(buffer, pose, ex, length, ez, red, green, blue, 0.0F, maxV);
            vertex(buffer, pose, nx, length, nz, red, green, blue, 0.4999F, maxV);
            vertex(buffer, pose, nx, 0.0F, nz, red, green, blue, 0.4999F, minV);
            vertex(buffer, pose, sx, 0.0F, sz, red, green, blue, 0.0F, minV);
            vertex(buffer, pose, sx, length, sz, red, green, blue, 0.0F, maxV);
            float vBase = Mth.floor(timeInTicks) % 2 == 0 ? 0.5F : 0.0F;
            vertex(buffer, pose, wnx, length, wnz, red, green, blue, 0.5F, vBase + 0.5F);
            vertex(buffer, pose, enx, length, enz, red, green, blue, 1.0F, vBase + 0.5F);
            vertex(buffer, pose, esx, length, esz, red, green, blue, 1.0F, vBase);
            vertex(buffer, pose, wsx, length, wsz, red, green, blue, 0.5F, vBase);
        });
    }

    private static void vertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z,
                                int red, int green, int blue, float u, float v) {
        builder.addVertex(pose, x, y, z)
                .setColor(red, green, blue, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public Vec3 getRenderOffset(CrystalSentinelRenderState state) {
        return state.renderOffset;
    }

    @Override
    protected void setupRotations(CrystalSentinelRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot + 180.0F, entityScale);
        poseStack.rotateAround(state.attachFace.getOpposite().getRotation(), 0.0F, 0.5F, 0.0F);
    }
}
