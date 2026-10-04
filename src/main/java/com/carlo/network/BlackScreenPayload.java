package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BlackScreenPayload(boolean enabled) implements CustomPayload {
    public static final CustomPayload.Id<BlackScreenPayload> ID = new CustomPayload.Id<>(Identifier.of("godeye", "black_screen"));
    public static final PacketCodec<RegistryByteBuf, BlackScreenPayload> CODEC = PacketCodec.tuple(
        net.minecraft.network.codec.PacketCodecs.BOOLEAN, BlackScreenPayload::enabled,
        BlackScreenPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
