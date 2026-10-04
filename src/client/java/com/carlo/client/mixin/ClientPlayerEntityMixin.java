package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void lockCameraToEntity(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        if (ClientStoryState.isCameraLocked && ClientStoryState.cameraLockTargetId != -1) {
            Entity target = player.getEntityWorld().getEntityById(ClientStoryState.cameraLockTargetId);
            if (target != null) {
                double dx = target.getX() - player.getX();
                double dy = target.getEyeY() - player.getEyeY();
                double dz = target.getZ() - player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                
                float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
                
                player.setYaw(targetYaw);
                player.setPitch(targetPitch);
                
                
            }
        }
    }
}

