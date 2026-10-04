package com.carlo.story.system;

import com.carlo.story.GlobalWorldState;
import com.carlo.story.event.ScheduledEvent;
import com.carlo.story.event.MobVanishStoryEvent;
import com.carlo.story.event.LeafDecayStoryEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class PersistentAnomalySystem {
    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 5 != 0) return; // run 4 times a second
        
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        boolean mobVanish = state.getFlag("mob_vanish_unlocked");
        boolean leafDecay = state.getFlag("leaf_decay_unlocked");
        String activeChapter = state.getActiveChapterId();
        
        if (activeChapter.equals("ch04")) {
            leafDecay = false; // Disabled in CH04
        }
        
        if (!mobVanish && !leafDecay) return;
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (mobVanish) { 
                // Always check for nearby mobs to vanish when unlocked
                ScheduledEvent dummy = new ScheduledEvent("persistent", "mob_vanish", 0, "persistent", Optional.of(player.getUuid()), "", false);
                new MobVanishStoryEvent().execute(server, dummy, player);
            }
            if (leafDecay && server.getTicks() % 40 == 0) { // Every 2 seconds for leaves
                float chance = 0.03f;
                if (activeChapter.equals("ch02")) chance = 0.10f;
                if (activeChapter.equals("ch03")) chance = 0.25f;
                
                if (server.getOverworld().random.nextFloat() < chance) { 
                    ScheduledEvent dummy = new ScheduledEvent("persistent", "leaf_decay", 0, "persistent", Optional.of(player.getUuid()), activeChapter, false);
                    new LeafDecayStoryEvent().execute(server, dummy, player);
                }
            }
        }
    }
}
