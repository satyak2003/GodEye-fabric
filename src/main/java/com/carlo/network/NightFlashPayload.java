package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NightFlashPayload(int durationTicks) implements CustomPayload {
    public static final Id<NightFlashPayload> ID = new Id<>(Identifier.of("godeye", "night_flash"));
    public static final PacketCodec<RegistryByteBuf, NightFlashPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, NightFlashPayload::durationTicks, NightFlashPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
