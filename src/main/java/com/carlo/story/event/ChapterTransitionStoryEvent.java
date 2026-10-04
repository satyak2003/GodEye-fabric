package com.carlo.story.event;

import com.carlo.story.GlobalWorldState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;
import java.util.UUID;

public class ChapterTransitionStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "chapter_transition";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        String flag = eventData.payload();
        if (!flag.isEmpty()) {
            state.setFlag(flag, true);
        }
    }
    
    public static void scheduleTransitionFlag(MinecraftServer server, String chapterId, long targetTick, String flagName) {
        EventScheduler.schedule(server, new ScheduledEvent("transition_" + System.currentTimeMillis(), "chapter_transition", targetTick, chapterId, Optional.empty(), flagName, false));
    }
}
