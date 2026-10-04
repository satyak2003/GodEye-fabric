package com.carlo.story.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import java.util.Optional;
import java.util.UUID;

public record ScheduledEvent(String eventId, String eventType, long targetTick, String chapterId, Optional<UUID> playerUuid, String payload, boolean isCompleted) {
    public static final Codec<ScheduledEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("eventId").forGetter(ScheduledEvent::eventId),
        Codec.STRING.fieldOf("eventType").forGetter(ScheduledEvent::eventType),
        Codec.LONG.fieldOf("targetTick").forGetter(ScheduledEvent::targetTick),
        Codec.STRING.fieldOf("chapterId").forGetter(ScheduledEvent::chapterId),
        Uuids.INT_STREAM_CODEC.optionalFieldOf("playerUuid").forGetter(ScheduledEvent::playerUuid),
        Codec.STRING.optionalFieldOf("payload", "").forGetter(ScheduledEvent::payload),
        Codec.BOOL.optionalFieldOf("isCompleted", false).forGetter(ScheduledEvent::isCompleted)
    ).apply(instance, ScheduledEvent::new));
}
