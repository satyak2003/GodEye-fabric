package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH04NightStartEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_night_start";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed")) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.setTimeOfDay(18000); // Night
        world.setWeather(0, 24000, true, false); // Ensure snow continues
        
        long delay = 20 * (20 + world.random.nextInt(11)); // 20-30 seconds
        if (pState.getFlag("ch04_debug_fast")) delay = 20 * 5;
        
        EventScheduler.schedule(server, new ScheduledEvent("ch04_witness_" + player.getUuidAsString(), "ch04_witness_start", world.getTime() + delay, "ch04", Optional.of(player.getUuid()), "", false));
    }
}
