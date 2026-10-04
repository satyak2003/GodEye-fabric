package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.FrostEntity;
import com.carlo.story.PlayerStoryState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public class CH04FrostSpawnEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_frost_spawn";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed") || pState.getFlag("ch04_witness_active")) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Spawn 4-7 blocks in front of the player
        float yaw = player.getYaw();
        double dist = 4.0 + world.random.nextDouble() * 3.0;
        Vec3d offset = Vec3d.fromPolar(0, yaw).multiply(dist);
        double targetX = player.getX() + offset.x;
        double targetZ = player.getZ() + offset.z;
        
        BlockPos safePos = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, (int)targetX, (int)targetZ, 3);
        if (safePos != null) {
            FrostEntity frost = new FrostEntity(Godeye.FROST, world);
            frost.refreshPositionAndAngles(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, yaw - 180f, 0f);
            
            // Check line of sight (center, head, foot) to ensure it's not behind a wall
            Vec3d eyePos = player.getEyePos();
            boolean visible = false;
            for (double yOff : new double[]{0.5, 1.5, 2.5}) {
                Vec3d target = new Vec3d(safePos.getX() + 0.5, safePos.getY() + yOff, safePos.getZ() + 0.5);
                net.minecraft.world.RaycastContext context = new net.minecraft.world.RaycastContext(eyePos, target, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, player);
                if (world.raycast(context).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
                    visible = true;
                    break;
                }
            }
            
            if (visible) {
                world.spawnEntity(frost);
            }
        }
        
        // Schedule next one randomly
        long delay = 20 * (30 + world.random.nextInt(60)); // 30-90 seconds
        if (pState.getFlag("ch04_debug_fast")) delay = 20 * 10;
        EventScheduler.schedule(server, new ScheduledEvent("ch04_frost_" + player.getUuidAsString(), "ch04_frost_spawn", world.getTime() + delay, "ch04", Optional.of(player.getUuid()), "", false));
    }
}
