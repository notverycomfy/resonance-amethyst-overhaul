package com.resonance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resonance.entity.CrystalShardEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class CrystalShardRenderer extends EntityRenderer<CrystalShardEntity, CrystalShardRenderer.State> {

    public static class State extends ThrownItemRenderState {
        public float yRot;
        public float xRot;
    }

    private final ItemModelResolver itemModelResolver;

    public CrystalShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    protected int getBlockLightLevel(CrystalShardEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrystalShardEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // FIXED avoids the camera-oriented transforms used by loose item visuals.
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.FIXED, entity);
        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(0.9F, 0.9F, 0.9F);
        // Align the shard's long axis with its flight direction. Render a
        // second perpendicular plane so the projectile has a stable crystal
        // silhouette from every camera angle instead of behaving like a
        // billboarded item sprite.
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
