package com.carlo.story;

import com.carlo.story.event.EventScheduler;
import com.carlo.story.event.ScheduledEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class Chapter02 implements Chapter {
    @Override
    public String getId() {
        return "ch02";
    }

    @Override
    public void onStart(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        if (state.getFlag("ch02_initialized")) return;
        state.setFlag("ch02_initialized", true);
        
        // Start sequence for currently online players
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            startPlayerSequence(server, player);
        }
    }

    private void startPlayerSequence(MinecraftServer server, ServerPlayerEntity player) {
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch02_started")) return;
        pState.setFlag("ch02_started", true);
        
        long currentTick = server.getOverworld().getTime();
        
        EventScheduler.schedule(server, new ScheduledEvent(
            "ch02_start_" + player.getUuidAsString(),
            "ch02_message",
            currentTick + 20,
            getId(),
            Optional.of(player.getUuid()),
            "start", false
        ));
    }

    @Override
    public void tick(MinecraftServer server) {
    }

    @Override
    public boolean isComplete(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerStoryState pState = PlayerStoryState.getState(player);
            if (pState.getFlag("ch02_completed")) return true;
        }
        return false;
    }

    @Override
    public String getNextChapterId() {
        return "ch03";
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        startPlayerSequence(((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer(), player);
    }
}

