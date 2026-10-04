package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.DialogueSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Deprecated // Obsolete. Replaced by CH03FinaleCinematicEvent. Retained only for legacy /godeye debug ch03_complete command.
public class CH03EndEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_end";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        DialogueSystem.send(player, "Unknown", "You saw it.");
        pState.setFlag("ch03_completed", true);
    }
}

