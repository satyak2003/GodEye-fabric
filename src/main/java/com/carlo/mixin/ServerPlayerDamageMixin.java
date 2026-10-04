package com.carlo.mixin;

import com.carlo.story.PlayerStoryState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(net.minecraft.server.world.ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        PlayerStoryState state = PlayerStoryState.getState(player);
        if (player.getEntityWorld().getTime() < state.getInvulnerabilityEndTick()) {
            // Is it a GodEye scripted damage source?
            // Vanilla sources usually don't have our custom names.
            // If the source name starts with "godeye", let it pass!
            if (!source.getName().startsWith("godeye")) {
                cir.setReturnValue(false);
            }
        }
    }
}
