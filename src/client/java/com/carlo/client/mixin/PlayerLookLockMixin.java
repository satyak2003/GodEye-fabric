package com.carlo.client.mixin;

import com.carlo.entity.WatcherEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerLookLockMixin {

    // This method intercepts mouse movement right before it rotates the camera
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void lockLookDuringJumpscare(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {

        if ((Object) this instanceof ClientPlayerEntity player) {

            // Draw a 2-block radius around the player and check if a Watcher is inside it
            boolean isJumpscareActive = !player.getWorld().getEntitiesByClass(
                    WatcherEntity.class,
                    player.getBoundingBox().expand(2.0),
                    watcher -> true
            ).isEmpty();

            // If the entity is right in front of them, cancel the mouse movement!
            if (isJumpscareActive) {
                ci.cancel();
            }
        }
    }
}