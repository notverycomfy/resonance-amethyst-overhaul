package com.resonance.client;

import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HarmonicAnchorRenderState extends ThrownItemRenderState {
    public @Nullable Vec3 beamTargetPosition;
    public Vec3 anchorPosition = Vec3.ZERO;
    public float beamTime;
}
