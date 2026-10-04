package com.carlo.story.system.watcher;

import com.carlo.Godeye;
import com.carlo.entity.WatcherEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;
import net.minecraft.entity.Entity;

public class WatcherEncounterSystem {
    public static WatcherEntity spawnWatcher(ServerPlayerEntity player, double minDistance, double maxDistance) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        BlockPos spawnPos = findWatcherSpawnPos(player, minDistance, maxDistance);
        if (spawnPos == null) return null;

        WatcherEntity watcher = new WatcherEntity(Godeye.GODEYE_WATCHER, world);
        watcher.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        watcher.setLifespan(-1);
        
        // Face the player initially
        double dx = player.getX() - watcher.getX();
        double dz = player.getZ() - watcher.getZ();
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        watcher.setYaw(yaw);
        watcher.setBodyYaw(yaw);
        watcher.setHeadYaw(yaw);

                        world.spawnEntity(watcher);
        world.playSound(null, watcher.getBlockPos(), Godeye.WATCHER_TP_EVENT, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        
        // Debug message
        double dist = Math.sqrt(player.squaredDistanceTo(watcher));
        player.sendMessage(net.minecraft.text.Text.literal(String.format("[GodEye Debug] Watcher spawned at X: %.2f Y: %.2f Z: %.2f | Distance: %.1f", watcher.getX(), watcher.getY(), watcher.getZ(), dist)), false);

        // 20-block clearing (radius 10) for blocks strictly above Watcher Y
        int r = 10;
        int wy = spawnPos.getY();
        for (int dx2 = -r; dx2 <= r; dx2++) {
            for (int dz2 = -r; dz2 <= r; dz2++) {
                if (dx2 * dx2 + dz2 * dz2 <= r * r) {
                    // Check up to roughly 30 blocks high to clear trees
                    for (int dy2 = 1; dy2 < 30; dy2++) {
                        BlockPos clearPos = spawnPos.add(dx2, dy2, dz2);
                        if (!world.getBlockState(clearPos).isAir()) {
                            world.setBlockState(clearPos, net.minecraft.block.Blocks.AIR.getDefaultState(), 2 | 16); // No drops, no neighbor updates if possible
                        }
                    }
                }
            }
        }
        
        return watcher;
    }

        public static void disappearWatcher(WatcherEntity watcher, ServerPlayerEntity player) {
        if (watcher == null || watcher.isRemoved()) return;
        ServerWorld world = (ServerWorld) watcher.getEntityWorld();
        
        // Play disappear sound
        world.playSound(null, watcher.getBlockPos(), Godeye.WATCHER_TP_EVENT, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        
        // Spawn supernatural particles
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, watcher.getX(), watcher.getY() + 1.0, watcher.getZ(), 20, 0.3, 1.0, 0.3, 0.05);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE, watcher.getX(), watcher.getY() + 1.0, watcher.getZ(), 15, 0.4, 1.0, 0.4, 0.02);
        }
        
        watcher.discard();
    }

    public static BlockPos findWatcherSpawnPos(ServerPlayerEntity player, double minDistance, double maxDistance) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Target precisely 10 blocks in front
        float yaw = player.getYaw();
        net.minecraft.util.math.Vec3d forward = net.minecraft.util.math.Vec3d.fromPolar(0, yaw).normalize();
        double targetX = player.getX() + forward.x * 10.0;
        double targetZ = player.getZ() + forward.z * 10.0;
        int py = (int) player.getY();
        
        // Search a small bounded area around the target, up to 4 blocks radius
        for (int r = 0; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r && r != 0) continue;
                    
                    int x = (int)targetX + dx;
                    int z = (int)targetZ + dz;
                    
                    // Verify it stays roughly in the front hemisphere
                    double relX = x - player.getX();
                    double relZ = z - player.getZ();
                    double dotProduct = (relX * forward.x) + (relZ * forward.z);
                    if (dotProduct < 5.0) continue; // Must be at least 5 blocks "forward"
                    
                    BlockPos testPos = new BlockPos(x, py, z);
                    int[] yOffsets = {0, 1, -1, 2, -2, 3, -3}; // Keep it at roughly player's Y level
                    for (int dy : yOffsets) {
                        BlockPos pos = testPos.up(dy);
                        if (world.getBlockState(pos.down()).isSolidBlock(world, pos.down()) && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) {
                            if (world.getFluidState(pos).isEmpty()) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static WatcherEntity getWatcher(ServerWorld world, String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = world.getEntity(uuid);
            if (entity instanceof WatcherEntity w) return w;
        } catch (Exception e) {}
        return null;
    }
}


