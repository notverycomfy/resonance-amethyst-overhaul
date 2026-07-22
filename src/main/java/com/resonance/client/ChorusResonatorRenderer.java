package com.resonance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resonance.block.entity.ChorusResonatorBlockEntity;
import com.resonance.registry.ModItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ChorusResonatorRenderer implements BlockEntityRenderer<ChorusResonatorBlockEntity, ChorusResonatorRenderState> {

    private final ItemModelResolver itemModelResolver;

    public ChorusResonatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ChorusResonatorRenderState createRenderState() {
        return new ChorusResonatorRenderState();
    }

    @Override
    public void extractRenderState(ChorusResonatorBlockEntity blockEntity, ChorusResonatorRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.hasHarmonic = blockEntity.hasHarmonicFragment();
        state.hasWhisper = blockEntity.hasWhisperFragment();
        state.hasCrystal = blockEntity.hasCrystalShard();
        state.time = (blockEntity.getLevel() != null
                ? (blockEntity.getLevel().getGameTime() % 240000L)
                : 0) + partialTicks;

        if (state.hasHarmonic) {
            this.itemModelResolver.updateForTopItem(state.harmonicFragment,
                    new ItemStack(ModItems.HARMONIC_FRAGMENT.get()), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
        }
        if (state.hasWhisper) {
            this.itemModelResolver.updateForTopItem(state.whisperFragment,
                    new ItemStack(ModItems.WHISPER_FRAGMENT.get()), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
        }
        if (state.hasCrystal) {
            this.itemModelResolver.updateForTopItem(state.crystalShard,
                    new ItemStack(ModItems.CRYSTAL_FRAGMENT.get()), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(ChorusResonatorRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        int slot = 0;
        if (state.hasHarmonic) {
            submitOrbitingShard(state.harmonicFragment, state, poseStack, submitNodeCollector, slot++);
        }
        if (state.hasWhisper) {
            submitOrbitingShard(state.whisperFragment, state, poseStack, submitNodeCollector, slot++);
        }
        if (state.hasCrystal) {
            submitOrbitingShard(state.crystalShard, state, poseStack, submitNodeCollector, slot++);
        }
    }

    private static void submitOrbitingShard(ItemStackRenderState item, ChorusResonatorRenderState state,
                                            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int slot) {
        if (item.isEmpty()) return;

        float angle = state.time * 0.03F + slot * (Mth.TWO_PI / 3.0F);
        float bob = Mth.sin(state.time * 0.06F + slot * 2.1F) * 0.08F;
        float radius = 0.55F;

        poseStack.pushPose();
        poseStack.translate(
                0.5F + Mth.cos(angle) * radius,
                1.25F + bob,
                0.5F + Mth.sin(angle) * radius);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 2.5F + slot * 120.0F));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(ChorusResonatorBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2.5, pos.getZ() + 2);
    }
}
