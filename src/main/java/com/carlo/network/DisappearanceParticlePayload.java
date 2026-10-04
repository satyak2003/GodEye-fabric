package com.carlo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DisappearanceParticlePayload(double x, double y, double z, boolean isWitness) implements CustomPayload {
    public static final Id<DisappearanceParticlePayload> ID = new Id<>(Identifier.of("godeye", "disappearance_particle"));
    public static final PacketCodec<RegistryByteBuf, DisappearanceParticlePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.DOUBLE, DisappearanceParticlePayload::x,
        PacketCodecs.DOUBLE, DisappearanceParticlePayload::y,
        PacketCodecs.DOUBLE, DisappearanceParticlePayload::z,
        PacketCodecs.BOOLEAN, DisappearanceParticlePayload::isWitness,
        DisappearanceParticlePayload::new
    );
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
