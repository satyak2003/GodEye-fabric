package com.carlo.story.system;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.event.EventScheduler;
import com.carlo.story.event.ScheduledEvent;
import java.util.Optional;

public class DeathSystem {
    public static void onPlayerDeath(ServerPlayerEntity player) {
        System.out.println("[GodEye DEATH] Player death detected");
        String activeChap = com.carlo.story.GlobalWorldState.getServerState(((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer()).getActiveChapterId();
        if ("ch05".equals(activeChap)) {
             System.out.println("[GodEye DEATH] Story complete = true. Normal death.");
             return;
        }
        
        System.out.println("[GodEye DEATH] Story complete = false");
        PlayerStoryState state = PlayerStoryState.getState(player);
        
        // Save death location
        System.out.println("[GodEye DEATH] Death location captured: dimension=" + player.getEntityWorld().getRegistryKey().getValue().toString() + ", x=" + player.getBlockPos().getX() + ", y=" + player.getBlockPos().getY() + ", z=" + player.getBlockPos().getZ());
        state.setDeathPos(player.getBlockPos());
        state.setDeathDimension(player.getEntityWorld().getRegistryKey().getValue().toString());
        state.setDeathYaw(player.getYaw());
        state.setDeathPitch(player.getPitch());
        
        // Clean up any active cinematics that MUST end on death (like Witness)
        if (state.getFlag("ch04_witness_active")) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(net.minecraft.util.Identifier.of("godeye", "witness_encounter_1"), net.minecraft.sound.SoundCategory.HOSTILE));
            com.carlo.story.system.CinematicLockSystem.unlockPlayer(player);
            player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
            net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(player.getBlockPos()).expand(50);
            java.util.List<com.carlo.entity.WitnessEntity> mobs = ((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getEntitiesByClass(com.carlo.entity.WitnessEntity.class, box, e -> true);
            for (com.carlo.entity.WitnessEntity witness : mobs) { witness.discard(); }
        }
        
        System.out.println("[GodEye DEATH] Automatic respawn requested");
        // Start death cinematic sequence (Step 1 will auto-respawn and then chain the rest)
        EventScheduler.schedule(((ServerWorld)player.getEntityWorld()).getServer(), new ScheduledEvent(
            "death_seq_1_" + player.getUuidAsString(),
            "godeye_death_step_1",
            player.getEntityWorld().getTime() + 10,
            "system",
            Optional.of(player.getUuid()),
            "",
            false
        ));
    }
}


