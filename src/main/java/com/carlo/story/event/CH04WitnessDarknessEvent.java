package com.carlo.story.event;

import com.carlo.entity.WitnessEntity;
import com.carlo.network.CameraLockPayload;
import com.carlo.network.ControlLockPayload;
import com.carlo.network.CinematicLockPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;

public class CH04WitnessDarknessEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_witness_darkness";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        try {
            Entity entity = world.getEntity(UUID.fromString(eventData.payload()));
            if (entity instanceof WitnessEntity witness) {
                ServerPlayNetworking.send(player, new com.carlo.network.DisappearanceParticlePayload(witness.getX(), witness.getY(), witness.getZ(), true));
				witness.discard(); // disappear instantly
            }
        } catch(Exception ignored){}
        
        // Stop sound
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(net.minecraft.util.Identifier.of("godeye", "witness_encounter_1"), net.minecraft.sound.SoundCategory.HOSTILE));
        
        // Remove darkness
        player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
        
        // Restore controls
        ServerPlayNetworking.send(player, new CameraLockPayload(false, 0));
        ServerPlayNetworking.send(player, new ControlLockPayload(false));
        com.carlo.story.PlayerStoryState.getState(player).setFlag("cinematic_locked", false);
        ServerPlayNetworking.send(player, new CinematicLockPayload(false));
        
        // Start dialogue
        EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_1_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 40, "ch04", Optional.of(player.getUuid()), "1", false));
    }
}




