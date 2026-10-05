package com.carlo.mixin;

import com.carlo.story.PlayerStoryState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow private Vec3d requestedTeleportPos;
    
    @Unique private boolean godeye$loggedMoveRejection = false;
    
    @Inject(
        method = "onPlayerMove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    public void preventMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        // If there is an active teleport pending confirmation from the client, allow vanilla to process it
        if (this.requestedTeleportPos != null) {
            return;
        }

        boolean movementLocked = PlayerStoryState.getState(this.player).getFlag("control_locked") 
                              || PlayerStoryState.getState(this.player).getFlag("cinematic_locked");
        
        if (movementLocked) {
            if (packet.changesPosition()) {
                if (!this.godeye$loggedMoveRejection) {
                    System.out.println("[GodEye DEBUG] Movement packet rejected: cinematic lock active");
                    this.godeye$loggedMoveRejection = true;
                }
                
                // If camera is NOT locked, still allow rotation updates from the packet
                boolean cameraLocked = PlayerStoryState.getState(this.player).getFlag("camera_locked");
                if (!cameraLocked && packet.changesLook()) {
                    this.player.setAngles(packet.getYaw(this.player.getYaw()), packet.getPitch(this.player.getPitch()));
                }
                
                ci.cancel();
            }
        } else {
            this.godeye$loggedMoveRejection = false;
        }
    }
}
