package com.resonance.event;

import com.resonance.Resonance;
import com.resonance.data.CrystalForestSpreadData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Resonance.MODID)
public class CrystalForestEvents {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            CrystalForestSpreadData.get(serverLevel).tick(serverLevel);
        }
    }
}
