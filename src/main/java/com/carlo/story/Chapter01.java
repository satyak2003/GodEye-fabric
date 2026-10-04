package com.carlo.story;

import com.carlo.story.event.ChapterTransitionStoryEvent;
import com.carlo.story.event.ScheduledEvent;
import com.carlo.story.event.EventScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import java.util.Optional;
import java.util.Random;

public class Chapter01 implements Chapter {
    @Override
    public String getId() {
        return "ch01";
    }

    @Override
    public void onStart(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        if (state.getFlag("ch01_initialized")) return;
        state.setFlag("ch01_initialized", true);
        
        long currentTick = server.getWorld(World.OVERWORLD).getTime();

        // CH01 logic: Randomly schedule for currently online players
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            schedulePlayerAnomalies(server, player, currentTick);
        }
    }

    private void schedulePlayerAnomalies(MinecraftServer server, ServerPlayerEntity player, long startTick) {
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch01_anomalies_scheduled")) return;
        pState.setFlag("ch01_anomalies_scheduled", true);

        Random random = new Random();
        
        // 4-5 teleports over 2-5 mins (2400 - 6000 ticks)
        int teleports = 4 + random.nextInt(2);
        pState.setTeleportsRemaining(teleports);
        
        for (int i = 0; i < teleports; i++) {
            long delay = 2400 + random.nextInt(3600);
            EventScheduler.schedule(server, new ScheduledEvent(
                "teleport_" + player.getUuidAsString() + "_" + i, 
                "teleport_anomaly", 
                startTick + delay, 
                getId(), 
                Optional.of(player.getUuid()), 
                "", false
            ));
        }

                // Unlock persistent anomalies
        GlobalWorldState.getServerState(server).setFlag("mob_vanish_unlocked", true);
        GlobalWorldState.getServerState(server).setFlag("leaf_decay_unlocked", true);

        // Sound anomalies (Creeper/Cave)
        for (int i = 0; i < 4; i++) {
            long delay = 1200 + random.nextInt(4800);
            String type = random.nextBoolean() ? "creeper" : "cave";
            EventScheduler.schedule(server, new ScheduledEvent("sound_" + player.getUuidAsString() + "_" + i, "sound_anomaly", startTick + delay, getId(), Optional.of(player.getUuid()), type, false));
        }

        // Frost intro (between 5 and 7 mins -> 6000 - 8400 ticks)
        long frostDelay = 6000 + random.nextInt(2400);
        EventScheduler.schedule(server, new ScheduledEvent("frost_" + player.getUuidAsString(), "frost_join", startTick + frostDelay, getId(), Optional.of(player.getUuid()), "", false));
        
        // Final transition flag (frost + tension period)
        ChapterTransitionStoryEvent.scheduleTransitionFlag(server, getId(), startTick + frostDelay + 2400, "ch01_tension_done_" + player.getUuidAsString());
    }

    @Override
    public void tick(MinecraftServer server) {
    }

    @Override
    public boolean isComplete(MinecraftServer server) {
        // Since chapter is global but anomalies are per-player, we consider CH01 complete
        // when AT LEAST ONE player has successfully completed the Frost intro and their tension period.
        // And they must have finished their teleport sequence.
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerStoryState pState = PlayerStoryState.getState(player);
            GlobalWorldState state = GlobalWorldState.getServerState(server);
            
            boolean teleportsDone = pState.getFlag("ch01_teleports_completed");
            boolean frostDone = pState.getFlag("ch01_frost_done");
            boolean tensionDone = state.getFlag("ch01_tension_done_" + player.getUuidAsString());
            if (teleportsDone && frostDone && tensionDone) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getNextChapterId() {
        return "ch02";
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        long currentTick = ((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer().getWorld(World.OVERWORLD).getTime();
        schedulePlayerAnomalies(((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer(), player, currentTick);
    }
}



