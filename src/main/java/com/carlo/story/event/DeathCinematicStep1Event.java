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
        
        if (player.isDead()) {
            server.getPlayerManager().respawnPlayer(player, false, net.minecraft.entity.Entity.RemovalReason.KILLED);
            player = server.getPlayerManager().getPlayer(player.getUuid());
            System.out.println("[GodEye DEATH] Player respawned");
        }
        
        // Wait briefly after respawn to let client catch up before applying cinematic locks/visuals
        EventScheduler.schedule(server, new ScheduledEvent("death_seq_2_" + player.getUuidAsString(), "godeye_death_step_2", player.getEntityWorld().getTime() + 10, "system", java.util.Optional.of(player.getUuid()), "", false));
    }
}
