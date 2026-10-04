package com.carlo.util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class TestCommand {
    public static void test(MinecraftServer server, BlockPos pos) throws Exception {
        server.getCommandManager().getDispatcher().execute("setworldspawn " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), server.getCommandSource());
    }
}
