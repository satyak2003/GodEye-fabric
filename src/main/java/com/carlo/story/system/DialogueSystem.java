package com.carlo.story.system;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DialogueSystem {
    public static void send(ServerPlayerEntity player, String speaker, String message) {
        if (player == null) return;
        
        if (message.isEmpty()) {
            player.sendMessage(Text.literal(String.format("<%s> ", speaker)), false);
        } else {
            player.sendMessage(Text.literal(String.format("<%s> %s", speaker, message)), false);
        }
    }
}
