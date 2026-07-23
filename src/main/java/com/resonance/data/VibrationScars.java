package com.resonance.data;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Tracks positions where resonant combat recently happened. Scars are
 * transient by design — they fade after a while and are not persisted.
 */
public class VibrationScars {

    /** How long a fresh scar lasts, in ticks (60 seconds). */
    public static final int SCAR_DURATION = 1200;
    private static final int MAX_SCARS_PER_LEVEL = 512;
    private static final int WRAITH_THRESHOLD = 7;
    private static final double CLUSTER_RADIUS = 8.0;

    private static final Map<ResourceKey<Level>, Map<Long, Integer>> scars = new ConcurrentHashMap<>();

    /**
     * Adds a scar and returns true if the cluster around this position
     * has reached the Wraith spawn threshold (7+).
     */
    public static boolean add(Level level, BlockPos pos) {
        Map<Long, Integer> map = scars.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        BlockPos placement = findAvailablePosition(map, pos);
        if (map.size() >= MAX_SCARS_PER_LEVEL && !map.containsKey(placement.asLong())) return false;
        map.put(placement.asLong(), SCAR_DURATION);
        return countNear(level, placement, CLUSTER_RADIUS) >= WRAITH_THRESHOLD;
    }

    /**
     * Keep repeated kills in the same block from replacing the existing scar.
     * A two-block horizontal spread provides enough distinct positions for the
     * seven-scar Wraith threshold without moving the cluster away from combat.
     */
    private static BlockPos findAvailablePosition(Map<Long, Integer> map, BlockPos origin) {
        if (!map.containsKey(origin.asLong())) return origin;
        for (int radius = 1; radius <= 2; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                    BlockPos candidate = origin.offset(x, 0, z);
                    if (!map.containsKey(candidate.asLong())) return candidate;
                }
            }
        }
        return origin;
    }

    /** Decrements every scar's timer for this level, dropping expired ones. */
    public static Map<Long, Integer> tick(Level level) {
        Map<Long, Integer> map = scars.get(level.dimension());
        if (map == null) return Map.of();
        Iterator<Map.Entry<Long, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                it.remove();
            } else {
                entry.setValue(remaining);
            }
        }
        return map;
    }

    /** Number of active scars within the given radius of a position. */
    public static int countNear(Level level, BlockPos center, double radius) {
        Map<Long, Integer> map = scars.get(level.dimension());
        if (map == null) return 0;
        double radiusSq = radius * radius;
        int count = 0;
        for (Long packed : map.keySet()) {
            if (BlockPos.of(packed).distSqr(center) <= radiusSq) count++;
        }
        return count;
    }

    /** Remove all scars within the given radius and return their positions. */
    public static java.util.List<BlockPos> clearNear(Level level, BlockPos center, double radius) {
        Map<Long, Integer> map = scars.get(level.dimension());
        if (map == null) return java.util.List.of();
        double radiusSq = radius * radius;
        java.util.List<BlockPos> removed = new java.util.ArrayList<>();
        map.keySet().removeIf(packed -> {
            if (BlockPos.of(packed).distSqr(center) <= radiusSq) {
                removed.add(BlockPos.of(packed));
                return true;
            }
            return false;
        });
        return removed;
    }
}
