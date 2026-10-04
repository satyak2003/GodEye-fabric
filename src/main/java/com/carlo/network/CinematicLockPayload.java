package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CinematicLockPayload(boolean locked) implements CustomPayload {
    public static final Id<CinematicLockPayload> ID = new Id<>(Identifier.of("godeye", "cinematic_lock"));
    public static final PacketCodec<RegistryByteBuf, CinematicLockPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, CinematicLockPayload::locked, CinematicLockPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
