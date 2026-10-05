package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.WitnessEntity;
import com.carlo.network.CameraLockPayload;
import com.carlo.network.CinematicLockPayload;
import com.carlo.story.PlayerStoryState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public class CH04WitnessSpawnEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_witness_spawn";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_completed")) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (player.getFrozenTicks() > 0) player.setFrozenTicks(0);
        
        // Spawn Witness 10-15 blocks away, visible, in front of player
        float angle = player.getYaw();
        double distance = 10.0 + world.random.nextDouble() * 5.0;
        Vec3d offset = Vec3d.fromPolar(0, angle).multiply(distance);
        double targetX = player.getX() + offset.x;
        double targetZ = player.getZ() + offset.z;
        
        BlockPos safePos = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, (int)targetX, (int)targetZ, 5);
        if (safePos == null) {
            safePos = player.getBlockPos().add((int)offset.x, 0, (int)offset.z);
        }
        
        WitnessEntity witness = new WitnessEntity(Godeye.WITNESS, world);
        witness.setPosition(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        
        // Witness initially faces away from player
        witness.setYaw(player.getYaw());
        witness.setBodyYaw(player.getYaw());
        witness.setHeadYaw(player.getYaw());
        
        world.spawnEntity(witness);
        
        pState.setFlag("ch04_witness_active", true);
        com.carlo.story.system.CinematicLockSystem.applyLock(player, true, true, witness.getId());
        
        // Start playing the sound!
        com.carlo.story.system.SoundHelper.playToPlayer(player, net.minecraft.registry.Registries.SOUND_EVENT.get(net.minecraft.util.Identifier.of("godeye", "witness_encounter_1")), net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        
        // Add darkness
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.DARKNESS, 20 * 60, 0, false, false, false));
        
        // Schedule next phase: witness turn (wait maybe 2 seconds before turning and approaching)
        EventScheduler.schedule(server, new ScheduledEvent("ch04_wit_turn_" + player.getUuidAsString(), "ch04_witness_turn", world.getTime() + 40, "ch04", Optional.of(player.getUuid()), witness.getUuidAsString(), false));
    }
}




