import sys

with open('src/main/java/com/carlo/Godeye.java', 'r', encoding='utf-8') as f:
    code = f.read()

findsnd1 = 'public static final net.minecraft.util.Identifier WITNESS_ENCOUNTER_1_ID = net.minecraft.util.Identifier.of("godeye", "witness_encounter_1");'
code = code.replace(findsnd1, findsnd1 + '\n\tpublic static final net.minecraft.util.Identifier GODEYE_DEATH_SCR_ID = net.minecraft.util.Identifier.of("godeye", "godeye_death_scr");')

findsnd2 = 'public static final net.minecraft.sound.SoundEvent WITNESS_ENCOUNTER_1_EVENT = net.minecraft.sound.SoundEvent.of(WITNESS_ENCOUNTER_1_ID);'
code = code.replace(findsnd2, findsnd2 + '\n\tpublic static final net.minecraft.sound.SoundEvent GODEYE_DEATH_SCR_EVENT = net.minecraft.sound.SoundEvent.of(GODEYE_DEATH_SCR_ID);')

findsnd3 = 'net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.SOUND_EVENT, WITNESS_ENCOUNTER_1_ID, WITNESS_ENCOUNTER_1_EVENT);'
code = code.replace(findsnd3, findsnd3 + '\n\t\tnet.minecraft.registry.Registry.register(net.minecraft.registry.Registries.SOUND_EVENT, GODEYE_DEATH_SCR_ID, GODEYE_DEATH_SCR_EVENT);')

findEv = 'com.carlo.story.event.EventRegistry.register(new com.carlo.story.event.CH04DialogueEvent());'
code = code.replace(findEv, findEv + '\n\t\tcom.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep1Event());\n\t\tcom.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep2Event());\n\t\tcom.carlo.story.event.EventRegistry.register(new com.carlo.story.event.DeathCinematicStep3Event());')

findPay = 'net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.CinematicLockPayload.ID, com.carlo.network.CinematicLockPayload.CODEC);'
code = code.replace(findPay, findPay + '\n\t\tnet.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.carlo.network.DisappearanceParticlePayload.ID, com.carlo.network.DisappearanceParticlePayload.CODEC);')

findTick = 'ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);'
hooks = '''
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
				state.setInvulnerabilityEndTick(newPlayer.getEntityWorld().getTime() + 200); // 10 seconds
			}
		});
'''
code = code.replace(findTick, hooks + '\n\t\t' + findTick)

import re
wReg = '''public static final net.minecraft.entity.EntityType<com.carlo.entity.WitnessEntity> WITNESS = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.ENTITY_TYPE,
			net.minecraft.util.Identifier.of("godeye", "witness"),
			net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder.create(net.minecraft.entity.SpawnGroup.MONSTER, com.carlo.entity.WitnessEntity::new).dimensions(net.minecraft.entity.EntityDimensions.fixed(0.6f, 2.2f).withEyeHeight(2.1f))
					.build(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ENTITY_TYPE, net.minecraft.util.Identifier.of("godeye", "witness")))
	);'''
code = re.sub(r'public static final net\.minecraft\.entity\.EntityType<com\.carlo\.entity\.WitnessEntity> WITNESS = net\.minecraft\.registry\.Registry\.register\([\s\S]*?\n\t\);', wReg, code)

with open('src/main/java/com/carlo/Godeye.java', 'w', encoding='utf-8') as f:
    f.write(code)
