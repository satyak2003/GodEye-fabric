package com.carlo.story.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.carlo.network.BlackScreenPayload;
import com.carlo.network.CinematicTextPayload;
import com.carlo.story.system.CinematicLockSystem;

public class DeathCinematicStep3Event implements StoryEvent {
    @Override
    public String getEventType() { return "godeye_death_step_3"; }
    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        // Clear text
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new CinematicTextPayload(0));
        
        // Clear black screen
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new BlackScreenPayload(false));
        
        // Unlock controls safely
        CinematicLockSystem.unlockPlayer(player);
        
        System.out.println("[GodEye DEATH] Death recovery cinematic ended");
    }
}

