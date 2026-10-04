package com.carlo.story.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class PlayerPositionTracker {
    // Max records per player
    private static final int MAX_RECORDS = 5;
    private static final int RECORD_INTERVAL = 20; // 1 second
    
    private static int tickCounter = 0;
    
    // UUID -> Deque of positions (newest last)
    private static final Map<UUID, LinkedList<Vec3d>> history = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= RECORD_INTERVAL) {
                tickCounter = 0;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    recordPosition(player);
                }
            }
        });
    }

    private static void recordPosition(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        history.putIfAbsent(id, new LinkedList<>());
        LinkedList<Vec3d> list = history.get(id);
        
        list.addLast(player.getEntityPos());
        if (list.size() > MAX_RECORDS) {
            list.removeFirst();
        }
    }
    
    public static Vec3d getOldestPosition(ServerPlayerEntity player) {
        LinkedList<Vec3d> list = history.get(player.getUuid());
        if (list == null || list.isEmpty()) {
            return player.getEntityPos();
        }
        return list.getFirst();
    }
}
