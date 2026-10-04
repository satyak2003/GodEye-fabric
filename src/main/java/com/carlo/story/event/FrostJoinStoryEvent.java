package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.FrostEntity;
import com.carlo.story.PlayerStoryState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.Optional;

public class FrostJoinStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "frost_join";
    }

    public static Vec3d findSafeSpawn(ServerWorld world, ServerPlayerEntity player, double minDist, double maxDist, FrostEntity tempFrost) {
        java.util.Random rand = new java.util.Random();
        System.out.println("[Frost DEBUG] Starting spawn candidate search...");
        
        for (int i = 0; i < 60; i++) {
            double distance = minDist + rand.nextDouble() * (maxDist - minDist);
            
            // Prefer rear hemisphere/sides (Yaw + 180 +/- 90 degrees)
            float angle = player.getYaw() + 180.0f + (rand.nextFloat() - 0.5f) * 180.0f;
            Vec3d offset = Vec3d.fromPolar(0, angle).multiply(distance);
            double targetX = player.getX() + offset.x;
            double targetZ = player.getZ() + offset.z;
            
            int startY = (int) player.getY() + 15;
            int endY = (int) player.getY() - 15;
            
            for (int cy = startY; cy >= endY; cy--) {
                BlockPos pos = new BlockPos((int)Math.floor(targetX), cy, (int)Math.floor(targetZ));
                
                // --- FOV Safety Check ---
                Vec3d candidatePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                Vec3d toCandidate = candidatePos.subtract(player.getEyePos()).normalize();
                Vec3d lookVec = player.getRotationVec(1.0F);
                // If dot product > 0.3, it is somewhat in front of the player (within ~140 deg cone).
                if (lookVec.dotProduct(toCandidate) > 0.3) {
                    continue; // Reject candidate, visible!
                }
                
                BlockState below = world.getBlockState(pos.down());
                boolean belowSolid = below.isOpaque() && below.getFluidState().isEmpty();
                
                if (belowSolid) {
                    // Place tempFrost here to accurately check actual collision
                    tempFrost.setPosition(targetX, cy, targetZ);
                    if (world.isSpaceEmpty(tempFrost) && !world.containsFluid(tempFrost.getBoundingBox())) {
                        
                        // NEW RAYCAST LOGIC (LOS validation)
                        Vec3d eye = player.getEyePos();
                        Vec3d center = tempFrost.getBoundingBox().getCenter();
                        Vec3d head = new Vec3d(center.x, tempFrost.getBoundingBox().maxY - 0.2, center.z);
                        Vec3d foot = new Vec3d(center.x, tempFrost.getBoundingBox().minY + 0.2, center.z);
                        
                        net.minecraft.world.RaycastContext ctxCenter = new net.minecraft.world.RaycastContext(eye, center, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player);
                        net.minecraft.world.RaycastContext ctxHead = new net.minecraft.world.RaycastContext(eye, head, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player);
                        net.minecraft.world.RaycastContext ctxFoot = new net.minecraft.world.RaycastContext(eye, foot, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player);
                        
                        net.minecraft.util.hit.BlockHitResult hitCenter = world.raycast(ctxCenter);
                        net.minecraft.util.hit.BlockHitResult hitHead = world.raycast(ctxHead);
                        net.minecraft.util.hit.BlockHitResult hitFoot = world.raycast(ctxFoot);
                        
                        boolean canSee = hitCenter.getType() == net.minecraft.util.hit.HitResult.Type.MISS ||
                                         hitHead.getType() == net.minecraft.util.hit.HitResult.Type.MISS ||
                                         hitFoot.getType() == net.minecraft.util.hit.HitResult.Type.MISS;
                                         
                        if (canSee) {
                            System.out.println("[Frost DEBUG] Candidate accepted: visible spawn at " + targetX + " " + cy + " " + targetZ);
                            return new Vec3d(targetX, cy, targetZ);
                        } else {
                            System.out.println("[Frost DEBUG] Candidate rejected: blocked LOS");
                        }
                    } else {
                        // System.out.println("[Frost DEBUG] Candidate rejected: collision or fluid");
                    }
                }
            }
        }
        System.out.println("[Frost DEBUG] No valid visible spawn found.");
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        Text msg = Text.literal("Frost joined the game").formatted(Formatting.YELLOW);
        server.getPlayerManager().broadcast(msg, false);

        if (player != null) {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            
            FrostEntity frost = Godeye.FROST.create(world, net.minecraft.entity.SpawnReason.EVENT);
            if (frost == null) return;
            
            Vec3d spawnPos = findSafeSpawn(world, player, 5.0, 6.0, frost);
            if (spawnPos == null) {
                frost.discard();
                return;
            }
            
            System.out.println("[Frost DEBUG] Spawn requested");
            System.out.println("[Frost DEBUG] Player position: " + player.getEntityPos());
            System.out.println("[Frost DEBUG] Player rotation: <" + player.getYaw() + ", " + player.getPitch() + ">");
            System.out.println("[Frost DEBUG] Spawn position: " + spawnPos);

            frost.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, player.getYaw() + 180.0f, 0);
            world.spawnEntity(frost);
            
            System.out.println("[Frost SOUND DEBUG] Spawn sound requested: godeye:frost_spawn");
            com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.FROST_SPAWN_EVENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
            
            System.out.println("[Frost DEBUG] Entity UUID: " + frost.getUuidAsString());
            System.out.println("[Frost DEBUG] Distance from player: " + player.distanceTo(frost));

            Text msg2 = Text.literal("[Frost DEBUG] Spawned at: " + (int)spawnPos.x + " " + (int)spawnPos.y + " " + (int)spawnPos.z).formatted(Formatting.AQUA);
            player.sendMessage(msg2, false);
        }
        
        if (player != null) {
            PlayerStoryState state = PlayerStoryState.getState(player);
            state.setFlag("ch01_frost_done", true);
        }
    }
}
