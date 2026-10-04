package com.carlo.client.mixin;

import com.carlo.client.ClientStoryState;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class ClientBiomeMixin {
    @Inject(method = "hasPrecipitation", at = @At("HEAD"), cancellable = true)
    private void godeye_hasPrecipitation(CallbackInfoReturnable<Boolean> cir) {
        if (ClientStoryState.ch04Snow) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isCold", at = @At("HEAD"), cancellable = true)
    private void godeye_isCold(BlockPos pos, int seaLevel, CallbackInfoReturnable<Boolean> cir) {
        if (ClientStoryState.ch04Snow) {
            cir.setReturnValue(true);
        }
    }
}

