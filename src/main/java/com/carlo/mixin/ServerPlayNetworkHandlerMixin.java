package com.carlo.mixin;

import com.carlo.story.PlayerStoryState;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow public void requestTeleport(double x, double y, double z, float yaw, float pitch) {}
    
    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    public void preventMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (PlayerStoryState.getState(this.player).getFlag("control_locked")) {
            if (packet.changesPosition()) {
                this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                ci.cancel();
            }
        }
    }
}
