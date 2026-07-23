package com.resonance.mixin;

import com.resonance.data.HarmonizedFarmlandData;
import com.resonance.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void resonance$harmonizedGrowth(BlockState state, ServerLevel level, BlockPos pos,
                                             RandomSource random, CallbackInfo callback) {
        BlockPos farmlandPos = pos.below();
        if (!HarmonizedFarmlandData.get(level).isHarmonized(farmlandPos)) {
            return;
        }
        BlockState farmland = level.getBlockState(farmlandPos);
        if (!farmland.is(Blocks.FARMLAND) && !farmland.is(ModBlocks.CRYSTAL_FARMLAND.get())) {
            HarmonizedFarmlandData.get(level).remove(farmlandPos);
            return;
        }
        CropBlock crop = (CropBlock) (Object) this;
        int age = crop.getAge(state);
        if (age < crop.getMaxAge() && random.nextFloat() < 0.20F) {
            level.setBlock(pos, crop.getStateForAge(age + 1), 2);
        }
    }
}
