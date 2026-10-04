package com.carlo.story.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.carlo.network.BlackScreenPayload;
import com.carlo.network.CinematicTextPayload;

public class DeathCinematicStep3Event implements StoryEvent {
    @Override
    public String getEventType() { return "godeye_death_step_3"; }
    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        // Clear text
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new CinematicTextPayload(0));
        
        if (player.isDead()) {
            server.getPlayerManager().respawnPlayer(player, false, net.minecraft.entity.Entity.RemovalReason.KILLED);
            // Re-fetch player since respawn creates a new entity instance!
            player = server.getPlayerManager().getPlayer(player.getUuid());
        }
        
        if (player != null) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new BlackScreenPayload(false));
        }
    }
}

