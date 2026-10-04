package com.carlo.story.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.carlo.network.BlackScreenPayload;
import com.carlo.network.CinematicTextPayload;
import com.carlo.story.system.SoundHelper;
import com.carlo.Godeye;

public class DeathCinematicStep1Event implements StoryEvent {
    @Override
    public String getEventType() { return "godeye_death_step_1"; }
    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        // Turn screen black
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new BlackScreenPayload(true));
        
        // Wait 1 second before text
        EventScheduler.schedule(server, new ScheduledEvent("death_seq_2_" + player.getUuidAsString(), "godeye_death_step_2", player.getEntityWorld().getTime() + 20, "system", java.util.Optional.of(player.getUuid()), "", false));
    }
}
