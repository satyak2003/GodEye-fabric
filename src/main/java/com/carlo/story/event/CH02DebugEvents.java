package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.CinematicLockSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH02DebugEvents {

    public static class CH02StartTriggerEvent implements StoryEvent {
        @Override public String getEventType() { return "ch02_start"; }
        @Override public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
            if (player != null) {
                new CH02MessageStoryEvent().execute(server, new ScheduledEvent("dbg", "ch02_message", 0, "ch02", Optional.of(player.getUuid()), "start", false), player);
            }
        }
    }

    public static class CH02TimeoutTriggerEvent implements StoryEvent {
        @Override public String getEventType() { return "ch02_timeout"; }
        @Override public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
            if (player != null) {
                new CH02MessageStoryEvent().execute(server, new ScheduledEvent("dbg", "ch02_message", 0, "ch02", Optional.of(player.getUuid()), "timeout_2", false), player);
            }
        }
    }

    public static class CH02ResetTriggerEvent implements StoryEvent {
        @Override public String getEventType() { return "ch02_reset"; }
        @Override public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
            if (player != null) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                for (com.carlo.entity.FrostEntity frost : world.getEntitiesByClass(com.carlo.entity.FrostEntity.class, player.getBoundingBox().expand(30), e -> true)) {
                    frost.discard();
                }
                CinematicLockSystem.unlockPlayer(player, "debug reset");
                PlayerStoryState state = PlayerStoryState.getState(player);
                state.setFlag("ch02_started", false);
                state.setFlag("ch02_responded", false);
                state.setFlag("ch02_completed", false);
                state.setFlag("ch02_response_window_active", false);
                state.setFlag("ch02_jumpscare_active", false);
            }
        }
    }
}
