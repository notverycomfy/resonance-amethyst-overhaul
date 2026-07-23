package com.resonance.event;

import com.resonance.Config;
import com.resonance.Resonance;
import com.resonance.block.CrystalLogBlock;
import com.resonance.data.HarmonizedFarmlandData;
import com.resonance.data.ResonantPathData;
import com.resonance.data.VibrationScars;
import com.resonance.entity.ShatteredEchoEntity;
import com.resonance.registry.ModBlocks;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModItems;
import com.resonance.registry.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Fabric event bridge for Resonance's loader-independent gameplay.
 */
public final class ResonanceEvents {
    private static final Identifier LEGGINGS_SPEED_ID = Identifier.fromNamespaceAndPath(Resonance.MODID, "leggings_speed");
    private static final Identifier PATH_SPEED_ID = Identifier.fromNamespaceAndPath(Resonance.MODID, "path_speed");
    private static final Map<UUID, Integer> SHIELD_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Float> PATH_SPEED_BONUS = new HashMap<>();
    private static final Set<Integer> RECENTLY_RESONANT = new HashSet<>();
    private static final Set<Integer> REAPPLYING_DAMAGE = new HashSet<>();
    private static final Set<Integer> BONUS_PENDING = new HashSet<>();
    private static final float MAX_PATH_SPEED = 0.20F;

    private ResonanceEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(ResonanceEvents::beforeDamage);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(ResonanceEvents::afterDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(ResonanceEvents::afterDeath);
        ServerTickEvents.END_LEVEL_TICK.register(ResonanceEvents::levelTick);
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
                afterBlockBreak(level, player, pos, state));
        UseBlockCallback.EVENT.register(ResonanceEvents::useBlock);
    }

    private static boolean beforeDamage(LivingEntity target, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(target.level() instanceof ServerLevel level)) {
            return true;
        }
        if (REAPPLYING_DAMAGE.contains(target.getId())) {
            return true;
        }

        Player attacker = source.getEntity() instanceof Player player ? player : null;
        if (target instanceof ServerPlayer player
                && !source.is(net.minecraft.world.damagesource.DamageTypes.FALL)
                && hasFullResonantArmor(player)
                && !SHIELD_COOLDOWNS.containsKey(player.getUUID())) {
            SHIELD_COOLDOWNS.put(player.getUUID(), Config.HARMONIC_SHIELD_COOLDOWN.getAsInt());
            level.playSound(null, player.blockPosition(), ModSounds.HARMONIC_SHIELD_ABSORB.get(), SoundSource.PLAYERS, 1.2F, 0.6F);
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 1.0, 1.0, 1.0, 0.1);
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0),
                    entity -> entity != player)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), player);
                Vec3 direction = mob.position().subtract(player.position()).normalize();
                mob.knockback(1.5, direction.x, direction.z);
            }
            return false;
        }

        if (target instanceof Wolf wolf && !source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR)) {
            ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
            if (armor.is(ModItems.RESONANT_WOLF_ARMOR.get())) {
                armor.hurtAndBreak(Mth.ceil(amount), wolf, EquipmentSlot.BODY);
                return false;
            }
        }

        if (target instanceof ServerPlayer player && source.getEntity() instanceof LivingEntity meleeAttacker
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_CHESTPLATE.get())) {
            meleeAttacker.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), player);
        }

        MobEffectInstance resonance = target.getEffect(ModEffects.RESONANCE);
        if (resonance != null) {
            RECENTLY_RESONANT.add(target.getId());
            BONUS_PENDING.add(target.getId());
            if (attacker != null && level.getRandom().nextFloat() < 0.01F) {
                trySpawnStalker(level, target.blockPosition());
            }
        }

        // Apply Resonance after checking the existing effect so the first sword
        // hit marks its target without also receiving the follow-up bonus.
        if (attacker != null && source.getDirectEntity() == attacker
                && attacker.getMainHandItem().is(ModItems.RESONANT_SWORD.get())) {
            target.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), attacker);
            RECENTLY_RESONANT.add(target.getId());
            level.playSound(null, target.blockPosition(), ModSounds.RESONANCE_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.3F);
        }
        return true;
    }

    private static void afterDamage(LivingEntity target, net.minecraft.world.damagesource.DamageSource source,
                                    float damageTaken, float damageBlocked, boolean blocked) {
        boolean bonusPending = BONUS_PENDING.remove(target.getId());
        if (!(target.level() instanceof ServerLevel level) || blocked || damageTaken <= 0.0F
                || REAPPLYING_DAMAGE.contains(target.getId()) || !bonusPending) {
            return;
        }
        MobEffectInstance resonance = target.getEffect(ModEffects.RESONANCE);
        if (resonance == null || !REAPPLYING_DAMAGE.add(target.getId())) {
            return;
        }
        float bonus = damageTaken * (float) (Config.RESONANCE_DAMAGE_BONUS.getAsDouble()
                * (resonance.getAmplifier() + 1));
        try {
            target.hurtServer(level, source, bonus);
        } finally {
            REAPPLYING_DAMAGE.remove(target.getId());
        }
    }

    private static void trySpawnStalker(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos candidate = origin.offset(
                    level.getRandom().nextIntBetweenInclusive(-8, 8), 0,
                    level.getRandom().nextIntBetweenInclusive(-8, 8));
            BlockPos spawnPos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            if (spawnPos.distSqr(origin) <= 9.0) {
                continue;
            }
            var stalker = ModEntities.RESONANT_STALKER.get().create(
                    level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (stalker != null) {
                stalker.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                stalker.setYRot(level.getRandom().nextFloat() * 360.0F);
                level.addFreshEntity(stalker);
            }
            return;
        }
    }

    private static void afterDeath(LivingEntity dead, net.minecraft.world.damagesource.DamageSource source) {
        if (!(dead.level() instanceof ServerLevel level)
                || (!RECENTLY_RESONANT.remove(dead.getId()) && !dead.hasEffect(ModEffects.RESONANCE))) {
            return;
        }

        if (level.getRandom().nextFloat() < 0.50F) {
            BlockPos scarPos = dead.blockPosition();
            if (VibrationScars.add(level, scarPos)) {
                VibrationScars.clearNear(level, scarPos, 8.0);
                var wraith = ModEntities.CRYSTAL_WRAITH.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if (wraith != null) {
                    wraith.setPos(scarPos.getX() + 0.5, scarPos.getY(), scarPos.getZ() + 0.5);
                    level.addFreshEntity(wraith);
                }
            }
        }

        if (level.getRandom().nextFloat() < 0.10F) {
            BlockPos origin = dead.blockPosition();
            int placed = 0;
            for (int attempt = 0; attempt < 40 && placed < 4; attempt++) {
                BlockPos target = origin.offset(level.getRandom().nextInt(5) - 2,
                        level.getRandom().nextInt(3) - 1, level.getRandom().nextInt(5) - 2);
                BlockState state = level.getBlockState(target);
                if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.DEEPSLATE)) {
                    level.setBlockAndUpdate(target, Blocks.AMETHYST_BLOCK.defaultBlockState());
                    placed++;
                }
            }
        }
    }

    private static void levelTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            tickPlayer(level, player);
        }
        for (var entity : level.getAllEntities()) {
            if (entity.tickCount % 40 == 0) {
                tickMountAura(level, entity);
            }
        }

        if (level.getGameTime() % 200 == 0) {
            ResonantPathData.get(level).pruneInvalid(level);
            HarmonizedFarmlandData.get(level).pruneInvalid(level);
        }

        Map<Long, Integer> scars = VibrationScars.tick(level);
        for (long packed : scars.keySet()) {
            if (level.getRandom().nextFloat() < 0.25F) {
                BlockPos pos = BlockPos.of(packed);
                level.sendParticles(new net.minecraft.core.particles.DustParticleOptions(0x7A5BB5, 0.7F),
                        pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5,
                        2, 0.15, 0.02, 0.15, 0.0);
            }
        }
    }

    private static void tickMountAura(ServerLevel level, net.minecraft.world.entity.Entity entity) {
        if (entity instanceof Wolf wolf
                && wolf.getItemBySlot(EquipmentSlot.BODY).is(ModItems.RESONANT_WOLF_ARMOR.get())) {
            applyHostileAura(level, wolf, 5.0);
        } else if (entity instanceof AbstractHorse horse
                && horse.getItemBySlot(EquipmentSlot.BODY).is(ModItems.RESONANT_HORSE_ARMOR.get())) {
            applyHostileAura(level, horse, 8.0);
        } else if (entity instanceof AbstractNautilus nautilus
                && nautilus.getItemBySlot(EquipmentSlot.BODY).is(ModItems.RESONANT_NAUTILUS_ARMOR.get())) {
            if (nautilus.isInWater()) {
                for (var passenger : nautilus.getPassengers()) {
                    if (passenger instanceof LivingEntity rider) {
                        rider.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 60, 0,
                                true, false, true));
                    }
                }
            }
            for (Monster mob : level.getEntitiesOfClass(Monster.class,
                    nautilus.getBoundingBox().inflate(6.0), Monster::isInWater)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE,
                        Config.RESONANCE_DURATION.getAsInt(), 0));
            }
        }
    }

    private static void applyHostileAura(ServerLevel level, LivingEntity source, double radius) {
        boolean applied = false;
        for (Monster mob : level.getEntitiesOfClass(Monster.class, source.getBoundingBox().inflate(radius))) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0));
            applied = true;
        }
        if (applied) {
            level.sendParticles(ParticleTypes.END_ROD, source.getX(), source.getY() + 0.5, source.getZ(),
                    8, 1.0, 0.3, 1.0, 0.02);
        }
    }

    private static void tickPlayer(ServerLevel level, ServerPlayer player) {
        ShatteredEchoEntity.trySpawnGeodeEncounter(level, player);
        UUID id = player.getUUID();
        Integer cooldown = SHIELD_COOLDOWNS.get(id);
        if (cooldown != null) {
            if (cooldown <= 1) {
                SHIELD_COOLDOWNS.remove(id);
                level.playSound(null, player.blockPosition(), ModSounds.HARMONIC_SHIELD_RECHARGE.get(),
                        SoundSource.PLAYERS, 0.6F, 1.5F);
            } else {
                SHIELD_COOLDOWNS.put(id, cooldown - 1);
            }
        }

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.RESONANT_LEGGINGS.get())
                && isOnResonantBlock(player)) {
            if (speed.getModifier(LEGGINGS_SPEED_ID) == null) {
                speed.addTransientModifier(new AttributeModifier(LEGGINGS_SPEED_ID, 0.1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            speed.removeModifier(LEGGINGS_SPEED_ID);
        }

        BlockPos feet = player.blockPosition();
        boolean onPath = isMarkedPath(level, feet) || isMarkedPath(level, feet.below());
        if (onPath) {
            level.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.1, player.getZ(),
                    1, 0.2, 0.0, 0.2, 0.01);
        }
        float bonus = PATH_SPEED_BONUS.getOrDefault(id, 0.0F);
        bonus = onPath ? Math.min(MAX_PATH_SPEED, bonus + 0.02F) : Math.max(0.0F, bonus - 0.04F);
        speed.removeModifier(PATH_SPEED_ID);
        if (bonus > 0.0F) {
            speed.addTransientModifier(new AttributeModifier(PATH_SPEED_ID, bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            PATH_SPEED_BONUS.put(id, bonus);
        } else {
            PATH_SPEED_BONUS.remove(id);
        }
    }

    private static InteractionResult useBlock(Player player, Level level, net.minecraft.world.InteractionHand hand,
                                              net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        ServerLevel server = (ServerLevel) level;
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getItemInHand(hand);
        BlockState replacement = null;

        if (held.getItem() instanceof AxeItem && state.getBlock() instanceof CrystalLogBlock log) {
            replacement = log.getStrippedState(state);
        } else if (held.getItem() instanceof ShovelItem && isCrystalDirt(state)) {
            replacement = ModBlocks.CRYSTAL_DIRT_PATH.get().defaultBlockState();
            if (held.is(ModItems.RESONANT_SHOVEL.get())) {
                ResonantPathData.get(server).add(pos);
            }
        } else if (held.getItem() instanceof HoeItem) {
            if (state.is(ModBlocks.COARSE_CRYSTAL_DIRT.get()) || state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get())) {
                replacement = ModBlocks.CRYSTAL_DIRT.get().defaultBlockState();
                if (state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get())) {
                    Block.popResource(level, pos, new ItemStack(Items.HANGING_ROOTS));
                }
            } else if (state.is(ModBlocks.CRYSTAL_DIRT.get()) || state.is(ModBlocks.CRYSTAL_GRASS_BLOCK.get())) {
                replacement = ModBlocks.CRYSTAL_FARMLAND.get().defaultBlockState();
                if (held.is(ModItems.RESONANT_HOE.get())) {
                    HarmonizedFarmlandData.get(server).add(pos);
                }
            }
        }

        if (replacement != null) {
            level.setBlockAndUpdate(pos, replacement);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        if (state.getBlock() instanceof SculkSensorBlock
                && (held.is(ModItems.RESONANT_SWORD.get()) || held.is(ModItems.RESONANT_AXE.get())
                || held.is(ModItems.RESONANT_SPEAR.get()))) {
            for (Monster mob : server.getEntitiesOfClass(Monster.class, new AABB(pos).inflate(8.0))) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE,
                        Config.RESONANCE_DURATION.getAsInt() * 2, 1), player);
            }
            server.playSound(null, pos, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, 0.6F);
            server.playSound(null, pos, ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 1.2F, 0.8F);
            server.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, 20, 2.0, 1.0, 2.0, 0.05);
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, 15, 2.0, 1.0, 2.0, 0.05);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState broken) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ResonantPathData.get(server).remove(pos);
        HarmonizedFarmlandData.get(server).remove(pos);
        if (!player.getMainHandItem().is(ModItems.RESONANT_PICKAXE.get())
                || server.getRandom().nextFloat() >= 0.15F) {
            return;
        }
        int count = 0;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (!nearby.equals(pos) && server.getBlockState(nearby).is(broken.getBlock())) {
                server.destroyBlock(nearby, true, player);
                if (++count >= 3) {
                    break;
                }
            }
        }
        if (count > 0) {
            server.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK,
                    SoundSource.BLOCKS, 1.0F, 1.2F);
            server.playSound(null, pos, ModSounds.RESONANCE_CHIME.get(),
                    SoundSource.BLOCKS, 0.6F, 1.3F);
            server.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private static boolean isCrystalDirt(BlockState state) {
        return state.is(ModBlocks.CRYSTAL_DIRT.get()) || state.is(ModBlocks.CRYSTAL_GRASS_BLOCK.get())
                || state.is(ModBlocks.COARSE_CRYSTAL_DIRT.get()) || state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get());
    }

    private static boolean isMarkedPath(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return (state.is(Blocks.DIRT_PATH) || state.is(ModBlocks.CRYSTAL_DIRT_PATH.get()))
                && ResonantPathData.get(level).isResonantPath(pos);
    }

    private static boolean hasFullResonantArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.RESONANT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.RESONANT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.RESONANT_BOOTS.get());
    }

    private static boolean isOnResonantBlock(Player player) {
        BlockState below = player.level().getBlockState(player.blockPosition().below());
        return below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(Blocks.DEEPSLATE)
                || below.is(Blocks.AMETHYST_BLOCK) || below.is(Blocks.BUDDING_AMETHYST);
    }
}
