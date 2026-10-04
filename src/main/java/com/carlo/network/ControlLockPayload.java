package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ControlLockPayload(boolean locked) implements CustomPayload {
    public static final Id<ControlLockPayload> ID = new Id<>(Identifier.of("godeye", "control_lock"));
    public static final PacketCodec<RegistryByteBuf, ControlLockPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, ControlLockPayload::locked, ControlLockPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
