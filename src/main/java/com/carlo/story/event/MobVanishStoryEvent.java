package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MobVanishStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "mob_vanish";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        String activeChap = com.carlo.story.GlobalWorldState.getServerState(server).getActiveChapterId();
        if (activeChap != null && activeChap.compareTo("ch04") >= 0) return;
        if (player == null) return;
        
        Box box = player.getBoundingBox().expand(8.0); // Close proximity (player approaches mob)
        List<Entity> validMobs = player.getEntityWorld().getOtherEntities(player, box, entity -> {
            if (entity instanceof MobEntity mob) {
                if (entity instanceof com.carlo.entity.FrostEntity || entity instanceof com.carlo.entity.WatcherEntity) return false;
                net.minecraft.util.Identifier id = net.minecraft.registry.Registries.ENTITY_TYPE.getId(entity.getType());
                if (id == null || !id.getNamespace().equals("minecraft")) return false;
                if (mob instanceof TameableEntity tameable && tameable.isTamed()) return false;
                
                // Only vanish if the player is moving/looking near them. Just distance is enough.
                return player.distanceTo(mob) < 6.0;
            }
            return false;
        });
        
        if (validMobs.isEmpty()) {
            return; // Fail silently, it's just a passive check now
        }

        int removedCount = 0;
        for (Entity e : validMobs) {
            e.discard(); // Vanish without drops/sound/death
            removedCount++;
        }
        
        PlayerStoryState state = PlayerStoryState.getState(player);
        state.setFlag("ch01_mob_anomaly_seen", true);
    }
}

