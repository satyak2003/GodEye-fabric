package com.carlo.entity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class WatcherEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifespan = 20; // Default to 1 second (20 ticks)

    public WatcherEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true); // So it doesn't fall if spawned mid-air
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D);
    }

    // This method lets us tell the entity exactly how long to exist before vanishing
    public void setLifespan(int ticks) {
        this.lifespan = ticks;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            // If lifespan is -1, it lives forever!
            if (this.lifespan != -1) {
                this.lifespan--;
                if (this.lifespan <= 0) {
                    this.discard();
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            // IMPORTANT: Change "head_lift" to exactly what you named the animation in Blockbench!
            // Notice we use .thenPlay() instead of .thenLoop() so it lifts its head and stares.
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}