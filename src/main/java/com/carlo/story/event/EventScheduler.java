package com.carlo.story.event;

import com.carlo.story.GlobalWorldState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public class EventScheduler {
    public static void tick(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        long currentTick = server.getWorld(World.OVERWORLD).getTime();

        List<ScheduledEvent> events = state.getScheduledEvents();
        if (events.isEmpty()) return;

        List<ScheduledEvent> toRemove = new ArrayList<>();
        boolean dirty = false;

        // Iterate over a copy to avoid ConcurrentModificationException if an event schedules another event
        for (ScheduledEvent scheduledEvent : new ArrayList<>(events)) {
            if (scheduledEvent.isCompleted()) {
                toRemove.add(scheduledEvent);
                dirty = true;
                continue;
            }

                        if (currentTick >= scheduledEvent.targetTick()) {
                StoryEvent logic = EventRegistry.get(scheduledEvent.eventType());
                if (logic != null) {
                    ServerPlayerEntity targetPlayer = null;
                    if (scheduledEvent.playerUuid().isPresent()) {
                        targetPlayer = server.getPlayerManager().getPlayer(scheduledEvent.playerUuid().get());
                        if (targetPlayer == null) {
                            continue; // Wait for player to come online
                        }
                    }
                    if (scheduledEvent.chapterId().equals("ch04")) {
                        System.out.println("[GodEye CH04] Event executed: " + scheduledEvent.eventType());
                    }
                    logic.execute(server, scheduledEvent, targetPlayer);
                } else {
                    System.out.println("[GodEye ERROR] Missing event logic for type: " + scheduledEvent.eventType());
                }

                toRemove.add(scheduledEvent);
                dirty = true;
            }
        }

        if (dirty) {
            // events list might have new events added during logic.execute(), so we remove processed ones
            events.removeAll(toRemove);
            state.markDirty();
        }
    }
    
        public static void schedule(MinecraftServer server, ScheduledEvent event) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        if (event.chapterId().equals("ch04")) {
            System.out.println("[GodEye CH04] Event scheduled: " + event.eventType() + " (tick " + event.targetTick() + ")");
        }
        state.getScheduledEvents().add(event);
        state.markDirty();
    }
}

