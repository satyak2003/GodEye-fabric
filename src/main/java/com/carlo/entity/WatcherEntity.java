package com.carlo.entity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public class WatcherEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifespan = 20; // Default to 1 second (20 ticks)

    public WatcherEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true); // So it doesn't fall if spawned mid-air
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 1.0D);
    }

    // This method lets us tell the entity exactly how long to exist before vanishing
    public void setLifespan(int ticks) {
        this.lifespan = ticks;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient()) {
            if (this.lifespan != -1) {
                this.lifespan--;
                if (this.lifespan <= 0) {
                    for (net.minecraft.server.network.ServerPlayerEntity p : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p, new com.carlo.network.DisappearanceParticlePayload(this.getX(), this.getY(), this.getZ(), false));
                    }
                    this.discard();
                }
            }
            
            // Update approximately every 5 ticks for efficiency, as requested
            if (this.age % 5 == 0) {
                PlayerEntity nearest = this.getEntityWorld().getClosestPlayer(this, 100.0);
                if (nearest != null) {
                    double dx = nearest.getX() - this.getX();
                    double dz = nearest.getZ() - this.getZ();
                    float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                    
                    this.setYaw(targetYaw);
                    this.setBodyYaw(targetYaw);
                    this.setHeadYaw(targetYaw);
                    
                    double dy = nearest.getEyeY() - this.getEyeY();
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
                    this.setPitch(targetPitch);
                    
                    // Force a packets sync of the head and body yaw to all tracking clients
                    net.minecraft.server.world.ServerWorld sw = (net.minecraft.server.world.ServerWorld) this.getEntityWorld();
                    sw.getChunkManager().sendToNearbyPlayers(this, new net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket(this, (byte) (targetYaw * 256.0F / 360.0F)));
                    // The tracker will handle body yaw, but we can also forcefully teleport slightly in place to sync
                    
                }
            }
        }
    }

            @Override
    public boolean damage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource source, float amount) {
        return false; // Immune to everything
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource damageSource) {
        return true;
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void checkDespawn() {
        // Prevent natural despawning so it doesn't vanish randomly at distance
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}




