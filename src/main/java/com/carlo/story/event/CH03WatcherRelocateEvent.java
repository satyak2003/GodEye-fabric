package com.carlo.story.event;

import com.carlo.entity.WatcherEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import com.carlo.story.system.watcher.WatcherObservationSystem;
import com.carlo.story.system.watcher.WatcherRelocationSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.Optional;

public class CH03WatcherRelocateEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_watcher_relocate";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        String uuidStr = eventData.payload();
        WatcherEntity watcher = WatcherEncounterSystem.getWatcher((ServerWorld) player.getEntityWorld(), uuidStr);
        
        if (watcher == null || watcher.isRemoved()) return;
        
        // Double check observation right before teleport
        if (WatcherObservationSystem.isObserving(player, watcher)) {
            // Player looked back! Cancel relocation, resume tick
            EventScheduler.schedule(server, new ScheduledEvent("ch03_obs_" + player.getUuidAsString(), "ch03_observation_tick", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 2 : 10), "ch03", Optional.of(player.getUuid()), uuidStr, false));
            return;
        }
        
        BlockPos newPos = WatcherRelocationSystem.findRelocationPos(player);
        if (newPos != null) {
            watcher.setPosition(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
            
            double dx = player.getX() - watcher.getX();
            double dz = player.getZ() - watcher.getZ();
            float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            watcher.setYaw(yaw);
            watcher.setBodyYaw(yaw);
            watcher.setHeadYaw(yaw);
            
            pState.setFlag("ch03_watcher_seen", false);
            pState.setFlag("ch03_has_relocated_once", true);
            pState.setCh03RelocationCount(pState.getCh03RelocationCount() + 1);
        }
        
        // Resume tick
        EventScheduler.schedule(server, new ScheduledEvent("ch03_obs_" + player.getUuidAsString(), "ch03_observation_tick", server.getOverworld().getTime() + (pState.getFlag("ch03_debug_fast") ? 2 : 10), "ch03", Optional.of(player.getUuid()), uuidStr, false));
    }
}


