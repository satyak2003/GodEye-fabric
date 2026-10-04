package com.carlo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import com.carlo.Godeye;
import com.carlo.entity.client.GodEyeBossRenderer;
import com.carlo.entity.client.GodEyeCoreRenderer;
import com.carlo.entity.client.WatcherRenderer;
import com.carlo.item.client.NightfallStaffRenderer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class GodeyeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.CameraLockPayload.ID, (p1, ctx1) -> {
			ctx1.client().execute(() -> {
				ClientStoryState.isCameraLocked = p1.locked();
				ClientStoryState.cameraLockTargetId = p1.targetEntityId();
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.NightFlashPayload.ID, (p2, ctx2) -> {
			ctx2.client().execute(() -> {
				ClientStoryState.nightFlashTicks = p2.durationTicks();
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.ControlLockPayload.ID, (p3, ctx3) -> {
			ctx3.client().execute(() -> {
				ClientStoryState.isControlLocked = p3.locked();
			});
		});
		
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.CinematicLockPayload.ID, (p4, ctx4) -> {
			ctx4.client().execute(() -> {
				ClientStoryState.isCinematicLocked = p4.locked();
				if (p4.locked() && ctx4.client().currentScreen != null) {
				    ctx4.client().setScreen(null);
				}
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.BlackScreenPayload.ID, (p5, ctx5) -> {
			ctx5.client().execute(() -> {
				ClientStoryState.isBlackScreen = p5.enabled();
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.CinematicTextPayload.ID, (p6, ctx6) -> {
			ctx6.client().execute(() -> {
				ClientStoryState.cinematicTextStage = p6.textStage();
				ClientStoryState.cinematicTextTicks = 0;
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.DisappearanceParticlePayload.ID, (p8, ctx8) -> {
			ctx8.client().execute(() -> {
				ClientStoryState.activeEffects.add(new ClientStoryState.ActiveParticleEffect(p8.x(), p8.y(), p8.z(), p8.isWitness()));
			});
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.carlo.network.CH04SnowPayload.ID, (p7, ctx7) -> {
			ctx7.client().execute(() -> {
				ClientStoryState.ch04Snow = p7.enabled();
			});
		});
		
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			java.util.Iterator<ClientStoryState.ActiveParticleEffect> it = ClientStoryState.activeEffects.iterator();
			while(it.hasNext()) {
			    ClientStoryState.ActiveParticleEffect eff = it.next();
			    eff.ticks++;
			    int maxTicks = eff.isWitness ? 20 : 15;
			    if (eff.ticks > maxTicks) { it.remove(); continue; }
			    
			    java.util.Random rand = new java.util.Random();
			    if (client.world != null) {
			        if (eff.isWitness) {
			            for(int i=0; i<3; i++) {
			                double ox = (rand.nextDouble() - 0.5) * 4.0 * (1.0 - (double)eff.ticks/20.0);
			                double oy = (rand.nextDouble() * 3.0);
			                double oz = (rand.nextDouble() - 0.5) * 4.0 * (1.0 - (double)eff.ticks/20.0);
			                client.particleManager.addParticle(net.minecraft.particle.ParticleTypes.SQUID_INK, eff.x + ox, eff.y + oy, eff.z + oz, -ox*0.1, -oy*0.05, -oz*0.1);
			                if (rand.nextBoolean()) client.particleManager.addParticle(net.minecraft.particle.ParticleTypes.WHITE_ASH, eff.x + ox, eff.y + oy, eff.z + oz, -ox*0.1, -oy*0.05, -oz*0.1);
			            }
			        } else {
			            for(int i=0; i<2; i++) {
			                double ox = (rand.nextDouble() - 0.5) * 2.0;
			                double oy = (rand.nextDouble() * 2.0);
			                double oz = (rand.nextDouble() - 0.5) * 2.0;
			                client.particleManager.addParticle(net.minecraft.particle.ParticleTypes.ASH, eff.x + ox, eff.y + oy, eff.z + oz, -ox*0.05, 0, -oz*0.05);
			            }
			        }
			    }
			}
			
			if (ClientStoryState.nightFlashTicks > 0) {
				ClientStoryState.nightFlashTicks--;
			}
			if (ClientStoryState.cinematicTextStage > 0) {
			    ClientStoryState.cinematicTextTicks++;
			}
			if (ClientStoryState.isCameraLocked && ClientStoryState.cameraLockTargetId != -1 && client.player != null && client.world != null) {
				net.minecraft.entity.Entity target = client.world.getEntityById(ClientStoryState.cameraLockTargetId);
				if (target != null) {
					net.minecraft.util.math.Vec3d diff = target.getEyePos().subtract(client.player.getEyePos());
					double diffXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
					float targetYaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;
					float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, diffXZ));
					client.player.setYaw(targetYaw);
					client.player.setPitch(targetPitch);
					client.player.setBodyYaw(targetYaw);
					client.player.setHeadYaw(targetYaw);
				}
			}
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;
			
			if (ClientStoryState.isBlackScreen) {
			    drawContext.fill(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 0xFF000000);
			    
			    if (ClientStoryState.cinematicTextStage > 0) {
			        String text = "";
			        if (ClientStoryState.cinematicTextStage == 1) text = "you had all the time to run";
			        if (ClientStoryState.cinematicTextStage == 2) text = "But you did not";
			        if (ClientStoryState.cinematicTextStage == 3) text = "I see you now";
			        if (ClientStoryState.cinematicTextStage == 4) text = "Quitting is not an option";
			        
			        int ticks = ClientStoryState.cinematicTextTicks;
			        float alpha = 1.0f;
			        if (ticks < 20) alpha = ticks / 20.0f;
			        else if (ticks > 80) alpha = Math.max(0, (100 - ticks) / 20.0f);
			        
			        int alphaInt = (int)(alpha * 255);
			        int color = (alphaInt << 24) | 0xFFFFFF;
			        
			        if (alphaInt > 0) {
			            int screenWidth = client.getWindow().getScaledWidth();
				        int screenHeight = client.getWindow().getScaledHeight();
			            int textWidth = client.textRenderer.getWidth(text);
			            drawContext.drawTextWithShadow(client.textRenderer, text, (screenWidth - textWidth) / 2, screenHeight / 2, color);
			        }
			    }
			}

			ItemStack mainHand = client.player.getMainHandStack();
			if (mainHand.getItem() == Godeye.NIGHTFALL_STAFF) {
				int souls = mainHand.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt().getInt("absorbed_souls").orElse(0);
				int screenWidth = client.getWindow().getScaledWidth();
				int screenHeight = client.getWindow().getScaledHeight();
				int yPos = screenHeight - 60;
				if (souls >= 10) {
					String text = "ORBITAL READY (Shift + Right Click to Fire)";
					int textWidth = client.textRenderer.getWidth(text);
					drawContext.drawTextWithShadow(client.textRenderer, text, (screenWidth - textWidth) / 2, yPos, 0xFF5555);
				} else {
					String text = "Souls: " + souls + " / 10";
					int textWidth = client.textRenderer.getWidth(text);
					drawContext.drawTextWithShadow(client.textRenderer, text, (screenWidth - textWidth) / 2, yPos, 0x55FFFF);
				}
			}
		});
		
		EntityRendererRegistry.register(Godeye.GODEYE_CORE, GodEyeCoreRenderer::new);
		EntityRendererRegistry.register(Godeye.GODEYE_BOSS, GodEyeBossRenderer::new);
		EntityRendererRegistry.register(Godeye.GODEYE_WATCHER, WatcherRenderer::new);
		EntityRendererRegistry.register(Godeye.WITNESS, com.carlo.entity.client.WitnessRenderer::new);
        EntityRendererRegistry.register(Godeye.FROST, com.carlo.entity.client.FrostRenderer::new);
	}
}



