package com.carlo.util;
import java.lang.reflect.Method;
import net.minecraft.world.World;
import net.minecraft.client.world.ClientWorld;
public class TestReflect {
    public static void main(String[] args) {
        System.out.println("World methods:");
        for (Method m : World.class.getMethods()) {
            if (m.getName().contains("Particle") || m.getName().contains("particle")) {
                System.out.println(m);
            }
        }
    }
}
