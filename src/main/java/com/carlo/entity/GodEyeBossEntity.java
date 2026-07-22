package com.carlo.entity;

import com.carlo.Godeye;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;


public class GodEyeBossEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Custom timer for the cinematic explosion!
    public int deathTicks = 0;
    public int skullCooldown = 0;
    public int beamHits = 0; // Tracks consecutive beam attacks
    public boolean isRainingSkulls = false;
    public int skullRainTicks = 0; // Timer for the AOE phase
    public int mobSpawnCooldown = 0;


    public GodEyeBossEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D);
    }

    // --- ALTITUDE AND HOVER LOGIC ---
    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient && this.isAlive()) {

            // PHASE 1 & 2: Health is high, hover and attack!
            if (this.getHealth() > 1.0f) {

                // --- NEW: CONTINUOUS MOB SPAWNING ---
                this.mobSpawnCooldown--;
                if (this.mobSpawnCooldown <= 0) {
                    ServerWorld serverWorld = (ServerWorld) this.getWorld();
                    // Count existing mobs in the arena (40 block radius)
                    Box arenaBox = this.getBoundingBox().expand(40.0);
                    long mobCount = serverWorld.getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class, arenaBox, entity -> entity != this).size();

                    if (mobCount < 15) { // Maintain at least 15 mobs in the arena
                        double spawnX = this.getX() + (this.random.nextDouble() - 0.5) * 30;
                        double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5) * 30;
                        net.minecraft.util.math.BlockPos spawnPos = new net.minecraft.util.math.BlockPos((int)spawnX, 64, (int)spawnZ);

                        if (serverWorld.getBlockState(spawnPos).isAir()) {
                            net.minecraft.entity.mob.MobEntity minion = this.random.nextBoolean() ?
                                    net.minecraft.entity.EntityType.WITHER_SKELETON.create(serverWorld) :
                                    net.minecraft.entity.EntityType.ZOMBIE.create(serverWorld);

                            if (minion != null) {
                                minion.refreshPositionAndAngles(spawnX, 64, spawnZ, 0, 0);
                                serverWorld.spawnEntity(minion);
                                serverWorld.spawnParticles(ParticleTypes.SOUL, spawnX, 65, spawnZ, 10, 0.5, 0.5, 0.5, 0.1);
                            }
                        }
                    }
                    this.mobSpawnCooldown = 60; // Check and spawn every 3 seconds
                }

                // --- PHASE 2.5: Wither Skull Rain ---
                if (this.isRainingSkulls) {
                    this.skullRainTicks++;
                    this.setVelocity(0, 0, 0); // Lock in place while channeling the rain

                    // Initial Scream
                    if (this.skullRainTicks == 1) {
                        this.playSound(net.minecraft.sound.SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 5.0F, 0.5F);
                    }

                    // Rain a Wither Skull every 5 ticks for 5 seconds (100 ticks)
                    if (this.skullRainTicks % 5 == 0 && this.skullRainTicks < 100) {
                        ServerWorld serverWorld = (ServerWorld) this.getWorld();
                        net.minecraft.entity.LivingEntity target = this.getTarget();

                        if (target != null) {
                            double dropX = target.getX() + (this.random.nextDouble() - 0.5) * 20;
                            double dropZ = target.getZ() + (this.random.nextDouble() - 0.5) * 20;
                            double dropY = target.getY() + 30.0;

                            // --- NEW: THE SCORCHED EARTH WITHER SKULL ---
                            // By using an anonymous class, we can override the impact without a custom entity registry!
                            net.minecraft.entity.projectile.WitherSkullEntity skull = new net.minecraft.entity.projectile.WitherSkullEntity(this.getWorld(), this, 0, -1.0, 0) {
                                @Override
                                protected void onCollision(net.minecraft.util.hit.HitResult hitResult) {
                                    super.onCollision(hitResult);
                                    if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld sw) {
                                        net.minecraft.util.math.BlockPos pos = net.minecraft.util.math.BlockPos.ofFloored(hitResult.getPos());

                                        // Create a crater of Netherrack and Fire
                                        for (int x = -1; x <= 1; x++) {
                                            for (int z = -1; z <= 1; z++) {
                                                net.minecraft.util.math.BlockPos targetBlock = pos.add(x, -1, z);
                                                // Only replace solid blocks, don't overwrite air
                                                if (!sw.getBlockState(targetBlock).isAir() && sw.getBlockState(targetBlock).getHardness(sw, targetBlock) >= 0) {
                                                    if (sw.random.nextFloat() < 0.6f) sw.setBlockState(targetBlock, net.minecraft.block.Blocks.NETHERRACK.getDefaultState());
                                                    if (sw.random.nextBoolean() && sw.getBlockState(targetBlock.up()).isAir()) sw.setBlockState(targetBlock.up(), net.minecraft.block.Blocks.FIRE.getDefaultState());
                                                }
                                            }
                                        }
                                    }
                                }
                            };

                            skull.setPosition(dropX, dropY, dropZ);
                            skull.setOnFire(true);
                            serverWorld.spawnEntity(skull);
                        }
                    }

                    if (this.skullRainTicks >= 100) {
                        this.isRainingSkulls = false;
                        this.skullRainTicks = 0;
                        this.beamHits = 0;
                    }
                }

                // --- PHASE 2.0: Abyssal Laser Attack ---
                // ... (Keep your exact existing Abyssal Laser Attack code here) ...

                // --- PHASE 2.0: Abyssal Laser Attack ---
                else {
                    // Standard hovering logic
                    if (!this.getWorld().getBlockState(this.getBlockPos().down(15)).isAir()) {
                        this.setVelocity(0, 0.1, 0);
                    } else {
                        this.setVelocity(0, 0, 0);
                    }

                    net.minecraft.entity.LivingEntity target = this.getTarget();
                    if (target != null) {
                        this.skullCooldown--;

                        if (this.skullCooldown <= 0) {
                            ServerWorld serverWorld = (ServerWorld) this.getWorld();
                            net.minecraft.util.math.Vec3d startPos = this.getPos().add(0, 2.0, 0);
                            net.minecraft.util.math.Vec3d targetPos = target.getEyePos();

                            net.minecraft.util.math.Vec3d direction = targetPos.subtract(startPos).normalize();
                            double distance = startPos.distanceTo(targetPos);

                            // Custom heavy laser sound
                            this.playSound(net.minecraft.sound.SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 2.0F, 1.5F);
                            this.playSound(net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, 3.0F, 2.0F);

                            // The Custom Abyssal Laser Visuals
                            for (int i = 1; i < distance; i += 1) {
                                net.minecraft.util.math.Vec3d particlePos = startPos.add(direction.multiply(i));
                                // Inner white core
                                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                                        particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
                                // Outer blue soul fire shell
                                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,
                                        particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.01);
                            }

                            // Hit logic
                            target.damage(this.getWorld().getDamageSources().magic(), 8.0f);
                            target.addVelocity(direction.x * 1.5, 0.5, direction.z * 1.5);
                            target.velocityModified = true;

                            // Trigger Wither Rain check
                            this.beamHits++;
                            if (this.beamHits >= 5) {
                                this.isRainingSkulls = true;
                                this.skullRainTicks = 0;
                            }

                            this.skullCooldown = 60; // Reset laser timer
                        }
                    }
                }
            }
            // PHASE 3: Orbital Strike hit (1 HP), float down to the player!
            else {
                if (this.getWorld().getBlockState(this.getBlockPos().down(2)).isAir()) {
                    this.setVelocity(0, -0.15, 0);
                } else {
                    this.setVelocity(0, 0, 0);
                }
            }
        }
    }

    // --- CINEMATIC DEATH SEQUENCE ---
    @Override
    protected void updatePostDeath() {
        this.deathTicks++;

        if (this.getWorld() instanceof ServerWorld serverWorld) {

            // --- 1. THE BUILD-UP (Screen Warp) ---
            if (this.deathTicks == 1) {
                this.setVelocity(0, 0, 0);
                serverWorld.playSound(null, this.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE, net.minecraft.sound.SoundCategory.HOSTILE, 5.0f, 0.5f);

                // Apply severe Nausea and Darkness to all nearby players to warp the screen
                Box area = this.getBoundingBox().expand(50.0);
                for (PlayerEntity player : serverWorld.getEntitiesByClass(PlayerEntity.class, area, p -> true)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 1)); // Wobbles the camera
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0)); // Pulses pitch black
                }
            }

            // Violent body explosions (Upgraded from standard EXPLOSION to EXPLOSION_EMITTER)
            if (this.deathTicks % 5 == 0 && this.deathTicks < 60) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                        this.getX() + (this.random.nextDouble() - 0.5) * 4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 4 + 2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 4,
                        1, 0, 0, 0, 0);
                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F, 1.0F);
            }

            // --- 2. THE GRAND FINALE (Delayed to tick 60) ---
            if (this.deathTicks == 60) {

                // Massive Sonic Boom Shockwave
                serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 2, this.getZ(), 1, 0, 0, 0, 0);

                // Huge Blinding Flash
                serverWorld.spawnParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2, this.getZ(), 50, 4.0, 4.0, 4.0, 0.0);

                // The "Abnormal" Screen Effect (Flashes the Elder Guardian ghost on the player's screen)
                serverWorld.spawnParticles(ParticleTypes.ELDER_GUARDIAN, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);

                // Play your new custom audio!
                this.playSound(com.carlo.Godeye.BOSS_DEATH_EVENT, 10.0F, 1.0F);

                // --- 3. THE FALLEN FROSTS ---
                // Find the closest player to center the ring of Frosts over them
                PlayerEntity player = serverWorld.getClosestPlayer(this, 50);
                if (player != null) {
                    for (int i = 0; i < 360; i += 45) { // Spawns 8 Frosts in a perfect circle
                        double angle = Math.toRadians(i);
                        double xOffset = 15 * Math.cos(angle); // 15 block radius
                        double zOffset = 15 * Math.sin(angle);

                        // CREATE THE DEAD FROST ENTITIES
                        var frost = Godeye.GODEYE_WATCHER.create(serverWorld);
                        if (frost != null) {
                            // Teleport them directly above the player's head in a circle
                            frost.refreshPositionAndAngles(player.getX() + xOffset, player.getY() + 5, player.getZ() + zOffset, 0, 0);
                            frost.setHealth(0.0f); // Instantly kill them so they play their death animation and drop
                            serverWorld.spawnEntity(frost);
                        }
                    }
                }

                // --- 4. THE ESCAPE PORTAL & COLLAPSE TRIGGER ---
                net.minecraft.util.math.BlockPos referencePos = com.carlo.Godeye.arenaCenter != null ? com.carlo.Godeye.arenaCenter : this.getBlockPos();

                // FORCE the Y-coordinate to 64 so it always spawns perfectly embedded in the floor!
                net.minecraft.util.math.BlockPos portalCenter = new net.minecraft.util.math.BlockPos(referencePos.getX(), 64, referencePos.getZ());

                // Clear ALL status effects so the player can see and sprint to the exit
                Box area = this.getBoundingBox().expand(50.0);
                for (PlayerEntity p : serverWorld.getEntitiesByClass(PlayerEntity.class, area, entity -> true)) {
                    p.clearStatusEffects();
                }

                // Build a 5x5 Crying Obsidian frame with an empty 3x3 center
                for(int px = -2; px <= 2; px++) {
                    for(int pz = -2; pz <= 2; pz++) {
                        net.minecraft.util.math.BlockPos framePos = portalCenter.add(px, 0, pz);
                        if (Math.abs(px) == 2 || Math.abs(pz) == 2) {
                            serverWorld.setBlockState(framePos, net.minecraft.block.Blocks.CRYING_OBSIDIAN.getDefaultState());
                        } else {
                            serverWorld.setBlockState(framePos, net.minecraft.block.Blocks.AIR.getDefaultState());
                        }
                    }
                }

                serverWorld.playSound(null, portalCenter, com.carlo.Godeye.VOICE_RUN_EVENT, net.minecraft.sound.SoundCategory.HOSTILE, 6.0f, 1.0f);

                com.carlo.Godeye.isDimensionCollapsing = true;
                com.carlo.Godeye.collapseTicks = 0;

                this.remove(RemovalReason.KILLED);
            }
        }
    }

    // --- DAMAGE IMMUNITY SHIELDS ---
    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (source.getTypeRegistryEntry().matchesKey(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
            return super.damage(source, amount);
        }
        if (this.getHealth() <= 1.0f && source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity) {
            return super.damage(source, amount);
        }
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, net.minecraft.entity.damage.DamageSource damageSource) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ring_controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("rings_anim"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        // Tells the boss to actively target players
        this.targetSelector.add(1, new net.minecraft.entity.ai.goal.ActiveTargetGoal<>(this, net.minecraft.entity.player.PlayerEntity.class, true));
        // Tells the boss to look at whatever it is targeting
        this.goalSelector.add(2, new net.minecraft.entity.ai.goal.LookAtEntityGoal(this, net.minecraft.entity.player.PlayerEntity.class, 64.0F));
    }
}