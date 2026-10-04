package com.carlo.story;

import com.carlo.story.event.EventScheduler;
import com.carlo.story.event.ScheduledEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class Chapter03 implements Chapter {
    public static final int NORMAL_ENCOUNTER_LIMIT = 3;
    public static final int NORMAL_ENCOUNTER_RELOCATIONS = 2;
    @Override
    public String getId() {
        return "ch03";
    }

    @Override
    public void onStart(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        if (state.getFlag("ch03_initialized")) return;
        state.setFlag("ch03_initialized", true);
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            startPlayerSequence(server, player);
        }
    }

    private void startPlayerSequence(MinecraftServer server, ServerPlayerEntity player) {
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch03_started")) return;
        pState.setFlag("ch03_started", true);
        pState.setCh03EncounterCount(0);
        
        long currentTick = server.getOverworld().getTime();
        
        EventScheduler.schedule(server, new ScheduledEvent(
            "ch03_start_" + player.getUuidAsString(),
            "ch03_start_encounter",
            currentTick + 100,
            getId(),
            Optional.of(player.getUuid()),
            "", false
        ));
    }

    @Override
    public void tick(MinecraftServer server) {
    }

    @Override
    public boolean isComplete(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerStoryState pState = PlayerStoryState.getState(player);
            if (pState.getFlag("ch03_completed")) return true;
        }
        return false;
    }

    @Override
    public String getNextChapterId() {
        return "ch04";
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        startPlayerSequence(((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer(), player);
    }
}


