package com.carlo.mixin;

import com.carlo.util.Ch04Freezable;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin implements Ch04Freezable {
    @Unique
    public boolean godeyeCh04Frozen = false;
    
    @Override
    public void setCh04Frozen(boolean frozen) {
        this.godeyeCh04Frozen = frozen;
    }
    
    @Override
    public boolean isCh04Frozen() {
        return this.godeyeCh04Frozen;
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void godeye_freezeTick(CallbackInfo ci) {
        if (godeyeCh04Frozen) {
            MobEntity mob = (MobEntity)(Object)this;
            mob.getNavigation().stop();
            mob.setTarget(null);
            mob.setJumping(false);
            mob.setForwardSpeed(0);
            mob.setUpwardSpeed(0);
            mob.setSidewaysSpeed(0);
            mob.setMovementSpeed(0);
        }
    }
    
    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void godeye_cancelAi(CallbackInfo ci) {
        if (godeyeCh04Frozen) {
            ci.cancel();
        }
    }
}
