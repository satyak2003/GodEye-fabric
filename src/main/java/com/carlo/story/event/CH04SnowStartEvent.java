package com.carlo.story.event;

import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH04SnowStartEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_snow_start";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed")) return;
        
        pState.setFlag("ch04_snow_active", true);
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        // Custom packet or command to force snow weather state just for this village area
        // In this implementation, the easiest way is to use a weather packet to the specific player.
        // Or if it's world level, just set it to raining/snowing (assuming biome can't be changed easily)
        // Wait, Plains biome normally rains, not snows. 
        // We will send a packet to the player to render snow instead of rain.
        // For now, we just set the server weather to rain (which renders as rain in plains).
        // A custom network payload would be better to force snow rendering, but the prompt says:
        // "Use the appropriate client/server weather mechanism or a controlled CH04 weather state so that the player sees continuous snowfall despite the Plains biome."
        // We can just rely on the tick function of Chapter04 to enforce snow rendering via a packet if needed.
        // Actually, let's just make sure it's raining on the server.
        world.setWeather(0, 24000, true, false);
        ServerPlayNetworking.send(player, new com.carlo.network.CH04SnowPayload(true)); 
        
        long currentTime = world.getTime();
        long pFreezeDelay = 20 * (60 + world.random.nextInt(31)); // 1 to 1.5 mins after snow starts
        long nightDelay = 20 * (3 * 60); // 3 mins after snow starts (so 5 mins total)
        
        if (pState.getFlag("ch04_debug_fast")) {
            pFreezeDelay = 20 * 10;
            nightDelay = 20 * 20;
        }
        
        EventScheduler.schedule(server, new ScheduledEvent("ch04_pl_freeze_" + player.getUuidAsString(), "ch04_player_freeze", currentTime + pFreezeDelay, "ch04", Optional.of(player.getUuid()), "", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch04_night_" + player.getUuidAsString(), "ch04_night_start", currentTime + nightDelay, "ch04", Optional.of(player.getUuid()), "", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch04_frost_" + player.getUuidAsString(), "ch04_frost_spawn", currentTime + pFreezeDelay + 100, "ch04", Optional.of(player.getUuid()), "", false));
    }
}

