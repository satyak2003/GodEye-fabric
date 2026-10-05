package com.carlo.story.event;

import com.carlo.entity.WitnessEntity;
import com.carlo.story.system.watcher.WatcherEncounterSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;

public class CH04WitnessTurnEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_witness_turn";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        try {
            Entity entity = world.getEntity(UUID.fromString(eventData.payload()));
            if (entity instanceof WitnessEntity witness) {
                // Face player
                double dx = player.getX() - witness.getX();
                double dz = player.getZ() - witness.getZ();
                float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                witness.setYaw(yaw);
                witness.setBodyYaw(yaw);
                witness.setHeadYaw(yaw);
                
                // Play eerie sound
                com.carlo.story.system.SoundHelper.playToPlayer(player, net.minecraft.sound.SoundEvents.ENTITY_WARDEN_HEARTBEAT, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
                
                // Start approaching
                EventScheduler.schedule(server, new ScheduledEvent("ch04_wit_app_" + player.getUuidAsString(), "ch04_witness_approach", world.getTime() + 20, "ch04", Optional.of(player.getUuid()), eventData.payload(), false));
            }
        } catch(Exception e) {
            System.err.println("[GodEye CH04 ERROR] Witness turn failed: " + e.getMessage());
        }
    }
}
