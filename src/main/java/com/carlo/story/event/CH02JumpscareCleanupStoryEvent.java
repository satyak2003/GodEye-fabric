package com.carlo.story.event;

import com.carlo.network.CameraLockPayload;
import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.UUID;

public class CH02JumpscareCleanupStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch02_jumpscare_cleanup";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Remove Frost
        try {
            UUID frostUuid = UUID.fromString(eventData.payload());
            Entity frost = world.getEntity(frostUuid);
            if (frost != null) {
					frost.setNoGravity(false);
                frost.discard();
            }
        } catch (Exception ignored) {}
        
        // Unlock camera and controls
        ServerPlayNetworking.send(player, new CameraLockPayload(false, -1));
        ServerPlayNetworking.send(player, new ControlLockPayload(false));
        
        // Remove darkness
        player.removeStatusEffect(StatusEffects.DARKNESS);
        
        // Mark CH02 complete
        PlayerStoryState pState = PlayerStoryState.getState(player);
        pState.setFlag("ch02_completed", true);
    }
}

