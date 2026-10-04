package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.DialogueSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class CH02MessageStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch02_message";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch02_responded")) return; // Abort if player responded

        String step = eventData.payload();
        long currentTick = server.getOverworld().getTime();
        String uuid = player.getUuidAsString();
        
        boolean debug = pState.getFlag("ch02_debug_fast");
        long waitTick = debug ? 40 : 400;

        switch (step) {
            case "start":
                DialogueSystem.send(player, "Unknown", "You do not belong here. RUN");
                pState.setFlag("ch02_response_window_active", true);
                EventScheduler.schedule(server, new ScheduledEvent("ch02_tout1_" + uuid, "ch02_message", currentTick + waitTick, "ch02", Optional.of(player.getUuid()), "timeout_1", false));
                break;
            case "timeout_1":
                pState.setFlag("ch02_response_window_active", false); 
                DialogueSystem.send(player, "Unknown", "CAN YOU NOT UNDERSTAND?");
                EventScheduler.schedule(server, new ScheduledEvent("ch02_e1_1_" + uuid, "ch02_message", currentTick + 30, "ch02", Optional.of(player.getUuid()), "esc_1_1", false));
                break;
            case "esc_1_1":
                DialogueSystem.send(player, "Unknown", "You need to get out of here..");
                EventScheduler.schedule(server, new ScheduledEvent("ch02_e1_2_" + uuid, "ch02_message", currentTick + 30, "ch02", Optional.of(player.getUuid()), "esc_1_2", false));
                break;
            case "esc_1_2":
                DialogueSystem.send(player, "Unknown", "RUN! RUN!");
                EventScheduler.schedule(server, new ScheduledEvent("ch02_e1_3_" + uuid, "ch02_message", currentTick + 40, "ch02", Optional.of(player.getUuid()), "esc_1_3", false));
                break;
            case "esc_1_3":
                DialogueSystem.send(player, "Unknown", "say something! can you understand!?");
                pState.setFlag("ch02_response_window_active", true);
                EventScheduler.schedule(server, new ScheduledEvent("ch02_tout2_" + uuid, "ch02_message", currentTick + waitTick, "ch02", Optional.of(player.getUuid()), "timeout_2", false));
                break;
            case "timeout_2":
                pState.setFlag("ch02_response_window_active", false);
                DialogueSystem.send(player, "GodEye", "");
                EventScheduler.schedule(server, new ScheduledEvent("ch02_jump_" + uuid, "ch02_jumpscare", currentTick + 20, "ch02", Optional.of(player.getUuid()), "", false));
                break;
        }
    }
}
