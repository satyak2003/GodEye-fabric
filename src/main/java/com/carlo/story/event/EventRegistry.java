package com.carlo.story.event;
import java.util.HashMap;
import java.util.Map;

public class EventRegistry {
    private static final Map<String, StoryEvent> EVENTS = new HashMap<>();
    public static void register(StoryEvent event) {
        EVENTS.put(event.getEventType(), event);
    }
    public static StoryEvent get(String type) {
        return EVENTS.get(type);
    }
}

