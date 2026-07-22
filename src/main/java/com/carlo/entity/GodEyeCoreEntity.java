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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.enchantment.Enchantments;

public class GodEyeCoreEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GodEyeCoreEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    // Set the Core's health to 1 so it shatters instantly
    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D); // So it doesn't get pushed around
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "core_controller", 0, event -> {
            // Must match the exact name from the JSON!
            return event.setAndContinue(RawAnimation.begin().thenLoop("floties"));
        }));
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        super.onDeath(damageSource);
        World world = this.getWorld();

        if (!world.isClient && world instanceof ServerWorld serverWorld) {

            BlockPos pos = this.getBlockPos();

            // 1. Delete the maze and build the Obsidian walls
            // Trigger the animated arena sequence in the main tick loop
            com.carlo.Godeye.arenaCenter = pos;
            com.carlo.Godeye.arenaTransformLayer = 0;
            com.carlo.Godeye.isTransformingArena = true;

            // 2. Scan the arena (50 block radius) for ALL players
            java.util.List<PlayerEntity> players = serverWorld.getEntitiesByClass(
                    PlayerEntity.class,
                    this.getBoundingBox().expand(50.0),
                    player -> true
            );

            // Equip every player in the arena and lock their spawn points
            // Equip every player in the arena and lock their spawn points
            for (PlayerEntity arenaPlayer : players) {
                equipBossFightGear(arenaPlayer);

                // We must cast the base PlayerEntity to a ServerPlayerEntity to set the spawn!
                if (arenaPlayer instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.setSpawnPoint(serverWorld.getRegistryKey(), pos, 0.0f, true, false);
                }
            }

            // 3. Play a deafening thunder sound
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, net.minecraft.sound.SoundCategory.MASTER, 2.0f, 0.5f);

            // 4. Spawn the Boss high up in the air (Y + 15) so it starts hovering
            GodEyeBossEntity boss = com.carlo.Godeye.GODEYE_BOSS.create(serverWorld);
            if (boss != null) {
                boss.refreshPositionAndAngles(this.getX(), this.getY() + 15.0, this.getZ(), this.getYaw(), this.getPitch());
                boss.setPersistent(); // <-- THIS PREVENTS DESPAWNING
                serverWorld.spawnEntity(boss);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, net.minecraft.entity.damage.DamageSource damageSource) {
        return false; // The Core will no longer shatter from hitting the ground
    }

    public static void equipBossFightGear(PlayerEntity player) {
        player.getInventory().clear();

        // Helper to quickly enchant items
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 4);
        helmet.addEnchantment(Enchantments.UNBREAKING, 3);

        ItemStack chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chest.addEnchantment(Enchantments.PROTECTION, 4);
        chest.addEnchantment(Enchantments.UNBREAKING, 3);

        ItemStack legs = new ItemStack(Items.DIAMOND_LEGGINGS);
        legs.addEnchantment(Enchantments.PROTECTION, 4);
        legs.addEnchantment(Enchantments.UNBREAKING, 3);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 4);
        boots.addEnchantment(Enchantments.FEATHER_FALLING, 4);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.addEnchantment(Enchantments.SHARPNESS, 5);
        sword.addEnchantment(Enchantments.UNBREAKING, 3);

        // Equip Armor & Weapons
        player.equipStack(EquipmentSlot.HEAD, helmet);
        player.equipStack(EquipmentSlot.CHEST, chest);
        player.equipStack(EquipmentSlot.LEGS, legs);
        player.equipStack(EquipmentSlot.FEET, boots);
        player.equipStack(EquipmentSlot.MAINHAND, sword);
        player.equipStack(EquipmentSlot.OFFHAND, new ItemStack(com.carlo.Godeye.NIGHTFALL_STAFF));

        player.getInventory().insertStack(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 5));
        player.getInventory().insertStack(new ItemStack(Items.GOLDEN_APPLE, 16));
    }
    // Helper 2: Deletes the maze and raises the Obsidian walls
    public static void transformArena(ServerWorld world, BlockPos center) {
        int radius = 25; // Assuming a 50x50 arena. Adjust this to your arena size!

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 20; y++) { // 20 blocks high
                    BlockPos current = center.add(x, y, z);

                    // If it is the outer edge of the radius, build an Obsidian wall
                    if (x == -radius || x == radius || z == -radius || z == radius) {
                        world.setBlockState(current, Blocks.OBSIDIAN.getDefaultState());
                    }
                    // Otherwise, if it is inside the arena and is a Maze Wall, delete it!
                    // (Change Blocks.BLACKSTONE to whatever block your maze is made of)
                    else if (world.getBlockState(current).isOf(Blocks.BLACKSTONE)) {
                        world.setBlockState(current, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }
}