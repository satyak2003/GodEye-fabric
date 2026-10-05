package com.carlo.util;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
public class TestFreeze {
    public static void test(ServerPlayerEntity player) {
        player.setFrozenTicks(400);
    }
}
