package com.carlo;

import com.carlo.item.NightfallStaffItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.util.Rarity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import com.carlo.entity.GodEyeCoreEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.carlo.entity.GodEyeBossEntity;
import com.carlo.entity.WatcherEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.ItemEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

public class Godeye implements ModInitializer {
	public static final String MOD_ID = "godeye";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean isTransformingArena = false;
	public static int arenaTransformLayer = 0;
	public static BlockPos arenaCenter = null;
	public static boolean isDimensionCollapsing = false;
	public static int collapseTicks = 0;
	public static final int MAX_COLLAPSE_TICKS = 1200; // Exactly 60 seconds to escape


	public static final RegistryKey<World> GODEYE_DIMENSION_KEY = RegistryKey.of(
			RegistryKeys.WORLD,
			new Identifier(MOD_ID, "godeye_domain")
	);

	public static final Identifier BOSS_DEATH_ID = new Identifier(MOD_ID, "boss_death");
	public static final net.minecraft.sound.SoundEvent BOSS_DEATH_EVENT = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SOUND_EVENT,
			BOSS_DEATH_ID,
			net.minecraft.sound.SoundEvent.of(BOSS_DEATH_ID)
	);

	public static final Identifier VOICE_CHANCE_ID = new Identifier(MOD_ID, "boss_chance");
	public static final net.minecraft.sound.SoundEvent VOICE_CHANCE_EVENT = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SOUND_EVENT,
			VOICE_CHANCE_ID,
			net.minecraft.sound.SoundEvent.of(VOICE_CHANCE_ID)
	);

	public static final Identifier VOICE_RUN_ID = new Identifier(MOD_ID, "boss_run");
	public static final net.minecraft.sound.SoundEvent VOICE_RUN_EVENT = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SOUND_EVENT,
			VOICE_RUN_ID,
			net.minecraft.sound.SoundEvent.of(VOICE_RUN_ID)
	);

	public static final Identifier JUMPSCARE_ID = new Identifier(MOD_ID, "jumpscare");
	public static final net.minecraft.sound.SoundEvent JUMPSCARE_EVENT = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SOUND_EVENT,
			JUMPSCARE_ID,
			net.minecraft.sound.SoundEvent.of(JUMPSCARE_ID)
	);

	public static final Identifier VOICE_YOURS_ID = new Identifier(MOD_ID, "your_world");
	public static final net.minecraft.sound.SoundEvent VOICE_YOURS_EVENT = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SOUND_EVENT,
			VOICE_YOURS_ID,
			net.minecraft.sound.SoundEvent.of(VOICE_YOURS_ID)
	);

	// Change this line in your Godeye.java
	public static final NightfallStaffItem NIGHTFALL_STAFF = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "nightfall_staff"),
			new NightfallStaffItem(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC).fireproof())
	);

	public static final EntityType<GodEyeCoreEntity> GODEYE_CORE = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(MOD_ID, "godeye_core"),
			FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GodEyeCoreEntity::new)
					.dimensions(EntityDimensions.fixed(3.0f, 3.0f)).build()
	);

	public static final EntityType<GodEyeBossEntity> GODEYE_BOSS = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(MOD_ID, "godeye_boss"),
			FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GodEyeBossEntity::new)
					.dimensions(EntityDimensions.fixed(3.0f, 3.0f)).build()
	);

	public static final EntityType<WatcherEntity> GODEYE_WATCHER = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(MOD_ID, "godeye_watcher"),
			FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WatcherEntity::new)
					.dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
	);

	private static final Map<UUID, Integer> playerTicks = new HashMap<>();
	public static final Set<UUID> WAITING_FOR_BRIDGE = new HashSet<>();

	private static boolean isBuildingBridge = false;
	private static int bridgeProgressZ = 3;

	@Override
	public void onInitialize() {

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {

			// Check if the player is respawning inside your custom Boss Dimension
			// (Replace "godeye:gods_domain" with your actual dimension identifier)
			if (newPlayer.getWorld().getRegistryKey().getValue().toString().equals("godeye:gods_domain")) {

				// Give them the boss gear back instantly upon respawning
				// (Call the method we made in Step 1. Adjust the class path if necessary)
				com.carlo.entity.GodEyeCoreEntity.equipBossFightGear(newPlayer);

				// Optional: Give them a few seconds of resistance so they don't get spawn-killed
				newPlayer.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 100, 4));
			}
		});
		LOGGER.info("The GodEye is watching...");
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
			content.add(NIGHTFALL_STAFF);
		});

		FabricDefaultAttributeRegistry.register(GODEYE_CORE, GodEyeCoreEntity.setAttributes());
		FabricDefaultAttributeRegistry.register(GODEYE_BOSS, GodEyeBossEntity.setAttributes());
		FabricDefaultAttributeRegistry.register(GODEYE_WATCHER, WatcherEntity.setAttributes());
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			UUID id = player.getUuid();
			int ticks = playerTicks.getOrDefault(id, 0) + 1;
			playerTicks.put(id, ticks);

			if (ticks == 1200) sendDistortedMessage(player, 0);
			else if (ticks == 2400) spawnLoreShrine(player);
			else if (ticks == 3600) sendDistortedMessage(player, 1);
			else if (ticks == 4800) sendDistortedMessage(player, 2);
			else if (ticks == 6000) sendDistortedMessage(player, 3);
			else if (ticks == 7200) {
				sendDistortedMessage(player, 4);
				triggerNightfall(player.getServerWorld());
			}
			else if (ticks == 8400) triggerAbduction(player);

			if (ticks >= 2400 && ticks < 8400) {
				triggerDecay(player, ticks);

				if (ticks >= 6000) {
					ServerWorld world = player.getServerWorld();

					if (world.random.nextInt(60) == 0) {
						triggerStalker(player);
					}

					if (world.random.nextInt(150) == 0) {
						triggerInFaceJumpscare(player);
					}
				}
			}

			// --- THE VOID PLATFORM SEQUENCE ---
			if (WAITING_FOR_BRIDGE.contains(id)) {
				ServerWorld world = player.getServerWorld();

				if (!player.hasStatusEffect(StatusEffects.DARKNESS)) {
					// Give it a 30-second duration (600 ticks) so it can do its heartbeat fade
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 600, 0, false, false, false));
				}

				// BUG FIX 1: Massive bounding box so the book never leaves tracking range
				Box voidBox = new Box(-50, -50, -50, 50, 100, 50);
				List<ItemEntity> fallingItems = world.getEntitiesByClass(
						ItemEntity.class,
						voidBox,
						item -> item.getStack().isOf(Items.WRITTEN_BOOK)
				);

				for (ItemEntity droppedBook : fallingItems) {
					if (droppedBook.getY() < -5) {

						droppedBook.discard();
						WAITING_FOR_BRIDGE.remove(id);
						player.removeStatusEffect(StatusEffects.DARKNESS);

						List<WatcherEntity> frosts = world.getEntitiesByClass(
								WatcherEntity.class,
								player.getBoundingBox().expand(100.0),
								entity -> true
						);
						for (WatcherEntity frost : frosts) {
							frost.discard();
						}

						world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.MASTER, 1.0f, 0.8f);
						isBuildingBridge = true;
						bridgeProgressZ = 3; // Reset progress explicitly
						break;
					}
				}
			}
		}

		ServerWorld godEyeWorld = server.getWorld(GODEYE_DIMENSION_KEY);
		if (godEyeWorld != null) {
			if (isBuildingBridge && bridgeProgressZ < 75) {
				buildBridgeTick(godEyeWorld, server.getTicks());
			} else if (isBuildingBridge && bridgeProgressZ >= 75) {
				isBuildingBridge = false;

				generateConcentricMaze(godEyeWorld);

				for (ServerPlayerEntity p : godEyeWorld.getPlayers()) {
					giveMazeGear(p);
					p.playSound(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 1.0f, 0.5f);
				}
			}
		}

		// --- THE ARENA TRANSFORMATION SEQUENCE ---
		if (isTransformingArena && arenaCenter != null) {
//			ServerWorld godEyeWorld = server.getWorld(GODEYE_DIMENSION_KEY);
			if (godEyeWorld != null) {

				int radius = 35; // Bigger arena!
				int floorY = 63; // The Y level the player walks on

				// LAYER 0: Instant floor expansion & maze deletion
				if (arenaTransformLayer == 0) {
					for (int x = -radius; x <= radius; x++) {
						for (int z = -radius; z <= radius; z++) {
							// 1. Expand the solid floor
							godEyeWorld.setBlockState(new BlockPos(arenaCenter.getX() + x, floorY, arenaCenter.getZ() + z), Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState());

							// 2. NUKE EVERYTHING ABOVE THE FLOOR (Up to Y+30 to catch tall altars)
							for (int y = floorY + 1; y <= floorY + 30; y++) {
								godEyeWorld.setBlockState(new BlockPos(arenaCenter.getX() + x, y, arenaCenter.getZ() + z), Blocks.AIR.getDefaultState());
							}
						}
					}
					godEyeWorld.playSound(null, arenaCenter, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 2.0f, 0.5f);
					arenaTransformLayer++;
				}
				// LAYERS 1-20: Build the Obsidian walls up over time
				else if (arenaTransformLayer <= 20) {
					// Build one layer per tick
					int currentY = floorY + arenaTransformLayer;

					for (int x = -radius; x <= radius; x++) {
						for (int z = -radius; z <= radius; z++) {
							if (x == -radius || x == radius || z == -radius || z == radius) {
								godEyeWorld.setBlockState(new BlockPos(arenaCenter.getX() + x, currentY, arenaCenter.getZ() + z), Blocks.OBSIDIAN.getDefaultState());
							}
						}
					}

					godEyeWorld.playSound(null, arenaCenter, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 3.0f, 0.5f);
					arenaTransformLayer++;
				}
				// Finish Transformation
				else {
					isTransformingArena = false;
				}
			}
		}

		// --- THE DIMENSION COLLAPSE SEQUENCE ---
		if (isDimensionCollapsing) {
			if (godEyeWorld != null) {
				collapseTicks++;
				int remainingSeconds = (MAX_COLLAPSE_TICKS - collapseTicks) / 20;

				// --- 1. CRUMBLING ARENA FLOOR ---
				if (arenaCenter != null) {
					// Delete 20 random floor blocks every single tick
					for(int i = 0; i < 20; i++) {
						int dx = godEyeWorld.random.nextInt(80) - 40;
						int dz = godEyeWorld.random.nextInt(80) - 40;
						BlockPos crumblePos = arenaCenter.add(dx, 0, dz);

						// Protect the 5x5 portal area so they can still jump into it!
						if (Math.abs(dx) > 2 || Math.abs(dz) > 2) {
							if (!godEyeWorld.getBlockState(crumblePos).isAir()) {
								godEyeWorld.setBlockState(crumblePos, Blocks.AIR.getDefaultState());
								godEyeWorld.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE, crumblePos.getX(), crumblePos.getY(), crumblePos.getZ(), 2, 0, 0, 0, 0);
							}
						}
					}
				}

				for (ServerPlayerEntity p : new java.util.ArrayList<>(godEyeWorld.getPlayers())) {
					// Terrifying Action Bar Timer
					p.sendMessage(Text.literal("DIMENSION COLLAPSING: " + remainingSeconds + "s").formatted(Formatting.RED, Formatting.BOLD), true);

					// --- 2. CUSTOM OVERWORLD ESCAPE PORTAL ---
					if (arenaCenter != null) {
						// We calculate a 2D horizontal distance, completely ignoring height!
						double dx = p.getX() - arenaCenter.getX();
						double dz = p.getZ() - arenaCenter.getZ();
						double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

						// If the player steps inside the 3x3 frame
						if (horizontalDistance <= 2.0 && p.getY() >= 60 && p.getY() <= 66) {
							ServerWorld overworld = server.getWorld(World.OVERWORLD);
							if (overworld != null) {
								int randomX = overworld.random.nextInt(10000) - 5000;
								int randomZ = overworld.random.nextInt(10000) - 5000;

								// --- NEW: INVENTORY WIPE (SAVE THE STAFF) ---
								net.minecraft.item.ItemStack savedStaff = null;
								for (int i = 0; i < p.getInventory().size(); i++) {
									net.minecraft.item.ItemStack currentItem = p.getInventory().getStack(i);
									// Check if the item is the Nightfall Staff
									if (currentItem.isOf(NIGHTFALL_STAFF)) {
										savedStaff = currentItem.copy(); // Create a safe duplicate including NBT data
										break;
									}
								}

								// Strip away all the armor, apples, and blocks
								p.getInventory().clear();

								// Hand the staff back to them
								if (savedStaff != null) {
									p.getInventory().insertStack(savedStaff);
								}

								// Teleport them high in the sky (Y=250) to allow chunks to load
								p.teleport(overworld, randomX + 0.5, 250.0, randomZ + 0.5, p.getYaw(), p.getPitch());

								p.playSound(com.carlo.Godeye.VOICE_YOURS_EVENT, net.minecraft.sound.SoundCategory.MASTER, 5.0f, 1.0f);

								// Grant 20 seconds of Slow Falling and total Resistance so they parachute down safely
								p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 400, 0, false, false, false));
								p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 255, false, false, false));

								p.sendMessage(Text.literal("You narrowly escaped the void, but he will return").formatted(Formatting.GREEN, Formatting.ITALIC), false);

								continue;
							}
						}
					}

					// --- 3. METEOR SHOWERS ---
					// Randomly spawn massive Ghast fireballs falling straight down
					if (godEyeWorld.random.nextInt(15) == 0) {
						double mX = p.getX() + (godEyeWorld.random.nextDouble() - 0.5) * 40;
						double mZ = p.getZ() + (godEyeWorld.random.nextDouble() - 0.5) * 40;

						net.minecraft.entity.projectile.FireballEntity fireball = new net.minecraft.entity.projectile.FireballEntity(godEyeWorld, p, 0, -3.0, 0, 2);
						fireball.setPosition(mX, p.getY() + 40, mZ);
						godEyeWorld.spawnEntity(fireball);
					}
				}

				// --- 4. TIME IS UP ---
				if (collapseTicks >= MAX_COLLAPSE_TICKS) {
					for (ServerPlayerEntity p : godEyeWorld.getPlayers()) {
						// Pure Void damage instantly kills them regardless of armor
						p.damage(godEyeWorld.getDamageSources().outOfWorld(), 1000.0f);
					}
					isDimensionCollapsing = false; // Reset the sequence
				}
			}
		}
	}

	private void sendDistortedMessage(ServerPlayerEntity player, int stage) {
		String[] words = {"Nothing ", "escapes ", "the God's ", "vision."};
		MutableText finalMessage = Text.empty();
		for (int i = 0; i < words.length; i++) {
			MutableText word = Text.literal(words[i]);
			if (i >= stage) word.formatted(Formatting.OBFUSCATED, Formatting.DARK_GRAY);
			else word.formatted(Formatting.DARK_RED, Formatting.BOLD);
			finalMessage.append(word);
		}
		player.sendMessage(finalMessage, false);
		player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_TENDRIL_CLICKS, SoundCategory.AMBIENT, 1.0f, 0.5f);
	}

	private void triggerDecay(ServerPlayerEntity player, int ticks) {
		ServerWorld world = player.getServerWorld();
		BlockPos playerPos = player.getBlockPos();

		if (ticks % 5 == 0) {
			int radius = 40; //radius of decay
			for (int i = 0; i < 300; i++) { // Scans 300 random blocks per tick
				int x = playerPos.getX() + world.random.nextInt(radius * 2) - radius;
				int y = playerPos.getY() + world.random.nextInt(40) - 10;
				int z = playerPos.getZ() + world.random.nextInt(radius * 2) - radius;
				BlockPos targetPos = new BlockPos(x, y, z);
				BlockState state = world.getBlockState(targetPos);

				if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES)) {
					// Delete the main block
					world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 3);

					// Randomly delete the blocks immediately above/below it to rip chunks out of the tree
					if (world.random.nextBoolean()) world.setBlockState(targetPos.up(), Blocks.AIR.getDefaultState(), 3);
					if (world.random.nextBoolean()) world.setBlockState(targetPos.down(), Blocks.AIR.getDefaultState(), 3);
				}
			}
		}

		if (ticks > 400 && ticks % 20 == 0) {
			Box box = new Box(playerPos).expand(40);
			List<MobEntity> mobs = world.getEntitiesByClass(MobEntity.class, box, entity -> true);
			if (!mobs.isEmpty()) {
				int mobsToDelete = Math.min(5, mobs.size());
				for (int i = 0; i < mobsToDelete; i++) mobs.get(i).discard();
			}
		}
	}

	private void triggerNightfall(ServerWorld world) {
		world.setTimeOfDay(18000);
		world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
		world.playSound(null, world.getSpawnPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.AMBIENT, 2.0f, 0.1f);
	}

	private void triggerAbduction(ServerPlayerEntity player) {
		ServerWorld targetWorld = player.getServer().getWorld(GODEYE_DIMENSION_KEY);
		if (targetWorld != null) {
			BlockPos spawnPos = new BlockPos(0, 64, 0);
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					targetWorld.setBlockState(spawnPos.add(x, -1, z), Blocks.OBSIDIAN.getDefaultState());
				}
			}
			player.teleport(targetWorld, 0.5, 64, 0.5, player.getYaw(), player.getPitch());
			player.getInventory().clear();

			ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
			NbtCompound nbt = book.getOrCreateNbt();
			nbt.putString("title", "The God's Domain");
			nbt.putString("author", "Lucas");
			NbtList pages = new NbtList();
			pages.add(NbtString.of("{\"text\":\"He is watching.\\n\\nThe Starfall Beacon is hidden in the dark.\\n\\nCast this book into the abyss to reveal the path, and don't look back.\"}"));
			nbt.put("pages", pages);
			player.getInventory().insertStack(book);

			WAITING_FOR_BRIDGE.add(player.getUuid());

			WatcherEntity distantFrost = GODEYE_WATCHER.create(targetWorld);
			if (distantFrost != null) {
				// BUG FIX 2: Hardcode the center of the platform so dimension desyncs don't break the spawn math
				net.minecraft.util.math.Vec3d platformCenter = new net.minecraft.util.math.Vec3d(0.5, 64, 0.5);

				// Ignore pitch so it doesn't spawn below the platform if the player is looking down
				net.minecraft.util.math.Vec3d flatLook = net.minecraft.util.math.Vec3d.fromPolar(0.0f, player.getYaw());
				net.minecraft.util.math.Vec3d spawnPosVec = platformCenter.add(flatLook.multiply(25.0)).add(0, 4, 0);

				distantFrost.refreshPositionAndAngles(spawnPosVec.x, spawnPosVec.y, spawnPosVec.z, player.getYaw() + 180.0f, 0);
				distantFrost.setLifespan(-1);

				distantFrost.setPersistent();
				// glowing outline for ma boi frost
				distantFrost.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 999999, 0, false, false, false));

				targetWorld.spawnEntity(distantFrost);
			}
		}
	}

	private void buildBridgeTick(ServerWorld godEyeWorld, int currentTick) {
		if (currentTick % 2 == 0) {
			BlockPos nextBlock = new BlockPos(0, 63, bridgeProgressZ);
			godEyeWorld.setBlockState(nextBlock, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState());
			godEyeWorld.playSound(null, nextBlock, SoundEvents.BLOCK_BASALT_PLACE, SoundCategory.BLOCKS, 1.0f, 0.5f);
			bridgeProgressZ++;
		}
	}

	private void generateConcentricMaze(ServerWorld world) {
		int centerX = 0;
		int centerZ = 75 + 20;
		int maxRadius = 45;

		// 1. Generate Floor with Traps
		for (int x = -maxRadius; x <= maxRadius; x++) {
			for (int z = -maxRadius; z <= maxRadius; z++) {
				BlockPos floorPos = new BlockPos(centerX + x, 63, centerZ + z);

				// 5% chance to spawn a trap
				if (world.random.nextFloat() < 0.05f) {
					if (world.random.nextBoolean()) {
						// Magma Block Trap
						world.setBlockState(floorPos, Blocks.MAGMA_BLOCK.getDefaultState());
					} else {
						// Cobweb Trap (Floor is normal, cobweb is on top)
						world.setBlockState(floorPos, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState());
						world.setBlockState(floorPos.up(), Blocks.COBWEB.getDefaultState());
					}
				} else {
					// Normal Floor
					world.setBlockState(floorPos, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState());
				}
			}
		}

		// 2. Generate Walls
		for (int r = 4; r <= maxRadius; r += 4) {
			int doorN = world.random.nextInt(r * 2) - r;
			int doorS = world.random.nextInt(r * 2) - r;
			int doorE = world.random.nextInt(r * 2) - r;
			int doorW = world.random.nextInt(r * 2) - r;

			for (int x = -r; x <= r; x++) {
				for (int z = -r; z <= r; z++) {
					if (Math.abs(x) == r || Math.abs(z) == r) {
						boolean isHole = false;

						if (z == -r && Math.abs(x - doorN) <= 1) isHole = true;
						if (z ==  r && Math.abs(x - doorS) <= 1) isHole = true;
						if (x == -r && Math.abs(z - doorE) <= 1) isHole = true;
						if (x ==  r && Math.abs(z - doorW) <= 1) isHole = true;

						if (r == maxRadius && Math.abs(x) <= 1 && z == -r) isHole = true;

						if (!isHole) {
							for (int y = 64; y <= 75; y++) {
								world.setBlockState(new BlockPos(centerX + x, y, centerZ + z), Blocks.REINFORCED_DEEPSLATE.getDefaultState());
							}
						}
					}
				}
			}
		}

		// 3. --- SPAWN THE CUSTOM ALTAR AND THE CORE (Runs exactly ONCE) ---
		Identifier altarId = new Identifier(MOD_ID, "altar");
		StructureTemplateManager templateManager = world.getServer().getStructureTemplateManager();
		java.util.Optional<StructureTemplate> templateOpt = templateManager.getTemplate(altarId);

		if (templateOpt.isPresent()) {
			StructureTemplate template = templateOpt.get();

			// Shifted up to Y=64 so it sits ON the floor, not IN it
			BlockPos altarPos = new BlockPos(
					centerX - (template.getSize().getX() / 2),
					64,
					centerZ - (template.getSize().getZ() / 2)
			);

			StructurePlacementData placementData = new StructurePlacementData()
					.setRotation(BlockRotation.NONE)
					.setMirror(BlockMirror.NONE);

			template.place(world, altarPos, altarPos, placementData, world.random, 2);

			GodEyeCoreEntity core = GODEYE_CORE.create(world);
			if (core != null) {
				// Added + 1.5 to make it float nicely above the altar
				core.refreshPositionAndAngles(centerX, 64 + template.getSize().getY() + 1.5, centerZ, 0, 0);
				core.setPersistent(); // Stop the core from despawning too!
				world.spawnEntity(core);
			}
		} else {
			LOGGER.error("Failed to load the altar.nbt structure! Make sure it is in data/godeye/structures/");
		}

		// 4. --- SPAWN MOBS ---
		for (int i = 0; i < 20; i++) {
			int spawnX = centerX + world.random.nextInt(maxRadius * 2) - maxRadius;
			int spawnZ = centerZ + world.random.nextInt(maxRadius * 2) - maxRadius;

			if (world.random.nextBoolean()) {
				WitherSkeletonEntity skeleton = EntityType.WITHER_SKELETON.create(world);
				if (skeleton != null) {
					skeleton.refreshPositionAndAngles(spawnX, 64, spawnZ, 0, 0);
					world.spawnEntity(skeleton);
				}
			} else {
				ZombieEntity zombie = EntityType.ZOMBIE.create(world);
				if (zombie != null) {
					zombie.refreshPositionAndAngles(spawnX, 64, spawnZ, 0, 0);
					world.spawnEntity(zombie);
				}
			}
		}
	}

	private void giveMazeGear(ServerPlayerEntity player) {
		player.getInventory().clear();

		player.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		player.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		player.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
		player.equipStack(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));

		player.getInventory().insertStack(new ItemStack(Items.DIAMOND_SWORD));
		player.getInventory().insertStack(new ItemStack(Items.GOLDEN_APPLE, 5));

		for (int i = 0; i < 4; i++) {
			player.getInventory().insertStack(new ItemStack(Items.COBBLESTONE, 64));
		}

		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		NbtCompound nbt = book.getOrCreateNbt();
		nbt.putString("title", "Through the Darkness");
		nbt.putString("author", "Lucas");
		NbtList pages = new NbtList();
		pages.add(NbtString.of("{\"text\":\"The walls shift, but stone remains.\\n\\nUse this cobblestone to seal off the corridors behind you. Mark your path.\\n\\nDo not trust the shadows. The Beacon lies at the center.\"}"));
		nbt.put("pages", pages);

		player.getInventory().insertStack(book);
	}

	private void triggerInFaceJumpscare(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();

		player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.BLINDNESS, 60, 0, false, false, false
		));

		player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.SLOWNESS, 60, 255, false, false, false
		));


		world.playSound(null, player.getBlockPos(), com.carlo.Godeye.JUMPSCARE_EVENT, SoundCategory.AMBIENT, 5.0f, 1.0f);

		net.minecraft.util.math.Vec3d lookVec = player.getRotationVector();
		net.minecraft.util.math.Vec3d spawnPos = player.getPos().add(lookVec.multiply(1.5));

		WatcherEntity watcher = GODEYE_WATCHER.create(world);
		if (watcher != null) {
			watcher.refreshPositionAndAngles(spawnPos.x, player.getY(), spawnPos.z, player.getYaw() + 180.0f, player.getPitch());
			world.spawnEntity(watcher);
			watcher.setLifespan(60);
		}
	}

	private void triggerStalker(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();

		int distance = 20 + world.random.nextInt(10);
		double angle = world.random.nextDouble() * Math.PI * 2;

		double spawnX = player.getX() + (Math.cos(angle) * distance);
		double spawnZ = player.getZ() + (Math.sin(angle) * distance);

		BlockPos spawnBlock = new BlockPos((int)spawnX, (int)player.getY(), (int)spawnZ);

		if (world.getBlockState(spawnBlock).isAir() && world.getBlockState(spawnBlock.up()).isAir()) {
			world.playSound(null, spawnBlock, SoundEvents.AMBIENT_CAVE.value(), SoundCategory.AMBIENT, 1.0f, 0.5f);

			WatcherEntity watcher = GODEYE_WATCHER.create(world);
			if (watcher != null) {
				watcher.lookAtEntity(player, 360.0f, 360.0f);
				watcher.refreshPositionAndAngles(spawnX, player.getY(), spawnZ, watcher.getYaw(), 0);
				world.spawnEntity(watcher);
				watcher.setLifespan(100);
			}
		}
	}

	private ItemStack createLoreBook() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		NbtCompound nbt = book.getOrCreateNbt();

		nbt.putString("title", "Journal of Frost");
		nbt.putString("author", "DanielFrost"); // Adds a terrifying personal touch for the SMP!

		NbtList pages = new NbtList();
		pages.add(NbtString.of("{\"text\":\"Day 14.\\n\\nThe shadows have started moving on their own. I hear a heavy heartbeat beneath the dirt.\\n\\nIf you are reading this, you are already marked.\"}"));
		pages.add(NbtString.of("{\"text\":\"There is a dimension hidden between the folds of our world.\\n\\nThe only way out is to cast the Beacon into the abyss. Do not trust your eyes.\"}"));
		pages.add(NbtString.of("{\"text\":\"I feel like my hands and legs are moving much slower.\\n Iam starting to think Iam going to loose my limbs at some point\\n I feel like iam freezing..?\"}"));
		nbt.put("pages", pages);

		return book;
	}

	private void spawnLoreShrine(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();

		// Calculate a position about 30 blocks away from the player in a random direction
		double angle = world.random.nextDouble() * Math.PI * 2;
		int distance = 30;
		int targetX = (int)(player.getX() + Math.cos(angle) * distance);
		int targetZ = (int)(player.getZ() + Math.sin(angle) * distance);

		// Find the highest solid block at those coordinates so it doesn't spawn floating
		BlockPos spawnPos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(targetX, 0, targetZ));

		Identifier shrineId = new Identifier(MOD_ID, "lore_shrine");
		StructureTemplateManager templateManager = world.getServer().getStructureTemplateManager();
		java.util.Optional<StructureTemplate> templateOpt = templateManager.getTemplate(shrineId);

		if (templateOpt.isPresent()) {
			StructureTemplate template = templateOpt.get();

			// Submerges the structure by 1 block so it blends into the natural terrain
			BlockPos placePos = spawnPos.add(-(template.getSize().getX() / 2), -1, -(template.getSize().getZ() / 2));

			StructurePlacementData placementData = new StructurePlacementData()
					.setRotation(BlockRotation.NONE)
					.setMirror(BlockMirror.NONE);

			template.place(world, placePos, placePos, placementData, world.random, 2);

			// Play a distant thunder sound to naturally draw the player's attention to that direction!
			world.playSound(null, placePos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 7.0f, 0.5f);

			// Scan the newly placed structure, find the chest, and insert the Lore Book
			for (int x = 0; x < template.getSize().getX(); x++) {
				for (int y = 0; y < template.getSize().getY(); y++) {
					for (int z = 0; z < template.getSize().getZ(); z++) {
						BlockPos checkPos = placePos.add(x, y, z);
						if (world.getBlockState(checkPos).isOf(Blocks.TRAPPED_CHEST)) {
							net.minecraft.block.entity.BlockEntity blockEntity = world.getBlockEntity(checkPos);
							if (blockEntity instanceof net.minecraft.block.entity.TrappedChestBlockEntity chest) {
								chest.setStack(13, createLoreBook()); // Slot 13 is dead-center of the chest inventory
							}
						}
					}
				}
			}
		} else {
			LOGGER.error("Failed to load lore_shrine.nbt! Make sure it is in data/godeye/structures/");
		}
	}
}