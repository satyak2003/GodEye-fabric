package com.carlo.story.event;

import com.carlo.network.BlackScreenPayload;
import com.carlo.network.ControlLockPayload;
import com.carlo.network.CameraLockPayload;
import com.carlo.network.CinematicTextPayload;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.ChapterManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;

public class CH03FinaleCinematicEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_finale_cinematic";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null || eventData == null) return;
        
        String action = eventData.payload();
        
        switch (action) {
            case "blackscreen":
                ServerPlayNetworking.send(player, new ControlLockPayload(true));
                ServerPlayNetworking.send(player, new CameraLockPayload(true, -1));
                ServerPlayNetworking.send(player, new BlackScreenPayload(true));
                break;
            case "text1":
                ServerPlayNetworking.send(player, new CinematicTextPayload(1));
                break;
            case "text2":
                ServerPlayNetworking.send(player, new CinematicTextPayload(2));
                break;
            case "text3":
                ServerPlayNetworking.send(player, new CinematicTextPayload(3));
                break;
            case "teleport":
                ServerPlayNetworking.send(player, new CinematicTextPayload(0));
                ServerPlayNetworking.send(player, new BlackScreenPayload(false));
                ServerPlayNetworking.send(player, new ControlLockPayload(false));
                ServerPlayNetworking.send(player, new CameraLockPayload(false, -1));
                
                                ServerWorld world = server.getOverworld();
                BlockPos start = player.getBlockPos();
                BlockPos targetPos = world.locateStructure(net.minecraft.registry.tag.StructureTags.VILLAGE, start, 100, false);
                
                // 2. Fallback to a plains biome
                if (targetPos == null) {
                    var biomeResult = world.locateBiome(b -> b.matchesKey(net.minecraft.world.biome.BiomeKeys.PLAINS), start, 6400, 32, 64);
                    if (biomeResult != null) {
                        targetPos = biomeResult.getFirst();
                    }
                }
                
                // 3. Absolute fallback
                if (targetPos == null) {
                    targetPos = start.add(500, 0, 500); // Random distant spot
                }
                
                                                // Safely find the surface at the target X/Z
                BlockPos safePos = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, targetPos.getX(), targetPos.getZ(), 16);
                if (safePos == null) {
                    safePos = targetPos.withY(world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetPos.getX(), targetPos.getZ()));
                }
                
                System.out.println("[GodEye DEBUG] CH03 finale teleport requested: X: " + safePos.getX() + " Y: " + safePos.getY() + " Z: " + safePos.getZ());
                
                player.teleport(world, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, java.util.Set.of(), player.getYaw(), player.getPitch(), true);
                
                System.out.println("[GodEye DEBUG] CH03 finale player position after teleport: X: " + player.getX() + " Y: " + player.getY() + " Z: " + player.getZ());
                
                double distSq = player.squaredDistanceTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                if (distSq < 100.0) {
                    System.out.println("[GodEye DEBUG] Teleport verified successful.");
                    player.sendMessage(net.minecraft.text.Text.literal(String.format("[GodEye Debug] Village teleport successful to X: %d Y: %d Z: %d", safePos.getX(), safePos.getY(), safePos.getZ())), false);
                    PlayerStoryState pState = PlayerStoryState.getState(player);
                    pState.setFlag("ch03_completed", true);
                } else {
                    System.out.println("[GodEye DEBUG] Teleport FAILED! Player is still at X: " + player.getX() + " Z: " + player.getZ());
                    player.sendMessage(net.minecraft.text.Text.literal("[GodEye Debug] Teleport FAILED!"), false);
                }
                break;
        }
    }
}




