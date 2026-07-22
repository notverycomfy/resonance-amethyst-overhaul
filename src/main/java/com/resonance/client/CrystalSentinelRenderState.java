package com.resonance.client;

import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CrystalSentinelRenderState extends ShulkerRenderState {
    public @Nullable Vec3 beamTargetPosition;
    public Vec3 eyePosition = Vec3.ZERO;
    public float beamTime;
    public float beamScale;
}
