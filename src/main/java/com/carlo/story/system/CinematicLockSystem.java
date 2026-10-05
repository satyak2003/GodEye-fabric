package com.carlo.story.system;

import com.carlo.network.CameraLockPayload;
import com.carlo.network.CinematicLockPayload;
import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CinematicLockSystem {
    private static final Map<UUID, Integer> cameraLockTargets = new HashMap<>();

    public static void applyLock(ServerPlayerEntity player, boolean movementLocked, boolean cameraLocked, int targetId) {
        PlayerStoryState state = PlayerStoryState.getState(player);
        state.setFlag("cinematic_locked", movementLocked);
        state.setFlag("control_locked", movementLocked);
        state.setFlag("camera_locked", cameraLocked);
        
        if (cameraLocked) {
            cameraLockTargets.put(player.getUuid(), targetId);
        } else {
            cameraLockTargets.remove(player.getUuid());
        }

        player.setVelocity(Vec3d.ZERO);
        player.velocityDirty = true;

        ServerPlayNetworking.send(player, new CinematicLockPayload(movementLocked));
        ServerPlayNetworking.send(player, new ControlLockPayload(movementLocked));
        ServerPlayNetworking.send(player, new CameraLockPayload(cameraLocked, targetId));
    }

    public static void unlockPlayer(ServerPlayerEntity player) {
        unlockPlayer(player, "standard unlock");
    }

    public static void unlockPlayer(ServerPlayerEntity player, String reason) {
        PlayerStoryState state = PlayerStoryState.getState(player);
        boolean wasJumpscare = state.getFlag("ch02_jumpscare_active");
        
        if (wasJumpscare) {
            System.out.println("[GodEye DEBUG] CH02 cleanup BEGIN reason=" + reason);
        }

        state.setFlag("cinematic_locked", false);
        state.setFlag("control_locked", false);
        state.setFlag("camera_locked", false);
        state.setFlag("ch02_jumpscare_active", false);
        state.setFlag("ch04_witness_active", false);
        cameraLockTargets.remove(player.getUuid());

        player.removeStatusEffect(StatusEffects.DARKNESS);
        player.setVelocity(Vec3d.ZERO);
        player.velocityDirty = true;

        ServerPlayNetworking.send(player, new CinematicLockPayload(false));
        ServerPlayNetworking.send(player, new ControlLockPayload(false));
        ServerPlayNetworking.send(player, new CameraLockPayload(false, -1));

        if (wasJumpscare) {
            System.out.println("[GodEye DEBUG] CH02 camera lock CLEARED");
            System.out.println("[GodEye DEBUG] CH02 control lock CLEARED");
            System.out.println("[GodEye DEBUG] CH02 cleanup END");
        }
    }

    public static void tick(MinecraftServer server) {
        if (cameraLockTargets.isEmpty()) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Integer targetId = cameraLockTargets.get(player.getUuid());
            if (targetId != null && targetId != -1) {
                Entity target = ((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getEntityById(targetId);
                if (target == null || target.isRemoved() || !target.isAlive()) {
                    boolean wasJumpscare = PlayerStoryState.getState(player).getFlag("ch02_jumpscare_active");
                    unlockPlayer(player, "target entity missing or removed");
                    if (wasJumpscare) {
                        PlayerStoryState.getState(player).setFlag("ch02_completed", true);
                    }
                }
            }
        }
    }
}

