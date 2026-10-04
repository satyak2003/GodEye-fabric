package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.MinecraftClient.class)
public class DeathScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void interceptDeathScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            // GodEye forces automatic respawn, no vanilla death screen!
            ci.cancel();
        }
    }
}
