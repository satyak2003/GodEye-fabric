package com.carlo.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FrostEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int vanishTickCounter = 0;
    private static final int GRACE_PERIOD_TICKS = 60; // 3 seconds

        public FrostEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean damage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // ANIMATION DISABLED FOR DIAGNOSTICS: controllers.add(...)
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.getEntityWorld().isClient()) {
            PlayerEntity player = this.getEntityWorld().getClosestPlayer(this, 100.0);
            if (player != null) {
                // Face the player
                this.lookAtEntity(player, 360.0F, 360.0F);
                
                vanishTickCounter++;
                boolean shouldLog = (vanishTickCounter % 10 == 0);
                
                // Grace Period
                if (this.age < GRACE_PERIOD_TICKS) {
                    if (shouldLog) {
                        System.out.println("[Frost DEBUG] Vanish logic paused: GRACE PERIOD ACTIVE (" + this.age + "/" + GRACE_PERIOD_TICKS + ")");
                    }
                    return; // Skip vanish logic
                }
                
                // Vanish logic
                double distance = Math.sqrt(this.squaredDistanceTo(player));
                if (distance < 63.0) { // < 4000 squared -> actually sqrt(4000) is 63.24
                    // Vector from player eyes to Frost center
                    Vec3d toFrost = this.getBoundingBox().getCenter().subtract(player.getEyePos()).normalize();
                    Vec3d lookVec = player.getRotationVec(1.0F);
                    double dot = lookVec.dotProduct(toFrost);
                    
                    if (shouldLog) {
                        System.out.println("[Frost DEBUG] Evaluating vanish. Distance: " + distance + ", Dot: " + dot);
                    }
                    
                    if (dot > 0.8) {
                        // Check Line of Sight
                        RaycastContext context = new RaycastContext(
                            player.getEyePos(),
                            this.getBoundingBox().getCenter(),
                            RaycastContext.ShapeType.OUTLINE,
                            RaycastContext.FluidHandling.NONE,
                            player
                        );
                        HitResult result = this.getEntityWorld().raycast(context);
                        boolean hasLOS = (result.getType() == HitResult.Type.MISS);
                        
                        if (shouldLog) {
                            System.out.println("[Frost DEBUG] Raycast hit type: " + result.getType());
                        }
                        
                        if (hasLOS) {
											                            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                                com.carlo.story.GlobalWorldState state = com.carlo.story.GlobalWorldState.getServerState(((net.minecraft.server.world.ServerWorld)serverPlayer.getEntityWorld()).getServer());
                                if (!"ch04".equals(state.getActiveChapterId())) {
                                    com.carlo.story.system.SoundHelper.playToPlayer(serverPlayer, com.carlo.Godeye.FROST_VANISH_EVENT, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                                }
                            }
											System.out.println("[Frost SOUND DEBUG] Vanish sound requested: godeye:frost_vanish");
											System.out.println("[Frost DEBUG] VANISH CONDITION MET");
											this.discard();
									} else if (shouldLog) {
                            System.out.println("[Frost DEBUG] VANISH CONDITION NOT MET (Blocked LOS)");
                        }
                    } else if (shouldLog) {
                        System.out.println("[Frost DEBUG] VANISH CONDITION NOT MET (Dot product too low)");
                    }
                }
            }
        }
    }
}





