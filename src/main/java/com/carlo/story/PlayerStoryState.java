package com.carlo.story;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class PlayerStoryState {
    private int ch04VillageCenterX = 0;
    private int ch04VillageCenterY = 0;
    private int ch04VillageCenterZ = 0;
    private boolean ch04VillageCenterSet = false;
    
    private net.minecraft.util.math.BlockPos deathPos = null;
    private String deathDimension = "";
    private float deathYaw = 0;
    private float deathPitch = 0;
    private long invulnerabilityEndTick = 0;

    public void setCh04VillageCenter(net.minecraft.util.math.BlockPos pos) {
        this.ch04VillageCenterX = pos.getX();
        this.ch04VillageCenterY = pos.getY();
        this.ch04VillageCenterZ = pos.getZ();
        this.ch04VillageCenterSet = true;
        markDirty();
    }

    public net.minecraft.util.math.BlockPos getCh04VillageCenter() {
        if (!ch04VillageCenterSet) return null;
        return new net.minecraft.util.math.BlockPos(ch04VillageCenterX, ch04VillageCenterY, ch04VillageCenterZ);
    }
    
    public net.minecraft.util.math.BlockPos getDeathPos() { return deathPos; }
    public void setDeathPos(net.minecraft.util.math.BlockPos pos) { this.deathPos = pos; markDirty(); }
    public String getDeathDimension() { return deathDimension; }
    public void setDeathDimension(String dim) { this.deathDimension = dim; markDirty(); }
    public float getDeathYaw() { return deathYaw; }
    public void setDeathYaw(float yaw) { this.deathYaw = yaw; markDirty(); }
    public float getDeathPitch() { return deathPitch; }
    public void setDeathPitch(float pitch) { this.deathPitch = pitch; markDirty(); }
    public long getInvulnerabilityEndTick() { return invulnerabilityEndTick; }
    public void setInvulnerabilityEndTick(long tick) { this.invulnerabilityEndTick = tick; markDirty(); }

    private Map<String, Boolean> flags = new HashMap<>();
    private int observationLevel = 0;

    // We rely on GlobalWorldState for actual persistent saving, but keeping player states cached
    private static final Map<java.util.UUID, PlayerStoryState> states = new HashMap<>();

    public static PlayerStoryState getState(ServerPlayerEntity player) {
        return states.computeIfAbsent(player.getUuid(), k -> new PlayerStoryState());
    }
    
    public static PlayerStoryState getState(java.util.UUID uuid) {
        return states.computeIfAbsent(uuid, k -> new PlayerStoryState());
    }

        private int teleportsRemaining = 0;
    private int ch03EncounterCount = 0;
    private int ch03RelocationCount = 0;

    public Map<String, Boolean> getFlags() { return flags; }
    public void setObservationLevel(int level) { this.observationLevel = level; markDirty(); }
    
    public int getTeleportsRemaining() { return teleportsRemaining; }
    public void setTeleportsRemaining(int t) { this.teleportsRemaining = t; markDirty(); }
    
    public int getCh03EncounterCount() { return ch03EncounterCount; }
    public void setCh03EncounterCount(int c) { this.ch03EncounterCount = c; markDirty(); }
    
    public int getCh03RelocationCount() { return ch03RelocationCount; }
    public void setCh03RelocationCount(int c) { this.ch03RelocationCount = c; markDirty(); }
	public boolean getFlag(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
        markDirty(); // Trigger global save
    }
    
    public int getObservationLevel() {
        return observationLevel;
    }
    
    public void addObservation() {
        observationLevel++;
        markDirty();
    }

    private void markDirty() {
        
    }
    
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound flagsNbt = new NbtCompound();
        flags.forEach(flagsNbt::putBoolean);
        nbt.put("Flags", flagsNbt);
        nbt.putInt("ObservationLevel", observationLevel);
        nbt.putInt("TeleportsRemaining", teleportsRemaining);
        nbt.putInt("Ch03EncounterCount", ch03EncounterCount);
        nbt.putInt("Ch03RelocationCount", ch03RelocationCount);
        
        nbt.putInt("Ch04VillageCenterX", ch04VillageCenterX);
        nbt.putInt("Ch04VillageCenterY", ch04VillageCenterY);
        nbt.putInt("Ch04VillageCenterZ", ch04VillageCenterZ);
        nbt.putBoolean("Ch04VillageCenterSet", ch04VillageCenterSet);
        
        if (deathPos != null) {
            nbt.putInt("DeathPosX", deathPos.getX());
            nbt.putInt("DeathPosY", deathPos.getY());
            nbt.putInt("DeathPosZ", deathPos.getZ());
            nbt.putString("DeathDimension", deathDimension);
            nbt.putFloat("DeathYaw", deathYaw);
            nbt.putFloat("DeathPitch", deathPitch);
            nbt.putBoolean("HasDeathPos", true);
        } else {
            nbt.putBoolean("HasDeathPos", false);
        }
        nbt.putLong("InvulnerabilityEndTick", invulnerabilityEndTick);

        return nbt;
    }

    public static PlayerStoryState fromNbt(NbtCompound nbt) {
        PlayerStoryState state = new PlayerStoryState();
        NbtCompound flagsNbt = nbt.getCompound("Flags").orElse(new NbtCompound());
        for (String key : flagsNbt.getKeys()) {
            state.flags.put(key, flagsNbt.getBoolean(key).orElse(false));
        }
        state.observationLevel = nbt.getInt("ObservationLevel").orElse(0);
        state.teleportsRemaining = nbt.getInt("TeleportsRemaining").orElse(0);
        state.ch03EncounterCount = nbt.getInt("Ch03EncounterCount").orElse(0);
        state.ch03RelocationCount = nbt.getInt("Ch03RelocationCount").orElse(0);
        
        if (nbt.contains("Ch04VillageCenterX")) {
            state.ch04VillageCenterX = nbt.getInt("Ch04VillageCenterX").orElse(0);
            state.ch04VillageCenterY = nbt.getInt("Ch04VillageCenterY").orElse(0);
            state.ch04VillageCenterZ = nbt.getInt("Ch04VillageCenterZ").orElse(0);
            state.ch04VillageCenterSet = nbt.getBoolean("Ch04VillageCenterSet").orElse(false);
        }
        
        if (nbt.getBoolean("HasDeathPos").orElse(false)) {
            state.deathPos = new net.minecraft.util.math.BlockPos(nbt.getInt("DeathPosX").orElse(0), nbt.getInt("DeathPosY").orElse(0), nbt.getInt("DeathPosZ").orElse(0));
            state.deathDimension = nbt.getString("DeathDimension").orElse("");
            state.deathYaw = nbt.getFloat("DeathYaw").orElse(0.0f);
            state.deathPitch = nbt.getFloat("DeathPitch").orElse(0.0f);
        }
        state.invulnerabilityEndTick = nbt.getLong("InvulnerabilityEndTick").orElse(0L);
        
        return state;
    }
}



