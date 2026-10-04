package com.carlo;
import java.lang.reflect.Method;
public class BiomeReflect {
    public static void main(String[] args) throws Exception {
        for(Method m : net.minecraft.world.biome.Biome.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
        }
    }
}
