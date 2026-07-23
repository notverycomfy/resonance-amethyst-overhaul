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
import net.minecraft.core.particles.ItemParticleOption;
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
    private static final java.util.List<WraithEmergenceEvent> PENDING_EMERGENCES = new java.util.ArrayList<>();
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
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), player);
                Vec3 direction = mob.position().subtract(player.position()).normalize();
                mob.push(direction.x * 1.5, 0.2, direction.z * 1.5);
            }
            return false;
        }

        if (target instanceof Wolf wolf && !source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR)) {
            ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
            if (armor.is(ModItems.RESONANT_WOLF_ARMOR.get())) {
                armor.hurtAndBreak(Mth.ceil(amount), wolf, EquipmentSlot.BODY);
                // Fabric cancels the hit to protect the wolf, so explicitly sync
                // the vanilla damage interaction that NeoForge retains at 0 damage.
                level.broadcastDamageEvent(wolf, source);
                return false;
            }
        }

        if (target instanceof ServerPlayer player && source.getEntity() instanceof LivingEntity meleeAttacker
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_CHESTPLATE.get())) {
            meleeAttacker.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), player);
        }

        MobEffectInstance resonance = target.getEffect(ModEffects.RESONANCE.holder());
        if (resonance != null) {
            RECENTLY_RESONANT.add(target.getId());
            BONUS_PENDING.add(target.getId());
        }

        // Apply Resonance after checking the existing effect so the first sword
        // hit marks its target without also receiving the follow-up bonus.
        if (attacker != null && source.getDirectEntity() == attacker
                && attacker.getMainHandItem().is(ModItems.RESONANT_SWORD.get())) {
            target.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0), attacker);
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
        MobEffectInstance resonance = target.getEffect(ModEffects.RESONANCE.holder());
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
            int dx = level.getRandom().nextIntBetweenInclusive(-8, 8);
            int dz = level.getRandom().nextIntBetweenInclusive(-8, 8);
            if (dx * dx + dz * dz <= 9) {
                continue;
            }
            BlockPos candidate = origin.offset(dx, 0, dz);
            BlockPos spawnPos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            var stalker = ModEntities.RESONANT_STALKER.get().create(
                    level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (stalker != null) {
                stalker.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                stalker.setYRot(level.getRandom().nextFloat() * 360.0F);
                if (level.noCollision(stalker) && level.addFreshEntity(stalker)) {
                    return;
                }
            }
        }
    }

    private static void afterDeath(LivingEntity dead, net.minecraft.world.damagesource.DamageSource source) {
        if (!(dead.level() instanceof ServerLevel level)
                || (!RECENTLY_RESONANT.remove(dead.getId()) && !dead.hasEffect(ModEffects.RESONANCE.holder()))) {
            return;
        }

        // A player killing a resonating mob has a 1% chance to draw a Stalker.
        if (source.getEntity() instanceof Player && level.getRandom().nextFloat() < 0.01F) {
            trySpawnStalker(level, dead.blockPosition());
        }

        if (level.getRandom().nextFloat() < 0.50F) {
            BlockPos scarPos = dead.blockPosition();
            if (VibrationScars.add(level, scarPos)) {
                java.util.List<BlockPos> scarPositions = VibrationScars.clearNear(level, scarPos, 8.0);
                playWraithEmergence(level, scarPos, scarPositions);
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
        tickEmergences(level);

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
        if (scars.isEmpty()) {
            return;
        }

        var crackDust = new net.minecraft.core.particles.DustParticleOptions(0x3F256B, 0.7F);
        var glowDust = new net.minecraft.core.particles.DustParticleOptions(0x7A5BB5, 0.5F);
        var brightDust = new net.minecraft.core.particles.DustParticleOptions(0xA678F1, 1.0F);
        java.util.List<long[]> scarPairs = new java.util.ArrayList<>();
        java.util.List<Map.Entry<Long, Integer>> scarEntries = new java.util.ArrayList<>(scars.entrySet());
        int[] nearbyCounts = new int[scarEntries.size()];
        java.util.Arrays.fill(nearbyCounts, 1);

        for (int i = 0; i < scarEntries.size(); i++) {
            BlockPos a = BlockPos.of(scarEntries.get(i).getKey());
            for (int j = i + 1; j < scarEntries.size(); j++) {
                BlockPos b = BlockPos.of(scarEntries.get(j).getKey());
                double distSq = a.distSqr(b);
                if (distSq <= 64.0) {
                    nearbyCounts[i]++;
                    nearbyCounts[j]++;
                }
                if (distSq <= 25.0) {
                    scarPairs.add(new long[]{scarEntries.get(i).getKey(), scarEntries.get(j).getKey()});
                }
            }
        }

        for (int scarIndex = 0; scarIndex < scarEntries.size(); scarIndex++) {
            Map.Entry<Long, Integer> entry = scarEntries.get(scarIndex);
            float freshness = entry.getValue() / (float) VibrationScars.SCAR_DURATION;
            BlockPos pos = BlockPos.of(entry.getKey());
            double groundY = getScarGroundY(level, pos);
            double x = pos.getX() + 0.5;
            double z = pos.getZ() + 0.5;
            int nearby = nearbyCounts[scarIndex];
            float intensity = Math.min(1.0F, freshness + nearby * 0.1F);

            if (level.getRandom().nextFloat() < 0.2F * intensity) {
                level.sendParticles(crackDust, x, groundY + 0.02, z, 3, 0.15, 0.0, 0.15, 0.0);
                level.sendParticles(glowDust, x, groundY + 0.03, z, 1, 0.2, 0.0, 0.2, 0.0);
            }
            if (nearby >= 3 && level.getRandom().nextFloat() < 0.06F * intensity) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, groundY + 0.02, z,
                        1, 0.1, 0.0, 0.1, 0.001);
                level.sendParticles(ParticleTypes.SMOKE, x, groundY + 0.05, z,
                        1, 0.15, 0.02, 0.15, 0.002);
            }
            if (nearby >= 6 && level.getRandom().nextFloat() < 0.04F * intensity) {
                level.sendParticles(brightDust, x, groundY + 0.03, z, 3, 0.2, 0.0, 0.2, 0.0);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, groundY + 0.05, z,
                        2, 0.3, 0.05, 0.3, 0.01);
                if (level.getRandom().nextFloat() < 0.1F) {
                    level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM,
                            SoundSource.AMBIENT, 0.4F, 0.5F + level.getRandom().nextFloat() * 0.3F);
                }
            }
        }

        for (long[] pair : scarPairs) {
            if (level.getRandom().nextFloat() > 0.12F) {
                continue;
            }
            BlockPos a = BlockPos.of(pair[0]);
            BlockPos b = BlockPos.of(pair[1]);
            double ax = a.getX() + 0.5;
            double az = a.getZ() + 0.5;
            double bx = b.getX() + 0.5;
            double bz = b.getZ() + 0.5;
            double ay = getScarGroundY(level, a);
            double by = getScarGroundY(level, b);
            int steps = (int) Math.ceil(Math.sqrt(a.distSqr(b))) * 2;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                double lx = ax + (bx - ax) * t + (level.getRandom().nextFloat() - 0.5) * 0.15;
                double ly = ay + (by - ay) * t + 0.02;
                double lz = az + (bz - az) * t + (level.getRandom().nextFloat() - 0.5) * 0.15;
                level.sendParticles(crackDust, lx, ly, lz, 1, 0.0, 0.0, 0.0, 0.0);
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
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(),
                        Config.RESONANCE_DURATION.getAsInt(), 0));
            }
        }
    }

    private static void applyHostileAura(ServerLevel level, LivingEntity source, double radius) {
        boolean applied = false;
        for (Monster mob : level.getEntitiesOfClass(Monster.class, source.getBoundingBox().inflate(radius))) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(), Config.RESONANCE_DURATION.getAsInt(), 0));
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
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE.holder(),
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

    private static double getScarGroundY(ServerLevel level, BlockPos pos) {
        BlockPos ground = pos.below();
        return level.getBlockState(ground).isSolidRender() ? ground.getY() + 1.0 : pos.getY();
    }

    private static final class WraithEmergenceEvent {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final BlockPos center;
        private final java.util.List<BlockPos> scarPositions;
        private int ticksRemaining;
        private int stage;

        private WraithEmergenceEvent(net.minecraft.resources.ResourceKey<Level> dimension,
                                    BlockPos center, java.util.List<BlockPos> scarPositions) {
            this.dimension = dimension;
            this.center = center;
            this.scarPositions = scarPositions;
        }
    }

    private static void playWraithEmergence(ServerLevel level, BlockPos center,
                                            java.util.List<BlockPos> scarPositions) {
        PENDING_EMERGENCES.add(new WraithEmergenceEvent(level.dimension(), center, scarPositions));
    }

    private static void tickEmergences(ServerLevel level) {
        var iterator = PENDING_EMERGENCES.iterator();
        while (iterator.hasNext()) {
            WraithEmergenceEvent event = iterator.next();
            if (!event.dimension.equals(level.dimension())) {
                continue;
            }
            if (event.ticksRemaining > 0) {
                event.ticksRemaining--;
                continue;
            }

            var brightPurple = new net.minecraft.core.particles.DustParticleOptions(0xA678F1, 1.5F);
            var hotWhite = new net.minecraft.core.particles.DustParticleOptions(0xE8D0FF, 2.0F);
            switch (event.stage) {
                case 0 -> {
                    level.playSound(null, event.center, SoundEvents.BEACON_ACTIVATE,
                            SoundSource.HOSTILE, 1.5F, 0.4F);
                    for (BlockPos scar : event.scarPositions) {
                        double x = scar.getX() + 0.5;
                        double y = getScarGroundY(level, scar);
                        double z = scar.getZ() + 0.5;
                        level.sendParticles(brightPurple, x, y + 0.05, z,
                                15, 0.2, 0.0, 0.2, 0.0);
                        level.sendParticles(hotWhite, x, y + 0.1, z,
                                5, 0.1, 0.0, 0.1, 0.0);
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.05, z,
                                4, 0.15, 0.0, 0.15, 0.005);
                    }
                    event.stage = 1;
                    event.ticksRemaining = 30;
                }
                case 1 -> {
                    level.playSound(null, event.center, ModSounds.CRYSTAL_WRAITH_ARMOR_BREAK.get(),
                            SoundSource.HOSTILE, 1.4F, 0.72F);
                    level.playSound(null, event.center, SoundEvents.GLASS_BREAK,
                            SoundSource.HOSTILE, 1.5F, 0.6F);
                    for (BlockPos scar : event.scarPositions) {
                        double x = scar.getX() + 0.5;
                        double y = getScarGroundY(level, scar);
                        double z = scar.getZ() + 0.5;
                        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.2, z,
                                1, 0.0, 0.0, 0.0, 0.0);
                        level.sendParticles(brightPurple, x, y + 0.5, z,
                                20, 0.3, 0.5, 0.3, 0.05);
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 0.3, z,
                                10, 0.2, 0.3, 0.2, 0.05);
                        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
                        level.sendParticles(shard, x, y + 0.2, z, 8, 0.2, 0.0, 0.2, 0.4);
                    }

                    double centerX = event.center.getX() + 0.5;
                    double centerY = getScarGroundY(level, event.center);
                    double centerZ = event.center.getZ() + 0.5;
                    for (BlockPos scar : event.scarPositions) {
                        double scarX = scar.getX() + 0.5;
                        double scarY = getScarGroundY(level, scar);
                        double scarZ = scar.getZ() + 0.5;
                        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(scar.distSqr(event.center))) * 3);
                        for (int i = 0; i <= steps; i++) {
                            float progress = i / (float) steps;
                            level.sendParticles(hotWhite,
                                    scarX + (centerX - scarX) * progress,
                                    scarY + (centerY - scarY) * progress + 0.1,
                                    scarZ + (centerZ - scarZ) * progress,
                                    1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                    event.stage = 2;
                    event.ticksRemaining = 20;
                }
                case 2 -> {
                    double centerX = event.center.getX() + 0.5;
                    double centerY = getScarGroundY(level, event.center);
                    double centerZ = event.center.getZ() + 0.5;
                    for (int y = 0; y < 12; y++) {
                        level.sendParticles(brightPurple, centerX, centerY + y * 0.25, centerZ,
                                5, 0.3, 0.0, 0.3, 0.0);
                        level.sendParticles(ParticleTypes.END_ROD, centerX, centerY + y * 0.25, centerZ,
                                2, 0.15, 0.0, 0.15, 0.02);
                    }
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10);
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                centerX + Math.cos(angle) * 2.0, centerY + 0.1,
                                centerZ + Math.sin(angle) * 2.0,
                                1, 0.0, 0.1, 0.0, 0.01);
                    }
                    var wraith = ModEntities.CRYSTAL_WRAITH.get().create(
                            level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                    if (wraith != null) {
                        wraith.setPos(centerX, centerY, centerZ);
                        wraith.setYRot(level.getRandom().nextFloat() * 360.0F);
                        level.addFreshEntity(wraith);
                    }
                    iterator.remove();
                }
                default -> iterator.remove();
            }
        }
    }

    private static boolean isOnResonantBlock(Player player) {
        BlockState below = player.level().getBlockState(player.blockPosition().below());
        return below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(Blocks.DEEPSLATE)
                || below.is(Blocks.AMETHYST_BLOCK) || below.is(Blocks.BUDDING_AMETHYST);
    }
}
