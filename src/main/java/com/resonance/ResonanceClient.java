package com.resonance;

import com.resonance.client.ClientEvents;
import net.fabricmc.api.ClientModInitializer;

public final class ResonanceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEvents.register();
    }
}
