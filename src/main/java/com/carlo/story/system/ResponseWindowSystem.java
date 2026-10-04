package com.carlo.story.system;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.event.EventScheduler;
import com.carlo.story.event.ScheduledEvent;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import java.util.Optional;

public class ResponseWindowSystem {
    public static void initialize() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            PlayerStoryState pState = PlayerStoryState.getState(sender);
            
            // Check if player has an active response window
            if (pState.getFlag("ch02_response_window_active")) {
                pState.setFlag("ch02_response_window_active", false);
                pState.setFlag("ch02_responded", true);
                
                // Immediately trigger response logic
                EventScheduler.schedule(((net.minecraft.server.world.ServerWorld)sender.getEntityWorld()).getServer(), new ScheduledEvent(
                    "ch02_response_" + sender.getUuidAsString(),
                    "ch02_response_logic",
                    ((net.minecraft.server.world.ServerWorld)sender.getEntityWorld()).getServer().getOverworld().getTime(),
                    "ch02",
                    Optional.of(sender.getUuid()),
                    "", false
                ));
            }
            return true; // Let the message send naturally
        });
    }
}

