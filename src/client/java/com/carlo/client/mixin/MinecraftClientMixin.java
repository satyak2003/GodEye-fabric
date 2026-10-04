package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
        @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preventAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ClientStoryState.isControlLocked || ClientStoryState.isCinematicLocked) cir.setReturnValue(false);
    }
    
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void preventUse(CallbackInfo ci) {
        if (ClientStoryState.isControlLocked || ClientStoryState.isCinematicLocked) ci.cancel();
    }
    
    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void preventPick(CallbackInfo ci) {
        if (ClientStoryState.isControlLocked || ClientStoryState.isCinematicLocked) ci.cancel();
    }
    
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void preventScreen(Screen screen, CallbackInfo ci) {
        if (ClientStoryState.isCinematicLocked && screen != null) {
            ci.cancel();
        } else if (ClientStoryState.isControlLocked && screen != null) {
            ci.cancel();
        }
    }
    
    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void preventInputEvents(CallbackInfo ci) {
        if (ClientStoryState.isCinematicLocked) {
            ci.cancel();
        }
    }
    }
