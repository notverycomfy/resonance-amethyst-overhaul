package com.resonance.item;

import com.resonance.Config;
import com.resonance.registry.ModEffects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ResonantAxeItem extends AxeItem {

    public ResonantAxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
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
        if (level.getRandom().nextFloat() >= 0.40F) return;

        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
        double cx = target.getX(), cy = target.getY() + 0.5, cz = target.getZ();
        for (int i = 0; i < 15; i++) {
            double dx = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dy = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dz = level.getRandom().nextDouble() * 2.0 - 1.0;
            Vec3 dir = new Vec3(dx, dy, dz).normalize();
            level.sendParticles(shard, cx, cy, cz, 0, dir.x, dir.y, dir.z, 0.4);
        }

        AABB area = target.getBoundingBox().inflate(3.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != attacker && e != target)) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), attacker);
            mob.hurtServer(level, level.damageSources().mobAttack(attacker), 1.0F);
            Vec3 knockback = mob.position().subtract(target.position()).normalize();
            mob.knockback(0.8, knockback.x, knockback.z);
        }
    }
}
