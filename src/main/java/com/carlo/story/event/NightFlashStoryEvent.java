package com.carlo.story.event;

import com.carlo.network.NightFlashPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class NightFlashStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "night_flash";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        // Default to 1 second (20 ticks) if payload is not set
        int duration = 20; 
        if (!eventData.payload().isEmpty()) {
            try { duration = Integer.parseInt(eventData.payload()); } catch (Exception ignored) {}
        }
        
        NightFlashPayload payloadPacket = new NightFlashPayload(duration);
        if (player != null) {
            ServerPlayNetworking.send(player, payloadPacket);
        } else {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(p, payloadPacket);
            }
        }
    }
}
