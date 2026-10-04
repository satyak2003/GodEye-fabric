package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.SoundHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class CH04DialogueEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_dialogue";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed")) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        String step = eventData.payload();
        
        switch (step) {
            case "1":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "That was close."); com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_2_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 60, "ch04", Optional.of(player.getUuid()), "2", false));
                break;
            case "2":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "you need to get out of this village and find the CORE,"); com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_3_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 60, "ch04", Optional.of(player.getUuid()), "3", false));
                break;
            case "3":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "its the only way to stop all this."); com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_4_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 60, "ch04", Optional.of(player.getUuid()), "4", false));
                break;
            case "4":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "find the three Veil Shards."); com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_5_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 60, "ch04", Optional.of(player.getUuid()), "5", false));
                break;
            case "5":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "they are somewhere in this village."); com.carlo.story.system.SoundHelper.playToPlayer(player, com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_6_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 60, "ch04", Optional.of(player.getUuid()), "6", false));
                break;
            case "6":
                com.carlo.story.system.DialogueSystem.send(player, "<Unknown>", "and...");
                SoundHelper.playToPlayer(player, net.minecraft.sound.SoundEvents.BLOCK_GLASS_BREAK, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.5f); // Glitch sound
                EventScheduler.schedule(server, new ScheduledEvent("ch04_dial_7_" + player.getUuidAsString(), "ch04_dialogue", world.getTime() + 40, "ch04", Optional.of(player.getUuid()), "finish", false));
                break;
            case "finish":
                // Sudden daytime
                world.setTimeOfDay(6000);
                world.setWeather(100000, 0, false, false);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.CH04SnowPayload(false));
                
                                // Cleanup and transition
                pState.setFlag("ch04_completed", true);
                pState.setFlag("ch04_snow_active", false);
                pState.setFlag("ch04_witness_active", false);
                
                net.minecraft.util.math.BlockPos center = pState.getCh04VillageCenter();
                if (center != null) {
                    net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(center).expand(60);
                    java.util.List<net.minecraft.entity.mob.MobEntity> mobs = world.getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class, box, e -> true);
                    for (net.minecraft.entity.mob.MobEntity mob : mobs) {
                        if (mob instanceof com.carlo.util.Ch04Freezable f) {
                            f.setCh04Frozen(false);
                        }
                    }
                }
                
                // End chapter
                com.carlo.story.ChapterManager.startNormalTransition(server, "ch05");
                break;
        }
    }
}



