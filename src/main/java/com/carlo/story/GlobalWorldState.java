package com.carlo.story;

import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.datafixer.DataFixTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.carlo.story.event.ScheduledEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GlobalWorldState extends PersistentState {
    private String activeChapterId = "ch00";
    private List<ScheduledEvent> scheduledEvents = new ArrayList<>();
    private Map<String, Boolean> globalFlags = new HashMap<>();

    public GlobalWorldState() {}

    public GlobalWorldState(String activeChapterId, List<ScheduledEvent> scheduledEvents, Map<String, Boolean> globalFlags) {
        this.activeChapterId = activeChapterId;
        this.scheduledEvents = new ArrayList<>(scheduledEvents);
        this.globalFlags = new HashMap<>(globalFlags);
    }

    public static final Codec<GlobalWorldState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("ActiveChapterId", "ch00").forGetter(GlobalWorldState::getActiveChapterId),
        ScheduledEvent.CODEC.listOf().optionalFieldOf("ScheduledEvents", List.of()).forGetter(GlobalWorldState::getScheduledEvents),
        Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("GlobalFlags", Map.of()).forGetter(GlobalWorldState::getGlobalFlags)
    ).apply(instance, GlobalWorldState::new));

    public static final PersistentStateType<GlobalWorldState> TYPE = new PersistentStateType<>(
        "godeye_global_state",
        GlobalWorldState::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    public String getActiveChapterId() { return activeChapterId; }
    public void setActiveChapterId(String id) { this.activeChapterId = id; this.markDirty(); }

    public List<ScheduledEvent> getScheduledEvents() { return scheduledEvents; }
    public void setScheduledEvents(List<ScheduledEvent> events) { this.scheduledEvents = events; this.markDirty(); }

    public Map<String, Boolean> getGlobalFlags() { return globalFlags; }
    public boolean getFlag(String key) { return globalFlags.getOrDefault(key, false); }
    public void setFlag(String key, boolean value) { this.globalFlags.put(key, value); this.markDirty(); }

    public static GlobalWorldState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE);
    }
}
