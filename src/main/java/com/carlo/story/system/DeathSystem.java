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
        PlayerStoryState state = PlayerStoryState.getState(player);
        
        // Save death location
        state.setDeathPos(player.getBlockPos());
        state.setDeathDimension(player.getEntityWorld().getRegistryKey().getValue().toString());
        state.setDeathYaw(player.getYaw());
        state.setDeathPitch(player.getPitch());
        
        // Clean up any active cinematics
        if (state.getFlag("ch04_witness_active")) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(net.minecraft.util.Identifier.of("godeye", "witness_encounter_1"), net.minecraft.sound.SoundCategory.HOSTILE));
            com.carlo.story.system.CinematicLockSystem.unlockPlayer(player);
            player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
            net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(player.getBlockPos()).expand(50);
            java.util.List<com.carlo.entity.WitnessEntity> mobs = ((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getEntitiesByClass(com.carlo.entity.WitnessEntity.class, box, e -> true);
            for (com.carlo.entity.WitnessEntity witness : mobs) { witness.discard(); }
        }
        
        // Start death cinematic sequence
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

