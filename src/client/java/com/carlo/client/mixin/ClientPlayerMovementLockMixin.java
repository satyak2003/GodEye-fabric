package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerMovementLockMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void lockMovement(CallbackInfo ci) {
        if (ClientStoryState.isControlLocked || ClientStoryState.isCinematicLocked) {
            ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
            player.input.playerInput = net.minecraft.util.PlayerInput.DEFAULT;
            player.setSprinting(false);
            player.forwardSpeed = 0.0f;
            player.sidewaysSpeed = 0.0f;
            player.upwardSpeed = 0.0f;
        }
    }
}
