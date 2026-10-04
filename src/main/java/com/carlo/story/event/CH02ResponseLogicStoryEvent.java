package com.carlo.story.event;

import com.carlo.story.system.DialogueSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.carlo.story.PlayerStoryState;

public class CH02ResponseLogicStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch02_response_logic";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        DialogueSystem.send(player, "GodEye", "");
        
        // Mark CH02 as complete for this player!
        PlayerStoryState pState = PlayerStoryState.getState(player);
        pState.setFlag("ch02_completed", true);
    }
}
