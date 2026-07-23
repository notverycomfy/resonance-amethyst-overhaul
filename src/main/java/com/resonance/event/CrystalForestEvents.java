package com.resonance.event;

import com.resonance.data.CrystalForestSpreadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class CrystalForestEvents {
    private CrystalForestEvents() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(level -> CrystalForestSpreadData.get(level).tick(level));
    }
}
