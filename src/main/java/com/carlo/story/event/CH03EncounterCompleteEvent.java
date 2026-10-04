package com.carlo.story.event;

import com.carlo.entity.WatcherEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH03EncounterCompleteEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_encounter_complete";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        String uuidStr = eventData.payload();
        WatcherEntity watcher = WatcherEncounterSystem.getWatcher((ServerWorld) player.getEntityWorld(), uuidStr);
        if (watcher != null) {
            WatcherEncounterSystem.disappearWatcher(watcher, player);
        }
        
                // Remove pending observation ticks for this player
        com.carlo.story.GlobalWorldState.getServerState(server).getScheduledEvents().removeIf(e -> 
            e.eventType().equals("ch03_observation_tick") && e.playerUuid().isPresent() && e.playerUuid().get().equals(player.getUuid())
        );

        // Increment encounter count
        pState.setCh03EncounterCount(pState.getCh03EncounterCount() + 1);
        
        if (pState.getCh03EncounterCount() >= com.carlo.story.Chapter03.NORMAL_ENCOUNTER_LIMIT) {
            // Stop normal system, schedule finale
            EventScheduler.schedule(server, new ScheduledEvent("ch03_finale_" + player.getUuidAsString(), "ch03_finale_start", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 10 : 60), "ch03", Optional.of(player.getUuid()), "", false));
        } else {
            // Schedule next normal encounter
            EventScheduler.schedule(server, new ScheduledEvent("ch03_start_retry_" + player.getUuidAsString(), "ch03_start_encounter", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 20 : 200), "ch03", Optional.of(player.getUuid()), "", false));
        }
    }
}



