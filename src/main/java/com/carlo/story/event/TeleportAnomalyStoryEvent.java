package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.PlayerPositionTracker;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import java.util.Optional;

public class TeleportAnomalyStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "teleport_anomaly";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Use tracker to find backward movement
        Vec3d oldestPos = PlayerPositionTracker.getOldestPosition(player);
        Vec3d currentPos = player.getEntityPos();
        
        // Vector from current to oldest (backward)
        Vec3d displacement = oldestPos.subtract(currentPos);
        
        // If player hasn't moved much, fall back to negative look vector
        if (displacement.lengthSquared() < 1.0) {
            displacement = Vec3d.fromPolar(0, player.getYaw()).multiply(-1.0);
        }
        
        // Normalize horizontally and multiply by ~10
        Vec3d horizontalDir = new Vec3d(displacement.x, 0, displacement.z).normalize();
        if (Double.isNaN(horizontalDir.x)) { // Fallback if perfectly zero
            horizontalDir = Vec3d.fromPolar(0, player.getYaw()).multiply(-1.0);
        }
        
        Vec3d targetBase = currentPos.add(horizontalDir.multiply(10.0));
        int targetX = (int) Math.floor(targetBase.x);
        int targetZ = (int) Math.floor(targetBase.z);
        
        // Load chunk safely natively
        Chunk chunk = world.getChunk(targetX >> 4, targetZ >> 4, ChunkStatus.FULL, true);
        if (chunk == null) return; // Failsafe
        
        int startY = (int) Math.floor(currentPos.y) + 5;
        int minY = (int) Math.floor(currentPos.y) - 10;
        
        BlockPos validPos = null;
        
        for (int y = startY; y >= minY; y--) {
            BlockPos testPos = new BlockPos(targetX, y, targetZ);
            BlockPos above1 = testPos.up();
            BlockPos above2 = testPos.up(2);
            
            BlockState foot = world.getBlockState(testPos);
            BlockState head1 = world.getBlockState(above1);
            BlockState head2 = world.getBlockState(above2);
            
            if (foot.isOpaqueFullCube() && head1.isAir() && head2.isAir()) {
                // Reject lava, fire, etc.
                if (!world.getBlockState(above1).getFluidState().isEmpty() || !world.getBlockState(above2).getFluidState().isEmpty()) {
                    continue;
                }
                validPos = above1;
                break;
            }
        }
        
        if (validPos != null) {
            // Teleport
            player.requestTeleport(validPos.getX() + 0.5, validPos.getY(), validPos.getZ() + 0.5);
            
            // Check remaining
            PlayerStoryState state = PlayerStoryState.getState(player);
            int remaining = state.getTeleportsRemaining();
            if (remaining > 0) {
                state.setTeleportsRemaining(remaining - 1);
                if (remaining - 1 == 0) {
                    state.setFlag("ch01_teleports_completed", true);
                }
            }
        } else {
            // Unsafe terrain, try again in 50 ticks to not lose the anomaly
            EventScheduler.schedule(server, new ScheduledEvent("teleport_retry_" + System.currentTimeMillis(), "teleport_anomaly", server.getOverworld().getTime() + 50, eventData.chapterId(), Optional.of(player.getUuid()), "", false));
        }
    }
}
