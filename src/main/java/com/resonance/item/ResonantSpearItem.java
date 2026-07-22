package com.resonance.item;

import com.resonance.registry.ModEffects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ResonantSpearItem extends Item {

    private static final float BURST_DAMAGE = 7.0F;

    public ResonantSpearItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return;
        if (!target.hasEffect(ModEffects.RESONANCE)) return;

        target.removeEffect(ModEffects.RESONANCE);
        target.hurt(level.damageSources().playerAttack((Player) attacker), BURST_DAMAGE);

        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                15, 0.4, 0.4, 0.4, 0.1);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10, 0.3, 0.3, 0.3, 0.2);
    }
}
