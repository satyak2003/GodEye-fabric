package com.carlo.story.event;

import com.carlo.entity.WatcherEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class CH03StartEncounterEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_start_encounter";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
                if (pState.getFlag("ch03_completed")) return;
        
        if (pState.getCh03EncounterCount() >= com.carlo.story.Chapter03.NORMAL_ENCOUNTER_LIMIT) {
            EventScheduler.schedule(server, new ScheduledEvent("ch03_finale_" + player.getUuidAsString(), "ch03_finale_start", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 5 : 20), "ch03", Optional.of(player.getUuid()), "", false));
            return;
        }

        // Reset relocation count for this new encounter
        pState.setFlag("ch03_has_relocated_once", false);
        pState.setCh03RelocationCount(0);
        
        WatcherEntity watcher = WatcherEncounterSystem.spawnWatcher(player, 12.0, 20.0);
        
        if (watcher != null) {
            String uuidStr = watcher.getUuidAsString();
            pState.setFlag("ch03_watcher_seen", false); // reset observation flag for new location
            
            // Start observation loop
            EventScheduler.schedule(server, new ScheduledEvent("ch03_obs_" + player.getUuidAsString(), "ch03_observation_tick", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 2 : 10), "ch03", Optional.of(player.getUuid()), uuidStr, false));
        } else {
            // Failed to spawn, try again soon
            EventScheduler.schedule(server, new ScheduledEvent("ch03_start_retry_" + player.getUuidAsString(), "ch03_start_encounter", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 10 : 100), "ch03", Optional.of(player.getUuid()), "", false));
        }
    }
}




