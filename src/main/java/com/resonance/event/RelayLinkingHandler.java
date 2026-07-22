package com.resonance.event;

import com.resonance.block.entity.FrequencyRelayBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RelayLinkingHandler {
    private static final Map<UUID, BlockPos> pendingLinks = new HashMap<>();

    public static void handleInteraction(Player player, BlockPos pos, Level level) {
        UUID id = player.getUUID();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (pendingLinks.containsKey(id)) {
            BlockPos firstPos = pendingLinks.remove(id);
            if (firstPos.equals(pos)) {
                serverPlayer.sendSystemMessage(Component.literal("Linking cancelled.").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (level.getBlockEntity(firstPos) instanceof FrequencyRelayBlockEntity relayA
                    && level.getBlockEntity(pos) instanceof FrequencyRelayBlockEntity relayB) {
                relayA.setLinkedPos(pos);
                relayB.setLinkedPos(firstPos);
                serverPlayer.sendSystemMessage(Component.literal("Relays linked!").withStyle(ChatFormatting.GREEN), true);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("Link failed - relay missing.").withStyle(ChatFormatting.RED), true);
            }
        } else {
            pendingLinks.put(id, pos);
            serverPlayer.sendSystemMessage(Component.literal("Relay selected. Right-click another to link.").withStyle(ChatFormatting.GOLD), true);
        }
    }
}
