package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

public class SoundAnomalyStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "sound_anomaly";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        
        String type = eventData.payload();
        Random random = new Random();
        
        if ("creeper".equals(type)) {
            // Play creeper sound behind player
            Vec3d back = Vec3d.fromPolar(0, player.getYaw()).multiply(-2.0).add(
                (random.nextDouble() - 0.5), 0, (random.nextDouble() - 0.5)
            );
            Vec3d soundPos = player.getEntityPos().add(back);
            
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(net.minecraft.registry.entry.RegistryEntry.of(SoundEvents.ENTITY_CREEPER_PRIMED), SoundCategory.AMBIENT, soundPos.x, soundPos.y, soundPos.z, 1.0f, 1.0f, random.nextLong()));
        } else if ("cave".equals(type)) {
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, random.nextLong()));
        }
        
        PlayerStoryState state = PlayerStoryState.getState(player);
        state.setFlag("ch01_sound_anomaly_seen", true);
    }
}
