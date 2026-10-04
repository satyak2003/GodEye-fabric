package com.carlo.client;

import net.minecraft.util.math.Vec3d;
import java.util.List;
import java.util.ArrayList;

public class ClientStoryState {
    public static boolean isCameraLocked = false;
    public static int cameraLockTargetId = -1;
    public static int nightFlashTicks = 0;
    public static boolean isControlLocked = false;
    public static boolean isCinematicLocked = false;
    public static boolean isBlackScreen = false;
    public static int cinematicTextStage = 0;
    public static int cinematicTextTicks = 0;
    public static boolean ch04Snow = false;
    
    public static class ActiveParticleEffect {
        public double x, y, z;
        public boolean isWitness;
        public int ticks;
        public ActiveParticleEffect(double x, double y, double z, boolean isWitness) {
            this.x = x; this.y = y; this.z = z; this.isWitness = isWitness; this.ticks = 0;
        }
    }
    public static List<ActiveParticleEffect> activeEffects = new ArrayList<>();
}
