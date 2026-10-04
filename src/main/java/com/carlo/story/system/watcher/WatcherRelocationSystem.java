package com.carlo.story.system.watcher;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.HitResult;

public class WatcherRelocationSystem {
    public static BlockPos findRelocationPos(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        
        for (int i = 0; i < 20; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double dist = 12.0 + world.random.nextDouble() * 8.0; // 12-20 blocks
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            
            BlockPos testPos = new BlockPos((int)x, (int)player.getY(), (int)z);
            int[] yOffsets = {0, 1, -1, 2, -2, 3, -3};
            for (int dy : yOffsets) {
                BlockPos pos = testPos.up(dy);
                if (world.getBlockState(pos.down()).isSolid() && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) {
                    if (!world.getFluidState(pos).isEmpty()) continue; // No water/lava
                    
                    Vec3d targetEye = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.62, pos.getZ() + 0.5);
                    
                    // Check if it's currently inside the player's FOV and visible
                    Vec3d toTarget = targetEye.subtract(eyePos).normalize();
                    double dot = look.dotProduct(toTarget);
                    
                    boolean visible = false;
                    if (dot > 0.3) {
                        RaycastContext context = new RaycastContext(
                            eyePos, targetEye,
                            RaycastContext.ShapeType.VISUAL,
                            RaycastContext.FluidHandling.NONE,
                            player
                        );
                        HitResult hit = world.raycast(context);
                        if (hit.getType() == HitResult.Type.MISS) {
                            visible = true; // Player can see it right now!
                        }
                    }
                    
                    if (!visible) {
                        return pos; // Good hidden spot!
                    }
                }
            }
        }
        return null;
    }
}
