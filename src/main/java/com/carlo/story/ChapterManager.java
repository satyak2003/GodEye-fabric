package com.carlo.story;

import com.carlo.story.event.ScheduledEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import java.util.ArrayList;

public class ChapterManager {
    public static void tick(MinecraftServer server) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        String activeId = state.getActiveChapterId();
        
        if (!state.getFlag("world_initialized")) {
            Chapter initial = ChapterRegistry.get(activeId);
            System.out.println("[GodEye DEBUG] Initial chapter lookup: " + activeId + " -> " + (initial != null ? initial.getClass().getName() : "null"));
            
            if (initial != null) {
                initial.onStart(server);
                state.setFlag("world_initialized", true);
            } else {
                System.err.println("[GodEye ERROR] ChapterRegistry missing initial chapter: " + activeId + ". Cannot complete world initialization!");
            }
        }
        
        Chapter chapter = ChapterRegistry.get(activeId);
        if (chapter != null) {
            chapter.tick(server);
            
            if (chapter.isComplete(server)) {
                System.out.println("[GodEye Chapter] Chapter complete: " + activeId);
                String nextId = chapter.getNextChapterId();
                if (nextId != null && ChapterRegistry.get(nextId) != null) {
                    System.out.println("[GodEye Chapter] Normal transition requested: " + activeId + " -> " + nextId);
                    startNormalTransition(server, nextId);
                }
            }
        }
    }
    
    public static void onPlayerJoin(ServerPlayerEntity player) {
        GlobalWorldState state = GlobalWorldState.getServerState(player.getEntityWorld().getServer());
        Chapter chapter = ChapterRegistry.get(state.getActiveChapterId());
        if (chapter != null) {
            chapter.onPlayerJoin(player);
        }
    }
    
    public static void startNormalTransition(MinecraftServer server, String nextId) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        String currentId = state.getActiveChapterId();
        
        // Clean up ONLY the orphaned events from the previous chapter
        java.util.List<com.carlo.story.event.ScheduledEvent> events = state.getScheduledEvents();
        events.removeIf(e -> e.chapterId().equals(currentId));
        state.setScheduledEvents(events);
        
        state.setActiveChapterId(nextId);
        
        Chapter next = ChapterRegistry.get(nextId);
        System.out.println("[GodEye DEBUG] Transition:");
        System.out.println("old=" + currentId);
        System.out.println("next=" + nextId);
        System.out.println("nextChapterResolved=" + (next != null));
        
        if (next != null) {
            System.out.println("[GodEye DEBUG] " + nextId + ".onStart() ENTER");
            next.onStart(server);
            
            // Count events for the new chapter
            int nextEvents = 0;
            for (com.carlo.story.event.ScheduledEvent e : state.getScheduledEvents()) {
                if (nextId.equals(e.chapterId())) {
                    nextEvents++;
                }
            }
            System.out.println("[GodEye DEBUG] " + nextId + ".onStart() scheduled events=" + nextEvents);
        }
    }
    
    public static void debugForceChapter(MinecraftServer server, String targetId) {
        GlobalWorldState state = GlobalWorldState.getServerState(server);
        String currentId = state.getActiveChapterId();
        
        // Clean up orphaned events from the current chapter
        List<ScheduledEvent> events = state.getScheduledEvents();
        events.removeIf(e -> e.chapterId().equals(currentId));
        state.setScheduledEvents(events);
        
        // DEBUG TRANSITION: We intentionally wipe the target chapter's flags
        // so it can cleanly reinitialize for debugging.
        List<String> keysToRemove = new ArrayList<>();
        for (String key : state.getGlobalFlags().keySet()) {
            if (key.startsWith(targetId + "_")) keysToRemove.add(key);
        }
        for (String key : keysToRemove) {
            state.getGlobalFlags().remove(key);
        }
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerStoryState pState = PlayerStoryState.getState(player);
            List<String> pKeys = new ArrayList<>();
            for (String key : pState.getFlags().keySet()) {
                if (key.startsWith(targetId + "_")) pKeys.add(key);
            }
            for (String key : pKeys) {
                pState.getFlags().remove(key);
            }
            if (targetId.equals("ch03")) {
                pState.setCh03EncounterCount(0);
                pState.setCh03RelocationCount(0);
            }
        }
        
        state.setActiveChapterId(targetId);
        
        Chapter target = ChapterRegistry.get(targetId);
        if (target != null) {
            target.onStart(server);
        }
    }
}
