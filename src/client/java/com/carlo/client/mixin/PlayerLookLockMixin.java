package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import com.carlo.entity.WatcherEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerLookLockMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void lockLookDuringJumpscare(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            if (ClientStoryState.isCameraLocked || ClientStoryState.isCinematicLocked) {
                ci.cancel();
                return;
            }
            
            boolean isJumpscareActive = !player.getEntityWorld().getEntitiesByClass(
                    WatcherEntity.class,
                    player.getBoundingBox().expand(2.0),
                    watcher -> true
            ).isEmpty();

            if (isJumpscareActive) {
                ci.cancel();
            }
        }
    }
}

