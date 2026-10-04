package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.WitnessEntity;
import com.carlo.network.CameraLockPayload;
import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public class CH04WitnessStartEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_witness_start";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed")) return;
        
        pState.setFlag("ch04_witness_active", true);
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // 1. Teleport player to village center safely
        BlockPos center = pState.getCh04VillageCenter();
        if (center != null) {
            BlockPos safe = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, center.getX(), center.getZ(), 5);
            if (safe == null) safe = center;
            // Face a fixed direction, say Yaw 0
            player.teleport(world, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, java.util.Set.of(), 0f, 0f, true);
        }
        
        // Lock player fully
        ServerPlayNetworking.send(player, new ControlLockPayload(true));
        
        // Schedule the actual spawn in a couple ticks to let the client settle
        EventScheduler.schedule(server, new ScheduledEvent("ch04_wit_spawn_" + player.getUuidAsString(), "ch04_witness_spawn", world.getTime() + 40, "ch04", Optional.of(player.getUuid()), "", false));
    }
}
