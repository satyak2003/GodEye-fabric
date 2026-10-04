package com.carlo.story.system;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundHelper {
    public static void playToPlayer(ServerPlayerEntity player, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (player == null || sound == null) return;
        
        RegistryEntry<SoundEvent> entry = RegistryEntry.of(sound);
        PlaySoundS2CPacket packet = new PlaySoundS2CPacket(
            entry, 
            category, 
            player.getX(), 
            player.getY(), 
            player.getZ(), 
            volume, 
            pitch, 
            player.getRandom().nextLong()
        );
        
        player.networkHandler.sendPacket(packet);
        System.out.println("[Frost SOUND DEBUG] Sending packet directly to player: " + player.getName().getString());
    }
    public static void stopSound(ServerPlayerEntity player, SoundEvent sound, SoundCategory category) {
        if (player == null || sound == null) return;
        net.minecraft.network.packet.s2c.play.StopSoundS2CPacket packet = new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(sound.id(), category);
        player.networkHandler.sendPacket(packet);
    }
}

