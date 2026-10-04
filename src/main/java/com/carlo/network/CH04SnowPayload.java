package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CH04SnowPayload(boolean enabled) implements CustomPayload {
    public static final Id<CH04SnowPayload> ID = new Id<>(Identifier.of("godeye", "ch04_snow"));
    public static final PacketCodec<RegistryByteBuf, CH04SnowPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeBoolean(value.enabled());
        },
        buf -> new CH04SnowPayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
