package com.carlo.story.event;

import com.carlo.Godeye;
import com.carlo.entity.FrostEntity;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.system.CinematicLockSystem;
import com.carlo.story.system.SoundHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public class CH02JumpscareStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch02_jumpscare";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Spawn Frost directly in front
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d pos = player.getEyePos().add(look.multiply(3.0)); // 3 blocks ahead
        
        int targetY = (int) pos.y;
        FrostEntity frost = new FrostEntity(Godeye.FROST, world);
        
        BlockPos safePos = null;
        for (int y = targetY + 2; y >= targetY - 4; y--) {
            BlockPos test = new BlockPos((int) pos.x, y, (int) pos.z);
            if (world.getBlockState(test.down()).isSolidBlock(world, test.down())) {
                frost.setPosition(test.getX() + 0.5, test.getY(), test.getZ() + 0.5);
                if (world.isSpaceEmpty(frost) && !world.containsFluid(frost.getBoundingBox())) {
                    safePos = test;
                    break;
                }
            }
        }
        
        if (safePos == null) {
            safePos = new BlockPos((int) pos.x, targetY, (int) pos.z);
            frost.setPosition(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        }
        
        double dx = player.getX() - frost.getX();
        double dz = player.getZ() - frost.getZ();
        float frostYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        
        double dy = player.getEyeY() - frost.getEyeY();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float frostPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        
        frost.setYaw(frostYaw);
        frost.setBodyYaw(frostYaw);
        frost.setHeadYaw(frostYaw);
        frost.setPitch(frostPitch);
        
        frost.setNoGravity(true);
        world.spawnEntity(frost);
        
        System.out.println("[GodEye DEBUG] CH02 jumpscare START playerPos=" + player.getEntityPos());
        System.out.println("[GodEye DEBUG] CH02 control lock ENABLED");
        System.out.println("[GodEye DEBUG] CH02 camera lock ENABLED target=" + frost.getId());
        
        // Lock controls and camera
        player.closeHandledScreen();
        PlayerStoryState.getState(player).setFlag("ch02_jumpscare_active", true);
        CinematicLockSystem.applyLock(player, true, true, frost.getId());
        
        // Darkness effect
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0, false, false, false));
        
        // Sound
        SoundHelper.playToPlayer(player, Godeye.JUMPSCARE_EVENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        
        // Schedule cleanup in 5 seconds (100 ticks)
        long currentTick = server.getOverworld().getTime();
        String payload = frost.getUuidAsString();
        EventScheduler.schedule(server, new ScheduledEvent("ch02_clean_" + player.getUuidAsString(), "ch02_jumpscare_cleanup", currentTick + 100, "ch02", Optional.of(player.getUuid()), payload, false));
    }
}

