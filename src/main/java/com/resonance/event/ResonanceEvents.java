package com.resonance.event;

import com.resonance.Config;
import com.resonance.Resonance;
import com.resonance.data.HarmonizedFarmlandData;
import com.resonance.data.ResonantPathData;
import com.resonance.data.VibrationScars;
import com.resonance.entity.ShatteredEchoEntity;
import com.resonance.registry.ModEffects;
import com.resonance.registry.ModEntities;
import com.resonance.registry.ModItems;
import com.resonance.registry.ModBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.resonance.registry.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Resonance.MODID)
public class ResonanceEvents {

    @SubscribeEvent
    public static void onCrystalDirtToolUse(BlockEvent.BlockToolModificationEvent event) {
        BlockState state = event.getState();
        if (event.getItemAbility() == ItemAbilities.SHOVEL_FLATTEN
                && (state.is(ModBlocks.CRYSTAL_DIRT.get()) || state.is(ModBlocks.CRYSTAL_GRASS_BLOCK.get())
                || state.is(ModBlocks.COARSE_CRYSTAL_DIRT.get()) || state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get()))) {
            event.setFinalState(ModBlocks.CRYSTAL_DIRT_PATH.get().defaultBlockState());
        } else if (event.getItemAbility() == ItemAbilities.HOE_TILL) {
            if (state.is(ModBlocks.COARSE_CRYSTAL_DIRT.get()) || state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get())) {
                event.setFinalState(ModBlocks.CRYSTAL_DIRT.get().defaultBlockState());
                if (state.is(ModBlocks.ROOTED_CRYSTAL_DIRT.get()) && !event.isSimulated()
                        && event.getLevel() instanceof net.minecraft.world.level.Level level) {
                    BlockPos pos = event.getPos();
                    Block.popResource(level, pos, new ItemStack(Items.HANGING_ROOTS));
                }
            } else if (state.is(ModBlocks.CRYSTAL_DIRT.get()) || state.is(ModBlocks.CRYSTAL_GRASS_BLOCK.get())) {
                event.setFinalState(ModBlocks.CRYSTAL_FARMLAND.get().defaultBlockState());
            }
        }
    }

    private static final Identifier LEGGINGS_SPEED_ID = Identifier.fromNamespaceAndPath(Resonance.MODID, "leggings_speed");
    private static final Identifier PATH_SPEED_ID = Identifier.fromNamespaceAndPath(Resonance.MODID, "path_speed");
    private static final Map<UUID, Integer> shieldCooldowns = new HashMap<>();
    private static final Map<UUID, Float> pathSpeedBonus = new HashMap<>();
    private static final java.util.List<WraithEmergenceEvent> pendingEmergences = new java.util.ArrayList<>();
    private static final java.util.Set<Integer> recentlyResonant = new java.util.HashSet<>();
    private static final float MAX_PATH_SPEED = 0.20F;
    private static final float PATH_SPEED_INC = 0.02F;
    private static final float PATH_SPEED_DEC = 0.04F;

    // --- Resonance damage bonus ---
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        MobEffectInstance resonance = target.getEffect(ModEffects.RESONANCE);
        if (resonance != null) {
            recentlyResonant.add(target.getId());
            float multiplier = 1.0F + (float) (Config.RESONANCE_DAMAGE_BONUS.getAsDouble() * (resonance.getAmplifier() + 1));
            event.setAmount(event.getAmount() * multiplier);
        }

        // Apply Resonance before damage resolves so sweeping targets and lethal
        // first hits both count as Resonance kills. This intentionally happens
        // after the bonus check so the application hit does not boost itself.
        if (event.getSource().getEntity() instanceof Player attacker
                && event.getSource().getDirectEntity() == attacker
                && attacker.getMainHandItem().is(ModItems.RESONANT_SWORD.get())) {
            target.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), attacker);
            recentlyResonant.add(target.getId());
            if (target.level() instanceof ServerLevel level) {
                level.playSound(null, target.blockPosition(), ModSounds.RESONANCE_CHIME.get(), SoundSource.PLAYERS,
                        0.8F, 1.2F + level.getRandom().nextFloat() * 0.4F);
                level.sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(),
                        4, 0.2, 0.1, 0.2, 0.02);
            }
        }

        // --- Harmonic Shield: absorb hit if full armor set (not fall damage) ---
        if (target instanceof ServerPlayer player) {
            if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) return;
            if (hasFullResonantArmor(player) && !shieldOnCooldown(player)) {
                event.setCanceled(true);
                startShieldCooldown(player);
                ServerLevel level = (ServerLevel) player.level();
                double cx = player.getX();
                double cy = player.getY() + 1.0;
                double cz = player.getZ();
                level.playSound(null, player.blockPosition(), ModSounds.HARMONIC_SHIELD_ABSORB.get(), SoundSource.PLAYERS, 1.2F, 0.6F);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 1.2F);
                level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 30, 1.0, 1.0, 1.0, 0.1);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
                // Shoot amethyst shards outward in all directions
                ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
                for (int i = 0; i < 40; i++) {
                    double dx = level.getRandom().nextDouble() * 2.0 - 1.0;
                    double dy = level.getRandom().nextDouble() * 2.0 - 1.0;
                    double dz = level.getRandom().nextDouble() * 2.0 - 1.0;
                    Vec3 dir = new Vec3(dx, dy, dz).normalize();
                    level.sendParticles(shard, cx, cy, cz, 0, dir.x, dir.y, dir.z, 0.7);
                }
                // Apply Resonance + knockback to nearby mobs
                AABB area = player.getBoundingBox().inflate(5.0);
                for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
                    mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), player);
                    var counterSource = level.damageSources().mobAttack(player);
                    mob.hurtServer(level, counterSource, 1.0F);
                    Vec3 knockbackDir = mob.position().subtract(player.position()).normalize();
                    mob.knockback(1.5, knockbackDir.x, knockbackDir.z, counterSource, 1.0F);
                }
            }
        }

        // --- Chestplate: apply Resonance to melee attacker ---
        if (target instanceof ServerPlayer player && event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.is(ModItems.RESONANT_CHESTPLATE.get())) {
                attacker.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0), player);
            }
        }

        // --- Resonant Wolf Armor: redirect all damage into the armor item ---
        if (target instanceof Wolf wolf && !event.getSource().is(DamageTypeTags.BYPASSES_WOLF_ARMOR)) {
            ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
            if (armor.is(ModItems.RESONANT_WOLF_ARMOR.get())) {
                armor.hurtAndBreak(Mth.ceil(event.getAmount()), wolf, EquipmentSlot.BODY);
                event.setAmount(0);
            }
        }
    }

    // --- Player tick: helmet glow, leggings speed, shovel speed, shield cooldown ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) player.level();

        ShatteredEchoEntity.trySpawnGeodeEncounter(level, player);

        // Shield cooldown tick
        UUID id = player.getUUID();
        if (shieldCooldowns.containsKey(id)) {
            int remaining = shieldCooldowns.get(id) - 1;
            if (remaining <= 0) {
                shieldCooldowns.remove(id);
                level.playSound(null, player.blockPosition(), ModSounds.HARMONIC_SHIELD_RECHARGE.get(), SoundSource.PLAYERS, 0.6F, 1.5F);
            } else {
                shieldCooldowns.put(id, remaining);
            }
        }

        // --- Leggings: speed boost on stone/deepslate/amethyst ---
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            if (leggings.is(ModItems.RESONANT_LEGGINGS.get()) && isOnResonantBlock(player)) {
                if (speedAttr.getModifier(LEGGINGS_SPEED_ID) == null) {
                    speedAttr.addTransientModifier(new AttributeModifier(LEGGINGS_SPEED_ID, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            } else {
                speedAttr.removeModifier(LEGGINGS_SPEED_ID);
            }
        }

        // --- Shovel: resonant path stacking speed boost (up to +20%) ---
        if (speedAttr != null) {
            BlockPos playerPos = player.blockPosition();
            boolean onPath = (level.getBlockState(playerPos).is(Blocks.DIRT_PATH)
                    || level.getBlockState(playerPos).is(ModBlocks.CRYSTAL_DIRT_PATH.get()))
                    && ResonantPathData.get(level).isResonantPath(playerPos);
            if (!onPath) {
                BlockPos below = playerPos.below();
                onPath = (level.getBlockState(below).is(Blocks.DIRT_PATH)
                        || level.getBlockState(below).is(ModBlocks.CRYSTAL_DIRT_PATH.get()))
                        && ResonantPathData.get(level).isResonantPath(below);
            }
            UUID pid = player.getUUID();
            float current = pathSpeedBonus.getOrDefault(pid, 0.0F);
            if (onPath) {
                current = Math.min(current + PATH_SPEED_INC, MAX_PATH_SPEED);
                level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 0.1, player.getZ(),
                        1, 0.2, 0.0, 0.2, 0.01);
                speedAttr.removeModifier(PATH_SPEED_ID);
                speedAttr.addTransientModifier(new AttributeModifier(PATH_SPEED_ID, current, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else {
                current = Math.max(current - PATH_SPEED_DEC, 0.0F);
                if (current <= 0.0F) {
                    speedAttr.removeModifier(PATH_SPEED_ID);
                    pathSpeedBonus.remove(pid);
                } else {
                    speedAttr.removeModifier(PATH_SPEED_ID);
                    speedAttr.addTransientModifier(new AttributeModifier(PATH_SPEED_ID, current, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
            pathSpeedBonus.put(pid, current);
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

    // --- Resonance kill effects: Vibration Scar (50%) + Amethyst Bloom (10%) ---
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) return;
        if (!recentlyResonant.remove(dead.getId()) && !dead.hasEffect(ModEffects.RESONANCE)) return;

        // A player killing a resonating mob has a 1% chance to draw a Stalker.
        if (event.getSource().getEntity() instanceof Player && level.getRandom().nextFloat() < 0.01F) {
            trySpawnStalker(level, dead.blockPosition());
        }

        // Vibration Scar: 50% chance on Resonance kill
        if (level.getRandom().nextFloat() < 0.50F) {
            BlockPos scarPos = dead.blockPosition();
            boolean wraith = VibrationScars.add(level, scarPos);
            if (wraith) {
                java.util.List<BlockPos> scarPositions = VibrationScars.clearNear(level, scarPos, 8.0);
                playWraithEmergence(level, scarPos, scarPositions);
            }
        }

        // Amethyst Bloom: 10% chance
        if (level.getRandom().nextFloat() >= 0.10F) return;

        // Bloom: convert nearby stone/deepslate into amethyst, with a rare budding core
        BlockPos deathPos = dead.blockPosition();
        int planted = 0;
        boolean corePlaced = false;
        for (int attempt = 0; attempt < 40 && planted < 4; attempt++) {
            BlockPos target = deathPos.offset(
                    level.getRandom().nextInt(5) - 2,
                    level.getRandom().nextInt(3) - 1,
                    level.getRandom().nextInt(5) - 2);
            BlockState ground = level.getBlockState(target);
            boolean stoneLike = ground.is(BlockTags.BASE_STONE_OVERWORLD) || ground.is(Blocks.DEEPSLATE);
            if (!stoneLike) continue;

            if (!corePlaced && level.getRandom().nextFloat() < 0.20F) {
                level.setBlockAndUpdate(target, Blocks.BUDDING_AMETHYST.defaultBlockState());
                corePlaced = true;
            } else {
                level.setBlockAndUpdate(target, Blocks.AMETHYST_BLOCK.defaultBlockState());
            }
            planted++;
        }

        if (planted > 0) {
            level.playSound(null, deathPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 0.8F);
            level.playSound(null, deathPos, ModSounds.RESONANCE_PULSE.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
            level.sendParticles(ParticleTypes.END_ROD,
                    deathPos.getX() + 0.5, deathPos.getY() + 1.0, deathPos.getZ() + 0.5,
                    25, 3.0, 1.5, 3.0, 0.05);
        }
    }

    // --- Vibration Scars: battlefields shimmer for a while after resonant combat ---
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        tickEmergences(level);

        // Keep persistent tool markers bounded in long-running worlds.
        if (level.getGameTime() % 200 == 0) {
            ResonantPathData.get(level).pruneInvalid(level);
            HarmonizedFarmlandData.get(level).pruneInvalid(level);
        }

        Map<Long, Integer> scars = VibrationScars.tick(level);
        if (scars.isEmpty()) return;

        var crackDust = new net.minecraft.core.particles.DustParticleOptions(0x3F256B, 0.7F);
        var glowDust = new net.minecraft.core.particles.DustParticleOptions(0x7A5BB5, 0.5F);
        var brightDust = new net.minecraft.core.particles.DustParticleOptions(0xA678F1, 1.0F);
        java.util.List<long[]> scarPairs = new java.util.ArrayList<>();

        // Calculate neighborhood counts and drawable pairs together. The old
        // path rescanned the complete scar map twice for every scar.
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
            double x = pos.getX() + 0.5, z = pos.getZ() + 0.5;
            int nearby = nearbyCounts[scarIndex];
            float intensity = Math.min(1.0F, freshness + nearby * 0.1F);

            // Scar point — dark crack mark on the ground with faint glow at edges
            if (level.getRandom().nextFloat() < 0.2F * intensity) {
                level.sendParticles(crackDust, x, groundY + 0.02, z, 3, 0.15, 0.0, 0.15, 0.0);
                level.sendParticles(glowDust, x, groundY + 0.03, z, 1, 0.2, 0.0, 0.2, 0.0);
            }

            // Cluster escalation — soul fire seeping from cracks
            if (nearby >= 3 && level.getRandom().nextFloat() < 0.06F * intensity) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, groundY + 0.02, z, 1, 0.1, 0.0, 0.1, 0.001);
                level.sendParticles(ParticleTypes.SMOKE, x, groundY + 0.05, z, 1, 0.15, 0.02, 0.15, 0.002);
            }

            // Heavy cluster — cracks pulsing with light + sound
            if (nearby >= 6 && level.getRandom().nextFloat() < 0.04F * intensity) {
                level.sendParticles(brightDust, x, groundY + 0.03, z, 3, 0.2, 0.0, 0.2, 0.0);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, groundY + 0.05, z, 2, 0.3, 0.05, 0.3, 0.01);
                if (level.getRandom().nextFloat() < 0.1F) {
                    level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.AMBIENT, 0.4F, 0.5F + level.getRandom().nextFloat() * 0.3F);
                }
            }
        }

        // Draw crack lines between nearby scar points
        for (long[] pair : scarPairs) {
            if (level.getRandom().nextFloat() > 0.12F) continue;
            BlockPos a = BlockPos.of(pair[0]);
            BlockPos b = BlockPos.of(pair[1]);
            double ax = a.getX() + 0.5, az = a.getZ() + 0.5;
            double bx = b.getX() + 0.5, bz = b.getZ() + 0.5;
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

    // --- Boots: reduce fall distance by 4 blocks ---
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.RESONANT_BOOTS.get())) return;

        double original = event.getDistance();
        double reduced = Math.max(original - 2.0, 0.0);
        event.setDistance(reduced);
        if (original > 3.0 && reduced <= 3.0) {
            ServerLevel level = (ServerLevel) player.level();
            level.playSound(null, player.blockPosition(), ModSounds.RESONANCE_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.4F);
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(), player.getZ(),
                    10, 0.5, 0.1, 0.5, 0.05);
        }
    }

    // --- Totem: apply Resonance III in 8-block area on use ---
    @SubscribeEvent
    public static void onTotemUse(LivingUseTotemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getTotem().is(ModItems.RESONANT_TOTEM.get())) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB area = player.getBoundingBox().inflate(8.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt() * 2, 2), player);
        }
        level.playSound(null, player.blockPosition(), ModSounds.RESONANCE_PULSE.get(), SoundSource.PLAYERS, 1.5F, 0.6F);
        double cx = player.getX(), cy = player.getY() + 1.0, cz = player.getZ();
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
        for (int i = 0; i < 30; i++) {
            double dx = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dy = level.getRandom().nextDouble() * 2.0 - 1.0;
            double dz = level.getRandom().nextDouble() * 2.0 - 1.0;
            Vec3 dir = new Vec3(dx, dy, dz).normalize();
            level.sendParticles(shard, cx, cy, cz, 0, dir.x, dir.y, dir.z, 0.6);
        }
    }

    // --- Pickaxe: 10% chance to chain-mine adjacent identical blocks ---
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) player.level();
        ResonantPathData.get(level).remove(event.getPos());
        HarmonizedFarmlandData.get(level).remove(event.getPos());
        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.RESONANT_PICKAXE.get())) return;

        if (level.getRandom().nextFloat() >= 0.15F) return;

        BlockState broken = event.getState();
        BlockPos center = event.getPos();
        BlockPos playerFeet = player.blockPosition();
        int chainCount = 0;
        for (BlockPos offset : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (offset.equals(center) || offset.equals(playerFeet)) continue;
            if (level.getBlockState(offset).is(broken.getBlock())) {
                level.destroyBlock(offset, true, player);
                chainCount++;
                if (chainCount >= 3) break;
            }
        }
        if (chainCount > 0) {
            level.playSound(null, center, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);
            level.playSound(null, center, ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 0.6F, 1.3F);
            level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    8, 0.5, 0.5, 0.5, 0.05);
        }
    }

    // --- Hoe: crops grow 20% faster on harmonized farmland ---
    @SubscribeEvent
    public static void onCropGrow(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos below = event.getPos().below();

        HarmonizedFarmlandData data = HarmonizedFarmlandData.get(level);
        if (!data.isHarmonized(below)) return;

        BlockState farmland = level.getBlockState(below);
        if (!farmland.is(Blocks.FARMLAND) && !farmland.is(ModBlocks.CRYSTAL_FARMLAND.get())) {
            data.remove(below);
            return;
        }

        if (level.getRandom().nextFloat() < 0.20F) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
        if (level.getRandom().nextFloat() < 0.40F) {
            level.sendParticles(ParticleTypes.END_ROD,
                    event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                    5, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                    ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 0.8F, 1.4F);
        }
    }

    // --- Mount/wolf armor: passive auras ---
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        // Wolf armor: attacks from the wolf apply Resonance, passive aura every 2s
        if (event.getEntity() instanceof Wolf wolf) {
            ItemStack bodyArmor = wolf.getItemBySlot(EquipmentSlot.BODY);
            if (!bodyArmor.is(ModItems.RESONANT_WOLF_ARMOR.get())) return;
            if (wolf.tickCount % 40 != 0) return;

            AABB area = wolf.getBoundingBox().inflate(5.0);
            boolean hit = false;
            for (Monster mob : level.getEntitiesOfClass(Monster.class, area)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0));
                hit = true;
            }
            if (hit) {
                level.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(),
                        ModSounds.RESONANCE_CHIME.get(), SoundSource.NEUTRAL, 0.5F, 1.4F);
                level.sendParticles(ParticleTypes.END_ROD, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                        8, 1.0, 0.3, 1.0, 0.02);
            }
        }

        // Horse armor
        if (event.getEntity() instanceof AbstractHorse horse) {
            ItemStack bodyArmor = horse.getItemBySlot(EquipmentSlot.BODY);
            if (!bodyArmor.is(ModItems.RESONANT_HORSE_ARMOR.get())) return;
            if (horse.tickCount % 40 != 0) return;

            AABB area = horse.getBoundingBox().inflate(8.0);
            boolean hit = false;
            for (LivingEntity mob : level.getEntitiesOfClass(Monster.class, area)) {
                mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0));
                hit = true;
            }
            if (hit) {
                level.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                        ModSounds.RESONANCE_CHIME.get(), SoundSource.NEUTRAL, 0.6F, 1.2F);
                level.sendParticles(ParticleTypes.END_ROD, horse.getX(), horse.getY() + 1.0, horse.getZ(),
                        12, 1.5, 0.5, 1.5, 0.02);
            }
        }

        // Nautilus armor
        if (event.getEntity() instanceof AbstractNautilus nautilus) {
            ItemStack bodyArmor = nautilus.getItemBySlot(EquipmentSlot.BODY);
            if (!bodyArmor.is(ModItems.RESONANT_NAUTILUS_ARMOR.get())) return;
            if (nautilus.tickCount % 40 != 0) return;

            // Give rider Conduit Power
            if (nautilus.isInWater()) {
                for (var passenger : nautilus.getPassengers()) {
                    if (passenger instanceof LivingEntity rider) {
                        rider.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 60, 0, true, false, true));
                    }
                }
            }

            // Apply Resonance to nearby hostile mobs in water
            AABB area = nautilus.getBoundingBox().inflate(6.0);
            boolean hit = false;
            for (Monster mob : level.getEntitiesOfClass(Monster.class, area)) {
                if (mob.isInWater()) {
                    mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt(), 0));
                    hit = true;
                }
            }
            if (hit) {
                level.playSound(null, nautilus.getX(), nautilus.getY(), nautilus.getZ(),
                        ModSounds.RESONANCE_CHIME.get(), SoundSource.NEUTRAL, 0.6F, 1.0F);
                level.sendParticles(ParticleTypes.END_ROD, nautilus.getX(), nautilus.getY() + 0.5, nautilus.getZ(),
                        8, 1.0, 0.3, 1.0, 0.02);
            }
        }
    }

    // --- Sculk dampening: full Resonant armor suppresses vibrations ---
    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof Player player && hasFullResonantArmor(player)) {
            event.setCanceled(true);
        }
    }

    // --- Sculk weaponization: right-click sculk sensor with Resonant weapon to emit Resonance pulse ---
    @SubscribeEvent
    public static void onRightClickBlockSculk(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof SculkSensorBlock)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.RESONANT_SWORD.get()) && !held.is(ModItems.RESONANT_AXE.get())
                && !held.is(ModItems.RESONANT_SPEAR.get())) return;

        BlockPos pos = event.getPos();
        AABB area = new AABB(pos).inflate(8.0);
        boolean hit = false;
        for (Monster mob : level.getEntitiesOfClass(Monster.class, area)) {
            mob.addEffect(new MobEffectInstance(ModEffects.RESONANCE, Config.RESONANCE_DURATION.getAsInt() * 2, 1), player);
            hit = true;
        }
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, 0.6F);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.RESONANCE_CHIME.get(), SoundSource.BLOCKS, 1.2F, 0.8F);
        level.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                20, 2.0, 1.0, 2.0, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                15, 2.0, 1.0, 2.0, 0.05);
    }

    // --- Helpers ---

    private static boolean hasFullResonantArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.RESONANT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.RESONANT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.RESONANT_BOOTS.get());
    }

    private static boolean shieldOnCooldown(Player player) {
        return shieldCooldowns.containsKey(player.getUUID());
    }

    private static void startShieldCooldown(Player player) {
        shieldCooldowns.put(player.getUUID(), Config.HARMONIC_SHIELD_COOLDOWN.getAsInt());
    }

    private static double getScarGroundY(ServerLevel level, BlockPos pos) {
        BlockPos ground = pos.below();
        return level.getBlockState(ground).isSolidRender() ? ground.getY() + 1.0 : pos.getY();
    }

    private static boolean isOnResonantBlock(Player player) {
        BlockState below = player.level().getBlockState(player.blockPosition().below());
        return below.is(BlockTags.BASE_STONE_OVERWORLD)
                || below.is(Blocks.DEEPSLATE)
                || below.is(Blocks.AMETHYST_BLOCK)
                || below.is(Blocks.BUDDING_AMETHYST);
    }

    // --- Wraith Emergence ---

    private static class WraithEmergenceEvent {
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        final BlockPos center;
        final java.util.List<BlockPos> scarPositions;
        int ticksRemaining;
        int stage; // 0=glow, 1=crack, 2=spawn

        WraithEmergenceEvent(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim, BlockPos center, java.util.List<BlockPos> scars) {
            this.dimension = dim;
            this.center = center;
            this.scarPositions = scars;
            this.ticksRemaining = 0;
            this.stage = 0;
        }
    }

    private static void playWraithEmergence(ServerLevel level, BlockPos center, java.util.List<BlockPos> scarPositions) {
        pendingEmergences.add(new WraithEmergenceEvent(level.dimension(), center, scarPositions));
        // Stage 0 starts immediately on the next tick
    }

    private static void tickEmergences(ServerLevel level) {
        var it = pendingEmergences.iterator();
        while (it.hasNext()) {
            WraithEmergenceEvent e = it.next();
            if (!e.dimension.equals(level.dimension())) continue;

            if (e.ticksRemaining > 0) {
                e.ticksRemaining--;
                continue;
            }

            var brightPurple = new net.minecraft.core.particles.DustParticleOptions(0xA678F1, 1.5F);
            var hotWhite = new net.minecraft.core.particles.DustParticleOptions(0xE8D0FF, 2.0F);

            switch (e.stage) {
                case 0 -> {
                    // Stage 0: All scars glow bright — intense purple burst at each position
                    level.playSound(null, e.center, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 0.4F);
                    for (BlockPos scar : e.scarPositions) {
                        double x = scar.getX() + 0.5, z = scar.getZ() + 0.5;
                        double y = getScarGroundY(level, scar);
                        level.sendParticles(brightPurple, x, y + 0.05, z, 15, 0.2, 0.0, 0.2, 0.0);
                        level.sendParticles(hotWhite, x, y + 0.1, z, 5, 0.1, 0.0, 0.1, 0.0);
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.05, z, 4, 0.15, 0.0, 0.15, 0.005);
                    }
                    e.stage = 1;
                    e.ticksRemaining = 30; // 1.5 seconds
                }
                case 1 -> {
                    // Stage 1: Cracks burst open — explosion particles, block break effects
                    level.playSound(null, e.center, ModSounds.CRYSTAL_WRAITH_ARMOR_BREAK.get(), SoundSource.HOSTILE, 1.4F, 0.72F);
                    level.playSound(null, e.center, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 0.6F);
                    for (BlockPos scar : e.scarPositions) {
                        double x = scar.getX() + 0.5, z = scar.getZ() + 0.5;
                        double y = getScarGroundY(level, scar);
                        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.2, z, 1, 0.0, 0.0, 0.0, 0.0);
                        level.sendParticles(brightPurple, x, y + 0.5, z, 20, 0.3, 0.5, 0.3, 0.05);
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 0.3, z, 10, 0.2, 0.3, 0.2, 0.05);
                        // Upward crystal shards from each crack
                        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD);
                        level.sendParticles(shard, x, y + 0.2, z, 8, 0.2, 0.0, 0.2, 0.4);
                    }
                    // Draw converging lines from all scars to center
                    double cx = e.center.getX() + 0.5, cz = e.center.getZ() + 0.5;
                    double cy = getScarGroundY(level, e.center);
                    for (BlockPos scar : e.scarPositions) {
                        double sx = scar.getX() + 0.5, sz = scar.getZ() + 0.5;
                        double sy = getScarGroundY(level, scar);
                        int steps = (int) Math.ceil(Math.sqrt(scar.distSqr(e.center))) * 3;
                        for (int i = 0; i <= steps; i++) {
                            float t = i / (float) steps;
                            double lx = sx + (cx - sx) * t;
                            double ly = sy + (cy - sy) * t + 0.1;
                            double lz = sz + (cz - sz) * t;
                            level.sendParticles(hotWhite, lx, ly, lz, 1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                    e.stage = 2;
                    e.ticksRemaining = 20; // 1 second
                }
                case 2 -> {
                    // Stage 2: Crystal Wraith rises from the center
                    double cx = e.center.getX() + 0.5, cz = e.center.getZ() + 0.5;
                    double cy = getScarGroundY(level, e.center);
                    // The entity plays its custom emergence cue on its first tick,
                    // keeping fracture and spawn-egg Wraiths sonically consistent.
                    // Rising column of particles
                    for (int y = 0; y < 12; y++) {
                        level.sendParticles(brightPurple, cx, cy + y * 0.25, cz, 5, 0.3, 0.0, 0.3, 0.0);
                        level.sendParticles(ParticleTypes.END_ROD, cx, cy + y * 0.25, cz, 2, 0.15, 0.0, 0.15, 0.02);
                    }
                    // Burst ring at ground level
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10);
                        double rx = cx + Math.cos(angle) * 2.0;
                        double rz = cz + Math.sin(angle) * 2.0;
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, cy + 0.1, rz, 1, 0.0, 0.1, 0.0, 0.01);
                    }
                    // Spawn the Wraith
                    var wraith = com.resonance.registry.ModEntities.CRYSTAL_WRAITH.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                    if (wraith != null) {
                        wraith.setPos(cx, cy, cz);
                        wraith.setYRot(level.getRandom().nextFloat() * 360.0F);
                        level.addFreshEntity(wraith);
                    }
                    it.remove();
                }
            }
        }
    }
}
