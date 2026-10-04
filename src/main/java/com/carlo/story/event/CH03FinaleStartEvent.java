package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.WatcherEntity;
import com.carlo.network.ControlLockPayload;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.SoundHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;
import net.minecraft.sound.SoundCategory;

public class CH03FinaleStartEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch03_finale_start";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_completed")) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // 1. Lock controls immediately BEFORE spawning Watchers
        ServerPlayNetworking.send(player, new ControlLockPayload(true));
        
        // 2. Spawn 8 Watchers in a semi-circle in front of the player
        int count = 8;
        float startAngle = -70.0f;
        float endAngle = 70.0f;
        float angleStep = (endAngle - startAngle) / (count - 1);
        
        for (int i = 0; i < count; i++) {
            float relAngle = startAngle + (i * angleStep);
            float angle = player.getYaw() + relAngle;
            
            double distance = 8.0; // 8 blocks away
            Vec3d offset = Vec3d.fromPolar(0, angle).multiply(distance);
            double targetX = player.getX() + offset.x;
            double targetZ = player.getZ() + offset.z;
            
            BlockPos safePos = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, (int)targetX, (int)targetZ, 3);
            
            if (safePos != null) {
                WatcherEntity watcher = new WatcherEntity(Godeye.GODEYE_WATCHER, world);
                watcher.setPosition(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                watcher.setLifespan(400); // 20 seconds
                
                // Watcher must face the player
                double dx = player.getX() - watcher.getX();
                double dz = player.getZ() - watcher.getZ();
                float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                watcher.setYaw(yaw);
                watcher.setBodyYaw(yaw);
                watcher.setHeadYaw(yaw);
                
                world.spawnEntity(watcher);
            }
        }
        
        // 3. Play Watcher Appearance Sound (just one, centrally for the player)
        SoundHelper.playToPlayer(player, Godeye.WATCHER_TP_EVENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        
        // 4. Play BGM (Non-positional, player specific)
        SoundHelper.playToPlayer(player, Godeye.CH03_BGM_EVENT, SoundCategory.MASTER, 1.0f, 1.0f);
        
        long startTick = server.getOverworld().getTime();
        boolean fast = pState.getFlag("ch03_debug_fast");
        
        long t1 = fast ? 20 : 100;
        long t2 = fast ? 40 : 140;
        long t3 = fast ? 60 : 240;
        long t4 = fast ? 80 : 340;
        long t5 = fast ? 100 : 440;
        
        EventScheduler.schedule(server, new ScheduledEvent("ch03_fin_1_" + player.getUuidAsString(), "ch03_finale_cinematic", startTick + t1, "ch03", Optional.of(player.getUuid()), "blackscreen", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch03_fin_2_" + player.getUuidAsString(), "ch03_finale_cinematic", startTick + t2, "ch03", Optional.of(player.getUuid()), "text1", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch03_fin_3_" + player.getUuidAsString(), "ch03_finale_cinematic", startTick + t3, "ch03", Optional.of(player.getUuid()), "text2", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch03_fin_4_" + player.getUuidAsString(), "ch03_finale_cinematic", startTick + t4, "ch03", Optional.of(player.getUuid()), "text3", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch03_fin_5_" + player.getUuidAsString(), "ch03_finale_cinematic", startTick + t5, "ch03", Optional.of(player.getUuid()), "teleport", false));
    }
}
