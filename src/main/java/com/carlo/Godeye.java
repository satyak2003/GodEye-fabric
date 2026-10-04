package com.carlo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import com.carlo.entity.GodEyeCoreEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.carlo.entity.GodEyeBossEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.carlo.entity.WatcherEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.carlo.story.ChapterManager;
import com.carlo.story.GlobalWorldState;
import com.carlo.story.event.EventScheduler;
import com.carlo.network.BlackScreenPayload;
import com.carlo.network.CinematicTextPayload;
import com.carlo.network.DisappearanceParticlePayload;
import net.minecraft.sound.SoundEvent;

public class Godeye implements ModInitializer {
    public static final String MOD_ID = "godeye";
    public static final Item NIGHTFALL_STAFF = new com.carlo.item.NightfallStaffItem(new Item.Settings().maxCount(1));
    
    public static final EntityType<com.carlo.entity.FrostEntity> FROST = Registry.register(Registries.ENTITY_TYPE, Identifier.of("godeye", "frost"), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, com.carlo.entity.FrostEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, Identifier.of("godeye", "frost"))));
    public static final EntityType<com.carlo.entity.WitnessEntity> WITNESS = Registry.register(Registries.ENTITY_TYPE, Identifier.of("godeye", "witness"), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, com.carlo.entity.WitnessEntity::new).dimensions(EntityDimensions.fixed(0.6f, 2.2f).withEyeHeight(2.1f)).build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, Identifier.of("godeye", "witness"))));
    public static final EntityType<GodEyeCoreEntity> GODEYE_CORE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "godeye_core"), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GodEyeCoreEntity::new).dimensions(EntityDimensions.fixed(3.0f, 3.0f)).build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "entity"))));
    public static final EntityType<GodEyeBossEntity> GODEYE_BOSS = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "godeye_boss"), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GodEyeBossEntity::new).dimensions(EntityDimensions.fixed(3.0f, 3.0f)).build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "entity"))));
    public static final EntityType<WatcherEntity> GODEYE_WATCHER = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "godeye_watcher"), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WatcherEntity::new).dimensions(EntityDimensions.fixed(0.8f, 4.5f)).build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "watcher"))));

    private static final Map<UUID, Integer> playerTicks = new HashMap<>();

        public static net.minecraft.util.math.BlockPos arenaCenter = null;
    public static int arenaTransformLayer = 0;
    public static boolean isTransformingArena = false;
    public static boolean isDimensionCollapsing = false;
    public static int collapseTicks = 0;
    public static final Identifier BOSS_DEATH_ID = Identifier.of("godeye", "boss_death");
    public static final SoundEvent BOSS_DEATH_EVENT = SoundEvent.of(BOSS_DEATH_ID);
    public static final Identifier VOICE_RUN_ID = Identifier.of("godeye", "boss_run");
    public static final SoundEvent VOICE_RUN_EVENT = SoundEvent.of(VOICE_RUN_ID);
	public static final Identifier FROST_SPAWN_ID = Identifier.of("godeye", "frost_spawn");
    public static final SoundEvent FROST_SPAWN_EVENT = SoundEvent.of(FROST_SPAWN_ID);
    public static final Identifier FROST_VANISH_ID = Identifier.of("godeye", "frost_vanish");
    public static final SoundEvent FROST_VANISH_EVENT = SoundEvent.of(FROST_VANISH_ID);
    
    public static final Identifier WITNESS_ENCOUNTER_1_ID = Identifier.of("godeye", "witness_encounter_1");
    public static final SoundEvent WITNESS_ENCOUNTER_1_EVENT = SoundEvent.of(WITNESS_ENCOUNTER_1_ID);
    public static final Identifier GODEYE_DEATH_SCR_ID = Identifier.of("godeye", "godeye_death_scr");
    public static final SoundEvent GODEYE_DEATH_SCR_EVENT = SoundEvent.of(GODEYE_DEATH_SCR_ID);

    public static final Identifier JUMPSCARE_ID = Identifier.of("godeye", "jumpscare");
    public static final SoundEvent JUMPSCARE_EVENT = SoundEvent.of(JUMPSCARE_ID);
    
    public static final Identifier WATCHER_TP_ID = Identifier.of("godeye", "watcher_tp");
    public static final SoundEvent WATCHER_TP_EVENT = SoundEvent.of(WATCHER_TP_ID);
    
    public static final Identifier CH03_BGM_ID = Identifier.of("godeye", "ch03_bgm");
    public static final SoundEvent CH03_BGM_EVENT = SoundEvent.of(CH03_BGM_ID);
    
    public static final Identifier VOICE_YOURS_ID = Identifier.of("godeye", "your_world");
    public static final SoundEvent VOICE_YOURS_EVENT = SoundEvent.of(VOICE_YOURS_ID);
    
    public static final Identifier VOICE_CHANCE_ID = Identifier.of("godeye", "boss_chance");
    public static final SoundEvent VOICE_CHANCE_EVENT = SoundEvent.of(VOICE_CHANCE_ID);

    @Override
    public void onInitialize() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(NIGHTFALL_STAFF);
        });

        Registry.register(Registries.ITEM, Identifier.of("godeye", "nightfall_staff"), NIGHTFALL_STAFF);

        FabricDefaultAttributeRegistry.register(FROST, com.carlo.entity.FrostEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(GODEYE_CORE, GodEyeCoreEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(GODEYE_BOSS, GodEyeBossEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(GODEYE_WATCHER, WatcherEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(WITNESS, com.carlo.entity.WitnessEntity.setAttributes());
        
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.TeleportAnomalyStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.LeafDecayStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.MobVanishStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.NightFlashStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH02JumpscareStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.FrostJoinStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH02MessageStoryEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03StartEncounterEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03WatcherRelocateEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03ObservationTickEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03EncounterCompleteEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03FinaleStartEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03FinaleCinematicEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH03EndEvent());
        
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04NightStartEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04SnowStartEvent());
        
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04FrostSpawnEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04WitnessStartEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04WitnessSpawnEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04WitnessApproachEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04WitnessDarknessEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04DialogueEvent());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep1Event());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep2Event());
        com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep3Event());

        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.CameraLockPayload.ID, com.carlo.network.CameraLockPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.NightFlashPayload.ID, com.carlo.network.NightFlashPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.ControlLockPayload.ID, com.carlo.network.ControlLockPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(BlackScreenPayload.ID, BlackScreenPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(CinematicTextPayload.ID, CinematicTextPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.CH04SnowPayload.ID, com.carlo.network.CH04SnowPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.CinematicLockPayload.ID, com.carlo.network.CinematicLockPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(DisappearanceParticlePayload.ID, DisappearanceParticlePayload.CODEC);

                Registry.register(Registries.SOUND_EVENT, BOSS_DEATH_ID, BOSS_DEATH_EVENT);
        Registry.register(Registries.SOUND_EVENT, VOICE_RUN_ID, VOICE_RUN_EVENT);
		Registry.register(Registries.SOUND_EVENT, FROST_SPAWN_ID, FROST_SPAWN_EVENT);
        Registry.register(Registries.SOUND_EVENT, FROST_VANISH_ID, FROST_VANISH_EVENT);
        Registry.register(Registries.SOUND_EVENT, WITNESS_ENCOUNTER_1_ID, WITNESS_ENCOUNTER_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, GODEYE_DEATH_SCR_ID, GODEYE_DEATH_SCR_EVENT);
        Registry.register(Registries.SOUND_EVENT, JUMPSCARE_ID, JUMPSCARE_EVENT);
        Registry.register(Registries.SOUND_EVENT, WATCHER_TP_ID, WATCHER_TP_EVENT);
        Registry.register(Registries.SOUND_EVENT, CH03_BGM_ID, CH03_BGM_EVENT);
        Registry.register(Registries.SOUND_EVENT, VOICE_YOURS_ID, VOICE_YOURS_EVENT);
        Registry.register(Registries.SOUND_EVENT, VOICE_CHANCE_ID, VOICE_CHANCE_EVENT);

        com.carlo.command.GodeyeCommands.register();

        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> {
            com.carlo.story.system.DeathSystem.onPlayerDeath(player);
            return true;
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            com.carlo.story.PlayerStoryState state = com.carlo.story.PlayerStoryState.getState(newPlayer);
            if (state.getDeathPos() != null) {
                newPlayer.setPosition(state.getDeathPos().getX() + 0.5, state.getDeathPos().getY(), state.getDeathPos().getZ() + 0.5);
                newPlayer.setYaw(state.getDeathYaw());
                newPlayer.setPitch(state.getDeathPitch());
                state.setInvulnerabilityEndTick(newPlayer.getEntityWorld().getTime() + 200);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(net.minecraft.server.MinecraftServer server) {
        com.carlo.story.ChapterManager.tick(server);
        com.carlo.story.event.EventScheduler.tick(server);
    }
}

