package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Input.class)
public class InputMixin {
    @Shadow public PlayerInput playerInput;
    @Shadow protected Vec2f movementVector;

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(CallbackInfo ci) {
                if (ClientStoryState.isControlLocked || ClientStoryState.isCinematicLocked) {
            this.playerInput = PlayerInput.DEFAULT;
            this.movementVector = Vec2f.ZERO;
        }
    }
}

