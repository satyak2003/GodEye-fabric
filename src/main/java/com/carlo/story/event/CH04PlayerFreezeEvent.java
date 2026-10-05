package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
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
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Duration: 12-20 seconds -> 240-400 ticks.
        // Minecraft decays frozenTicks by 1 tick if the entity is just standing, but wait, usually it decreases by 2 if not in powder snow!
        // In 1.21.11, the decay is 2 per tick. So to last 12-20 seconds (240-400 ticks), we need to set frozen ticks to (12-20) * 20 * 2 = 480 to 800 ticks.
        // The max is 140 before damage starts. The scale starts rendering when it goes above 0, fully frosted at 140.
        int seconds = 12 + world.random.nextInt(9); // 12 to 20 seconds
        int frozenTicksTarget = seconds * 20 * 2;
        player.setFrozenTicks(frozenTicksTarget);
        
        // Schedule next random freeze
        long nextDelay = 20 * (60 + world.random.nextInt(120)); // 1-3 minutes
        if (pState.getFlag("ch04_debug_fast")) nextDelay = 20 * 15;
        EventScheduler.schedule(server, new ScheduledEvent("ch04_pl_freeze_" + player.getUuidAsString(), "ch04_player_freeze", world.getTime() + nextDelay, "ch04", Optional.of(player.getUuid()), "", false));
    }
}
