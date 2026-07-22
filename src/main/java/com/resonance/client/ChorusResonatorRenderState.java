package com.resonance.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ChorusResonatorRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState harmonicFragment = new ItemStackRenderState();
    public final ItemStackRenderState whisperFragment = new ItemStackRenderState();
    public final ItemStackRenderState crystalShard = new ItemStackRenderState();
    public boolean hasHarmonic;
    public boolean hasWhisper;
    public boolean hasCrystal;
    public float time;
}
