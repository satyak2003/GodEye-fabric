package com.carlo.story;

import com.carlo.story.event.ChapterTransitionStoryEvent;
import com.carlo.story.event.ControlLockStoryEvent;
import com.carlo.story.event.ScheduledEvent;
import com.carlo.story.event.EventScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import java.util.Optional;
import java.util.Random;

public class Chapter00 implements Chapter {
    @Override
    public String getId() {
        return "ch00";
    }

    @Override
    public void onStart(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        long currentTick = server.getWorld(World.OVERWORLD).getTime();

        if (state.getFlag("ch00_initialized")) return;
        state.setFlag("ch00_initialized", true);

        EventScheduler.schedule(server, new ScheduledEvent("ch00_flash", "night_flash", currentTick + 3600, getId(), Optional.empty(), "20", false));
        ChapterTransitionStoryEvent.scheduleTransitionFlag(server, getId(), currentTick + 3600, "ch00_flash_done");

        EventScheduler.schedule(server, new ScheduledEvent("ch00_freeze_start", "control_lock", currentTick + 6000, getId(), Optional.empty(), "true", false));
        EventScheduler.schedule(server, new ScheduledEvent("ch00_freeze_end", "control_lock", currentTick + 6200, getId(), Optional.empty(), "false", false));
        ChapterTransitionStoryEvent.scheduleTransitionFlag(server, getId(), currentTick + 6200, "ch00_freeze_done");

        long transitionDelay = 8400 + new Random().nextInt(14400 - 8400);
        ChapterTransitionStoryEvent.scheduleTransitionFlag(server, getId(), currentTick + transitionDelay, "ch00_transition_ready");
    }

    @Override
    public void tick(MinecraftServer server) {
    }

    @Override
    public boolean isComplete(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        return state.getFlag("ch00_flash_done") && 
               state.getFlag("ch00_freeze_done") && 
               state.getFlag("ch00_transition_ready");
    }

    @Override
    public String getNextChapterId() {
        return "ch01";
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        GlobalWorldState state = GlobalWorldState.getServerState(player.getEntityWorld().getServer());
        PlayerStoryState pState = PlayerStoryState.getState(player);
        
        if (!pState.getFlag("ch00_spawned")) {
            pState.setFlag("ch00_spawned", true);
            net.minecraft.server.world.ServerWorld world = (net.minecraft.server.world.ServerWorld) player.getEntityWorld();
            var biomeResult = world.locateBiome(b -> b.matchesKey(net.minecraft.world.biome.BiomeKeys.DARK_FOREST), new net.minecraft.util.math.BlockPos(0, 0, 0), 10000, 32, 64);
            if (biomeResult != null) {
                net.minecraft.util.math.BlockPos target = biomeResult.getFirst();
                net.minecraft.util.math.BlockPos safePos = null;
                java.util.Random rand = new java.util.Random();
                
                for (int attempt = 0; attempt < 20; attempt++) {
                    int offsetX = (rand.nextInt(100) - 50);
                    int offsetZ = (rand.nextInt(100) - 50);
                    safePos = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, target.getX() + offsetX, target.getZ() + offsetZ, 16);
                    if (safePos != null) {
                        break;
                    }
                }
                
                if (safePos != null) {
                    try {
                        world.getServer().getCommandManager().getDispatcher().execute("setworldspawn " + safePos.getX() + " " + safePos.getY() + " " + safePos.getZ(), world.getServer().getCommandSource());
                    } catch (Exception e) {}
                    
                    player.teleport(world, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, java.util.Set.of(), player.getYaw(), player.getPitch(), true);
                    player.sendMessage(net.minecraft.text.Text.literal(String.format("[GodEye Debug] Initial spawn resolved at X: %d Y: %d Z: %d. Surface validation: PASS", safePos.getX(), safePos.getY(), safePos.getZ())), false);
                }
            }
        }

        if (pState.getFlag("control_locked")) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.ControlLockPayload(true));
        }
    }
}
