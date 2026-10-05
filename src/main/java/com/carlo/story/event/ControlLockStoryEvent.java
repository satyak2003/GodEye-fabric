package com.carlo.story.event;

import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;
import java.util.UUID;

public class ControlLockStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "control_lock";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        boolean lock = true;
        if (!eventData.payload().isEmpty()) {
            lock = Boolean.parseBoolean(eventData.payload());
        }

        if (player != null) {
            applyLock(player, lock);
        } else {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                applyLock(p, lock);
            }
        }
    }

    private void applyLock(ServerPlayerEntity player, boolean lock) {
        if (lock) {
            com.carlo.story.system.CinematicLockSystem.applyLock(player, true, false, -1);
        } else {
            com.carlo.story.system.CinematicLockSystem.unlockPlayer(player);
        }
    }
    
    public static void scheduleLock(MinecraftServer server, String chapterId, Optional<UUID> target, long targetTick, boolean lock) {
        String payload = Boolean.toString(lock);
        EventScheduler.schedule(server, new ScheduledEvent("lock_" + System.currentTimeMillis(), "control_lock", targetTick, chapterId, target, payload, false));
    }
}

