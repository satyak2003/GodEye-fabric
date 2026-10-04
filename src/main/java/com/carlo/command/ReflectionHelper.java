package com.carlo.command;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import java.lang.reflect.Method;
public class ReflectionHelper {
    public static void run() {
        System.out.println("Methods in PlayerEntity:");
        for (Method m : PlayerEntity.class.getMethods()) {
            if (m.getName().equals("playSound")) {
                System.out.println(m);
            }
        }
        System.out.println("Methods in Entity:");
        for (Method m : Entity.class.getMethods()) {
            if (m.getName().equals("playSound")) {
                System.out.println(m);
            }
        }
    }
}
