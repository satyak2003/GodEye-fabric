package com.carlo.util;
import net.minecraft.world.WorldProperties;
import java.lang.reflect.Method;
public class TestRun {
    public static void test() {
        for (Method m : WorldProperties.class.getMethods()) {
            if (m.getName().toLowerCase().contains("spawn")) {
                System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
