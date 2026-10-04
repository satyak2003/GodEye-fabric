package com.carlo.story.event;

import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class CH04PlayerUnfreezeEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_player_unfreeze";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_witness_active")) return;
        
        ServerPlayNetworking.send(player, new ControlLockPayload(false));
    }
}
