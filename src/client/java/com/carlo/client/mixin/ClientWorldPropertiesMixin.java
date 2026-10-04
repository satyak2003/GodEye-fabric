package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class ClientWorldPropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    public void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (ClientStoryState.nightFlashTicks > 0) {
            cir.setReturnValue(18000L); // Force visual midnight
        }
    }
}
