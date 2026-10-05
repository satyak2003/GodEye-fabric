package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class DeathScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void interceptDeathScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            // Send the respawn packet instantly instead of showing the screen
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket(net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            }
            ci.cancel();
        }
    }
}
