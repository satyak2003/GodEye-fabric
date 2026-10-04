package com.carlo.story.event;

import com.carlo.entity.WatcherEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import com.carlo.story.system.watcher.WatcherObservationSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH03ObservationTickEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_observation_tick";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        String uuidStr = eventData.payload();
        WatcherEntity watcher = WatcherEncounterSystem.getWatcher((ServerWorld) player.getEntityWorld(), uuidStr);
        
                if (watcher == null || watcher.isRemoved()) {
            if (pState.getCh03EncounterCount() < com.carlo.story.Chapter03.NORMAL_ENCOUNTER_LIMIT) {
                EventScheduler.schedule(server, new ScheduledEvent("ch03_restart_" + player.getUuidAsString(), "ch03_start_encounter", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 10 : 100), "ch03", Optional.of(player.getUuid()), "", false));
            }
            return;
        }
        
        double distSq = player.squaredDistanceTo(watcher);
        
        if (distSq < 64.0) { // < 8 blocks
            // Player approached!
            EventScheduler.schedule(server, new ScheduledEvent("ch03_app_" + player.getUuidAsString(), "ch03_watcher_approach", server.getOverworld().getTime(), "ch03", Optional.of(player.getUuid()), uuidStr, false));
            return; // Loop ends here
        }
        
        boolean observed = WatcherObservationSystem.isObserving(player, watcher);
        long currentTick = server.getOverworld().getTime();
        
        if (observed) {
            if (!pState.getFlag("ch03_watcher_seen")) {
                pState.setFlag("ch03_watcher_seen", true);
            }
            // Always face player when observed
            double dx = player.getX() - watcher.getX();
            double dz = player.getZ() - watcher.getZ();
            float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            watcher.setYaw(yaw);
            watcher.setBodyYaw(yaw);
            watcher.setHeadYaw(yaw);
        } else {
            if (pState.getFlag("ch03_watcher_seen")) {
                if (pState.getCh03RelocationCount() < com.carlo.story.Chapter03.NORMAL_ENCOUNTER_RELOCATIONS) {
                    if (server.getOverworld().random.nextFloat() < 0.1f) {
                        EventScheduler.schedule(server, new ScheduledEvent("ch03_reloc_" + player.getUuidAsString(), "ch03_watcher_relocate", currentTick, "ch03", Optional.of(player.getUuid()), uuidStr, false));
                        return; // End loop until relocated
                    }
                } else {
                    if (server.getOverworld().random.nextFloat() < 0.1f) {
                        // Encounter completes naturally
                        EventScheduler.schedule(server, new ScheduledEvent("ch03_enc_end_" + player.getUuidAsString(), "ch03_encounter_complete", currentTick, "ch03", Optional.of(player.getUuid()), uuidStr, false));
                        return; // End loop
                    }
                }
            }
        }
        
        // Loop
        EventScheduler.schedule(server, new ScheduledEvent("ch03_obs_" + player.getUuidAsString(), "ch03_observation_tick", currentTick + (pState.getFlag("ch03_debug_fast") ? 2 : 10), "ch03", Optional.of(player.getUuid()), uuidStr, false));
    }
}


