package com.carlo.item;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.constant.DataTickets;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.world.World.ExplosionSourceType;
import com.carlo.entity.GodEyeBossEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NightfallStaffItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public NightfallStaffItem(Settings settings) {
        super(settings);
    }

    // --- 1. ITEM USAGE MECHANICS ---

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000; // Allows the item to be held down infinitely
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW; // Gives the player the slow-walking "aiming" stance
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        net.minecraft.nbt.NbtCompound nbt = stack.getOrCreateNbt();
        int currentSouls = nbt.getInt("absorbed_souls");

        // IF THE PLAYER IS HOLDING SHIFT (SNEAKING)
        if (user.isSneaking()) {
            if (currentSouls >= 10) {

                if (!world.isClient) {
                    ServerWorld serverWorld = (ServerWorld) world;

                    // Scan a massive 100-block radius for the boss
                    Box searchBox = user.getBoundingBox().expand(100.0);
                    List<GodEyeBossEntity> bosses = world.getEntitiesByClass(GodEyeBossEntity.class, searchBox, entity -> entity.isAlive());

                    if (!bosses.isEmpty()) {
                        GodEyeBossEntity boss = bosses.get(0);

                        /// --- ENHANCED ORBITAL STRIKE VISUALS ---
                        net.minecraft.util.math.Vec3d targetPos = boss.getPos(); // Target the boss directly

                        // 1. Strike the exact position with real Lightning
                        LightningEntity lightning = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(serverWorld);
                        if (lightning != null) {
                            lightning.refreshPositionAfterTeleport(targetPos);
                            serverWorld.spawnEntity(lightning);
                        }

                        // 2. Play a deafening, bass-heavy Warden Sonic Boom sound combined with an explosion
                        serverWorld.playSound(null, net.minecraft.util.math.BlockPos.ofFloored(targetPos), net.minecraft.sound.SoundEvents.ENTITY_WARDEN_SONIC_BOOM, net.minecraft.sound.SoundCategory.PLAYERS, 4.0F, 0.5F);
                        serverWorld.playSound(null, net.minecraft.util.math.BlockPos.ofFloored(targetPos), net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE, net.minecraft.sound.SoundCategory.PLAYERS, 3.0F, 1.0F);

                        // 3. Draw a massive, dark cinematic pillar of Soul Fire and Sonic particles up to the sky
                        for(int y = 0; y < 40; y += 2) {
                            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                    targetPos.x, targetPos.y + y, targetPos.z,
                                    20, 1.5, 0.5, 1.5, 0.05); // Wide, chaotic flame spread

                            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                                    targetPos.x, targetPos.y + y, targetPos.z,
                                    1, 0.0, 0.0, 0.0, 0.0); // Shockwaves running up the beam
                        }

                        // Creates physical explosion damage in the world without breaking blocks
                        world.createExplosion(null, boss.getX(), boss.getY(), boss.getZ(), 3.0f, ExplosionSourceType.NONE);

                        // 4. Guaranteed 1 HP Drop (Fixed!)
                        boss.setHealth(1.0f); // Lock health to 1 FIRST so it cannot die
                        boss.damage(world.getDamageSources().outOfWorld(), 0.1f); // Tiny void damage to trigger the red flash!
                        user.sendMessage(net.minecraft.text.Text.literal("ORBITAL STRIKE DEPLOYED!").formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.BOLD), true);

                        nbt.putInt("absorbed_souls", 0);
                    }
                }

                return TypedActionResult.success(stack);
            } else {
                if (!world.isClient) {
                    user.sendMessage(net.minecraft.text.Text.literal("Not enough souls!").formatted(net.minecraft.util.Formatting.RED), true);
                }
                return TypedActionResult.fail(stack);
            }
        }

        // IF NOT SNEAKING, START THE SOUL VACUUM CHARGE (Normal Right Click)
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    // --- 2. SOUL ABSORBING LOGIC ---

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {

            // Trigger every 5 ticks while holding right-click
            if (remainingUseTicks % 5 == 0) {
                Box suckBox = user.getBoundingBox().expand(10.0);
                List<MobEntity> mobs = world.getEntitiesByClass(MobEntity.class, suckBox, entity -> true);

                // Get or create the NBT memory for the staff
                net.minecraft.nbt.NbtCompound nbt = stack.getOrCreateNbt();
                int currentSouls = nbt.getInt("absorbed_souls");

                for (MobEntity mob : mobs) {
                    // Check if the mob is currently alive before we hit it
                    if (mob instanceof GodEyeBossEntity) continue;
                    boolean wasAlive = mob.isAlive();

                    // A very gentle push just for visual effect, so they don't go flying
                    net.minecraft.util.math.Vec3d push = mob.getPos().subtract(user.getPos()).normalize().multiply(0.2).add(0, 0.2, 0);
                    mob.addVelocity(push.x, push.y, push.z);
                    mob.velocityModified = true;

                    // ONE-SHOT MASSIVE DAMAGE (100 damage guarantees an instant kill on standard mobs)
                    mob.damage(world.getDamageSources().magic(), 15.0f);

                    // Spawn particles
                    ((ServerWorld)world).spawnParticles(ParticleTypes.SOUL,
                            mob.getX(), mob.getY() + 1.0, mob.getZ(),
                            5, 0.5, 0.5, 0.5, 0.1);

                    // IF THE HIT KILLED THE MOB AND WE NEED SOULS
                    if (wasAlive && !mob.isAlive() && currentSouls < 10) {
                        currentSouls++;
                        nbt.putInt("absorbed_souls", currentSouls); // Save to item memory

                        // Send an action bar message to the player
                        player.sendMessage(net.minecraft.text.Text.literal("Souls Absorbed: " + currentSouls + "/10  Right Click to Absorb").formatted(net.minecraft.util.Formatting.AQUA), true);

                        // Play a sound when maxed out
                        if (currentSouls == 10) {
                            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                            // FIXED SERVER-SIDE BROADCAST
                            ((ServerWorld)world).playSound(null, player.getBlockPos(), com.carlo.Godeye.VOICE_CHANCE_EVENT, net.minecraft.sound.SoundCategory.MASTER, 5.0f, 1.0f);
                            player.sendMessage(net.minecraft.text.Text.literal("ORBITAL STRIKE READY! Shift + Right Mouse Button").formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.BOLD), true);
                        }
                    }
                }
            }
        }
    }



    // --- 3. GECKOLIB ANIMATION & RENDERING ---

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        // Handled via injection in GodeyeClient.java to bypass Fabric split-sources.
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {

            // Get the entity holding the staff
            Entity entity = event.getData(DataTickets.ENTITY);

            // If the entity is actively holding down right-click with this staff
            if (entity instanceof LivingEntity living && living.isUsingItem() && living.getActiveItem().getItem() == this) {
                // Play your custom vibrating crystal animation!
                return event.setAndContinue(RawAnimation.begin().thenLoop("crystal"));
            }

            // Otherwise, play nothing (or replace "STOP" with an "idle" animation if you make one)
            return software.bernie.geckolib.core.object.PlayState.STOP;
        }));


    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}