package com.resonance.block.entity;

import com.resonance.Config;
import com.resonance.registry.ModBlockEntities;
import com.resonance.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ResonantLanternBlockEntity extends BlockEntity {

    private int tickCounter = 0;

    public ResonantLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_LANTERN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonantLanternBlockEntity be) {
        be.tickCounter++;
        if (be.tickCounter >= 60) {
            be.tickCounter = 0;
            if (level instanceof ServerLevel serverLevel) {
                AABB area = new AABB(pos).inflate(12.0);
                for (Monster mob : serverLevel.getEntitiesOfClass(Monster.class, area)) {
                    mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, 100, 0));
                }
            }
        }
    }
}
