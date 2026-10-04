package com.carlo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class CacheDumpCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("godeyereflect")
                .executes(context -> {
                    ReflectionHelper.run();
                    context.getSource().sendMessage(Text.literal("Reflection run. Check console."));
                    return 1;
                })
            );
        });
    }
}
