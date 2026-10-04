package com.carlo.story.event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface StoryEvent {
    String getEventType();
    void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player);
}
