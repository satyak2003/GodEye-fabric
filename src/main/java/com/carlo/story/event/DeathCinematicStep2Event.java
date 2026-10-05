package com.carlo.story.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.carlo.network.BlackScreenPayload;
import com.carlo.network.CinematicTextPayload;
import com.carlo.story.system.SoundHelper;
import com.carlo.story.system.CinematicLockSystem;
import com.carlo.Godeye;

public class DeathCinematicStep2Event implements StoryEvent {
    @Override
    public String getEventType() { return "godeye_death_step_2"; }
    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        System.out.println("[GodEye DEATH] Death recovery cinematic started");
        
        // Lock controls
        CinematicLockSystem.applyLock(player, true, true, -1);
        
        // Turn screen black
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new BlackScreenPayload(true));
        
        // Text and sound
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new CinematicTextPayload(4)); // 4 = "Quitting is not an option"
        SoundHelper.playToPlayer(player, Godeye.GODEYE_DEATH_SCR_EVENT, net.minecraft.sound.SoundCategory.RECORDS, 1.0f, 1.0f);
        
        // Wait 4 seconds for sound/text, then finish
        EventScheduler.schedule(server, new ScheduledEvent("death_seq_3_" + player.getUuidAsString(), "godeye_death_step_3", player.getEntityWorld().getTime() + 80, "system", java.util.Optional.of(player.getUuid()), "", false));
    }
}

