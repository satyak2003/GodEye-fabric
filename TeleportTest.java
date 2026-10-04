package com.carlo;
import net.minecraft.server.network.ServerPlayerEntity;
import java.lang.reflect.Method;
public class TeleportTest {
    public static void main(String[] args) {
        for(Method m : ServerPlayerEntity.class.getMethods()) {
            if (m.getName().toLowerCase().contains("teleport")) {
                System.out.println(m.getName() + ": " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
