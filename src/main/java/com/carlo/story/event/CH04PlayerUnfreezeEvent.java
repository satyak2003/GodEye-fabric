package com.carlo.story.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class CH04PlayerUnfreezeEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_player_unfreeze";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        // Obsolete, left for registry compatibility
    }
}
