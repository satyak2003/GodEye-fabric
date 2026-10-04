package com.carlo.story;
import java.util.HashMap;
import java.util.Map;

public class ChapterRegistry {
    private static final Map<String, Chapter> CHAPTERS = new HashMap<>();
    public static void register(Chapter chapter) {
        CHAPTERS.put(chapter.getId(), chapter);
    }
    public static Chapter get(String id) {
        return CHAPTERS.get(id);
    }
}
