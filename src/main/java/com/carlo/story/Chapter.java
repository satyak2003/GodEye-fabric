package com.carlo.story;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Chapter {
    String getId();
    void onStart(MinecraftServer server);
    void tick(MinecraftServer server);
    boolean isComplete(MinecraftServer server);
    String getNextChapterId();
    void onPlayerJoin(ServerPlayerEntity player);
}
