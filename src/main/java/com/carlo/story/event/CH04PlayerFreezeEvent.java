package com.carlo.story.event;

import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH04PlayerFreezeEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_player_freeze";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed") || pState.getFlag("ch04_witness_active")) return;
        
        // Only lock movement
        ServerPlayNetworking.send(player, new ControlLockPayload(true));
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        long duration = 20 * (5 + world.random.nextInt(3)); // 5-7 seconds
        
        EventScheduler.schedule(server, new ScheduledEvent("ch04_pl_unfreeze_" + player.getUuidAsString(), "ch04_player_unfreeze", world.getTime() + duration, "ch04", Optional.of(player.getUuid()), "", false));
        
        // Schedule next random freeze
        long nextDelay = 20 * (60 + world.random.nextInt(120)); // 1-3 minutes
        if (pState.getFlag("ch04_debug_fast")) nextDelay = 20 * 15;
        EventScheduler.schedule(server, new ScheduledEvent("ch04_pl_freeze_" + player.getUuidAsString(), "ch04_player_freeze", world.getTime() + nextDelay, "ch04", Optional.of(player.getUuid()), "", false));
    }
}
