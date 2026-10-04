package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CameraLockPayload(boolean locked, int targetEntityId) implements CustomPayload {
    public static final Id<CameraLockPayload> ID = new Id<>(Identifier.of("godeye", "camera_lock"));
    public static final PacketCodec<RegistryByteBuf, CameraLockPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeBoolean(value.locked());
            buf.writeInt(value.targetEntityId());
        },
        buf -> new CameraLockPayload(buf.readBoolean(), buf.readInt())
    );
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
