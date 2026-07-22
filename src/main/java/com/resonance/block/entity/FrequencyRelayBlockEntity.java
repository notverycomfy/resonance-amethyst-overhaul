package com.resonance.block.entity;

import com.resonance.registry.ModBlockEntities;
import com.resonance.registry.ModEffects;
import com.mojang.serialization.Codec;
import com.resonance.block.FrequencyRelayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class FrequencyRelayBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos linkedPos = null;
    private int signalTicks = 0;
    private int tickCounter = 0;

    public FrequencyRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FREQUENCY_RELAY.get(), pos, state);
    }

    public void setLinkedPos(@Nullable BlockPos pos) {
        this.linkedPos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getLinkedPos() {
        return linkedPos;
    }

    public int getRedstoneSignal() {
        return signalTicks > 0 ? 15 : 0;
    }

    public void activateSignal() {
        this.signalTicks = 20;
        setChanged();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            setActiveState(true);
        }
    }

    private void setActiveState(boolean active) {
        if (level != null && getBlockState().getValue(FrequencyRelayBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(FrequencyRelayBlock.ACTIVE, active), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FrequencyRelayBlockEntity be) {
        if (be.signalTicks > 0) {
            be.signalTicks--;
            if (be.signalTicks == 0) {
                be.setChanged();
                level.updateNeighborsAt(pos, state.getBlock());
                be.setActiveState(false);
            }
        }

        be.tickCounter++;
        if (be.tickCounter >= 10) {
            be.tickCounter = 0;
            if (be.linkedPos != null && level instanceof ServerLevel serverLevel) {
                AABB area = new AABB(pos).inflate(8.0);
                boolean found = false;
                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (entity.hasEffect(ModEffects.RESONANCE)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    if (serverLevel.getBlockEntity(be.linkedPos) instanceof FrequencyRelayBlockEntity partner) {
                        partner.activateSignal();
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (linkedPos != null) {
            output.store("LinkedPos", BlockPos.CODEC, linkedPos);
        }
        output.store("SignalTicks", Codec.INT, signalTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("LinkedPos", BlockPos.CODEC).ifPresent(p -> this.linkedPos = p);
        this.signalTicks = input.read("SignalTicks", Codec.INT).orElse(0);
    }
}
