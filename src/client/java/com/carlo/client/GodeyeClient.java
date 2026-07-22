package com.carlo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

// We safely import the client classes here!
import software.bernie.geckolib.animatable.client.RenderProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
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

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;

			ItemStack mainHand = client.player.getMainHandStack();

			// Check if holding the staff
			if (mainHand.getItem() == Godeye.NIGHTFALL_STAFF) {
				int souls = mainHand.hasNbt() ? mainHand.getNbt().getInt("absorbed_souls") : 0;

				int screenWidth = client.getWindow().getScaledWidth();
				int screenHeight = client.getWindow().getScaledHeight();

				// Position it about 60 pixels from the bottom (above XP and health)
				int yPos = screenHeight - 60;

				if (souls >= 10) {
					String text = "ORBITAL READY (Shift + Right Click to Fire)";
					int textWidth = client.textRenderer.getWidth(text);
					// Draw in bright Red
					drawContext.drawTextWithShadow(client.textRenderer, text, (screenWidth - textWidth) / 2, yPos, 0xFF5555);
				} else {
					String text = "Souls: " + souls + " / 10";
					int textWidth = client.textRenderer.getWidth(text);
					// Draw in Cyan
					drawContext.drawTextWithShadow(client.textRenderer, text, (screenWidth - textWidth) / 2, yPos, 0x55FFFF);
				}
			}

		});
		// 1. Entity Renderers
		EntityRendererRegistry.register(Godeye.GODEYE_CORE, GodEyeCoreRenderer::new);
		EntityRendererRegistry.register(Godeye.GODEYE_BOSS, GodEyeBossRenderer::new);
		EntityRendererRegistry.register(Godeye.GODEYE_WATCHER, WatcherRenderer::new);

		// 2. Inject the GeckoLib Renderer safely into the Staff!
		Godeye.NIGHTFALL_STAFF.renderProvider = new Supplier<Object>() {
			private RenderProvider provider;

			@Override
			public Object get() {
				if (this.provider == null) {
					this.provider = new RenderProvider() {
						private NightfallStaffRenderer renderer;
						@Override
						public BuiltinModelItemRenderer getCustomRenderer() {
							if (this.renderer == null) {
								this.renderer = new NightfallStaffRenderer();
							}
							return this.renderer;
						}
					};
				}
				return this.provider;
			}
		};
	}
}