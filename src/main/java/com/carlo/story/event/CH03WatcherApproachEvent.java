package com.carlo.story.event;

import com.carlo.entity.WatcherEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH03WatcherApproachEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_watcher_approach";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        String uuidStr = eventData.payload();
        WatcherEntity watcher = WatcherEncounterSystem.getWatcher((ServerWorld) player.getEntityWorld(), uuidStr);
        
        if (watcher != null) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.DisappearanceParticlePayload(watcher.getX(), watcher.getY(), watcher.getZ(), false));
			watcher.discard(); // Simple subtle disappearance
        }
        
                // Remove pending observation ticks for this player
        com.carlo.story.GlobalWorldState.getServerState(server).getScheduledEvents().removeIf(e -> 
            e.eventType().equals("ch03_observation_tick") && e.playerUuid().isPresent() && e.playerUuid().get().equals(player.getUuid())
        );
        
        // Approaching ends the current encounter early!
        EventScheduler.schedule(server, new ScheduledEvent("ch03_enc_end_" + player.getUuidAsString(), "ch03_encounter_complete", server.getOverworld().getTime(), "ch03", Optional.of(player.getUuid()), uuidStr, false));
    }
}


