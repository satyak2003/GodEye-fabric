package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CinematicTextPayload(int textStage) implements CustomPayload {
    public static final CustomPayload.Id<CinematicTextPayload> ID = new CustomPayload.Id<>(Identifier.of("godeye", "cinematic_text"));
    public static final PacketCodec<RegistryByteBuf, CinematicTextPayload> CODEC = PacketCodec.tuple(
        net.minecraft.network.codec.PacketCodecs.INTEGER, CinematicTextPayload::textStage,
        CinematicTextPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
