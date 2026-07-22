package com.resonance.item;

import com.resonance.data.HarmonizedFarmlandData;
import com.resonance.registry.ModBlocks;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.resonance.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

public class ResonantHoeItem extends HoeItem {

    public ResonantHoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        if (result.consumesAction() && context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos();
            if (level.getBlockState(pos).is(Blocks.FARMLAND)
                    || level.getBlockState(pos).is(ModBlocks.CRYSTAL_FARMLAND.get())) {
                HarmonizedFarmlandData.get(level).add(pos);
                level.playSound(null, pos, ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 0.4F, 1.6F);
                level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        5, 0.3, 0.1, 0.3, 0.02);
            }
        }
        return result;
    }
}
