package com.resonance.item;

import com.resonance.data.ResonantPathData;
import com.resonance.registry.ModSounds;
import com.resonance.registry.ModBlocks;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ResonantShovelItem extends ShovelItem {

    public ResonantShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Properties properties) {
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
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.DIRT_PATH) || state.is(ModBlocks.CRYSTAL_DIRT_PATH.get())) {
                ResonantPathData.get(level).add(pos);
                level.playSound(null, pos, ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 0.5F, 1.5F);
                level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        6, 0.3, 0.1, 0.3, 0.02);
            }
        }
        return result;
    }
}
