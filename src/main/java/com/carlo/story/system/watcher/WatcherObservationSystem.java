package com.carlo.story.system.watcher;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.HitResult;

public class WatcherObservationSystem {
    public static boolean isObserving(ServerPlayerEntity player, Entity watcher) {
        Vec3d eyePos = player.getEyePos();
        Vec3d watcherPos = watcher.getEyePos().subtract(0, 0.5, 0); // Aim at chest
        
        // 1. Distance check
        double distSq = eyePos.squaredDistanceTo(watcherPos);
        if (distSq > 6400) return false; // 80 blocks max
        
        // 2. FOV check
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d toWatcher = watcherPos.subtract(eyePos).normalize();
        double dot = look.dotProduct(toWatcher);
        if (dot < 0.5) return false; // Approx 60 deg cone

        // 3. Line of sight
        RaycastContext context = new RaycastContext(
            eyePos, watcherPos,
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.NONE,
            player
        );
        HitResult hit = player.getEntityWorld().raycast(context);
        return hit.getType() == HitResult.Type.MISS;
    }
}
