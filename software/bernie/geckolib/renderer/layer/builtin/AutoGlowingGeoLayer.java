package software.bernie.geckolib.renderer.layer.builtin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Built-in {@link GeoRenderLayer} for rendering an emissive layer over an existing GeoAnimatable
 * <p>
 * By default, it uses a custom RenderType created by GeckoLib to make things easier, but it can be overridden as needed
 *
 * @param <T> Animatable class type. Inherited from the renderer this layer is attached to
 * @param <O> Associated object class type, or {@link Void} if none. Inherited from the renderer this layer is attached to
 * @param <R> RenderState class type. Inherited from the renderer this layer is attached to
 * @see <a href="https://github.com/bernie-g/geckolib/wiki/Emissive-Textures-Glow-Layer">GeckoLib Wiki - Glow Layers</a>
 */
public class AutoGlowingGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends TextureLayerGeoLayer<T, O, R> {
	private final Map<Identifier, Identifier> emissiveResourceCache = new Object2ObjectOpenHashMap<>();
	protected static final RenderPipeline RENDER_PIPELINE = RenderPipelines.register(EmissiveRenderType.createRenderPipeline());

	public AutoGlowingGeoLayer(GeoRenderer<T, O, R> renderer) {
		super(renderer);
	}

	/**
	 * Get the texture resource path for the given {@link GeoRenderState}
	 */
	@Override
	protected Identifier getTextureResource(R renderState) {
		return this.emissiveResourceCache.computeIfAbsent(this.renderer.getTextureLocation(renderState), RenderUtil::getEmissiveResource);
	}

	/**
	 * Override to return true if you want your emissive texture to respect lighting in the world
	 * <p>
	 * This will lower its overall brightness slightly but allow it to shade properly
	 * <p>
	 * It may also improve compatibility and/or visual aesthetics when rendering an emissive layer on a translucent model
	 */
	protected boolean shouldRespectWorldLighting(R renderState) {
		return false;
	}

	/**
	 * Override to return true to apply a view-space z-offset to the render buffer
	 * <p>
	 * Typically, you'd use this for armor rendering, but it can be worth trying if you have a custom RenderType object that isn't showing glowmasks
	 */
	protected boolean shouldAddZOffset(R renderState) {
		return getRenderer() instanceof GeoArmorRenderer;
	}

	/**
	 * Override to return a different lighting value if you want to customize the level of emissivity
	 */
	protected int getBrightness(R renderState) {
		return LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE;
	}

	/**
	 * Get the render type to use for this glowmask renderer, or null if the layer should not render
	 * <p>
	 * Uses a custom RenderType similar to {@link RenderLayers#eyes(Identifier)} by default
	 * <p>
	 * Automatically accounts for entity states like invisibility and glowing
	 */
	@Override
	protected @Nullable RenderLayer getRenderType(R renderState) {
		Identifier texture = getTextureResource(renderState);
		boolean respectLighting = shouldRespectWorldLighting(renderState);
		boolean zOffset = shouldAddZOffset(renderState);

		if (!(renderState instanceof EntityRenderState entityRenderState))
			return EmissiveRenderType.getRenderType(texture, false, respectLighting, zOffset);

		boolean invisible = entityRenderState.invisible;

		if (invisible && !renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false))
			return RenderLayers.itemEntityTranslucentCull(texture);

		if (entityRenderState.hasOutline()) {
			if (invisible)
				return RenderLayers.outlineNoCull(texture);

			return EmissiveRenderType.getRenderType(texture, true, respectLighting, zOffset);
		}

		return invisible ? null : EmissiveRenderType.getRenderType(texture, false, respectLighting, zOffset);
	}

	/**
	 * This is the method that is actually called by the render for your render layer to function
	 * <p>
	 * This is called <i>after</i> the animatable has been submitted for rendering, but before supplementary rendering submissions like nametags
	 */
    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, OrderedRenderCommandQueue renderTasks) {
        final int packedLight = renderPassInfo.packedLight();

        renderPassInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, getBrightness(renderPassInfo.renderState()));
        super.submitRenderTask(renderPassInfo, renderTasks);
        renderPassInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, packedLight);
	}

	/**
	 * Wrapper class to contain the various methods involved in handling the variants of this GeckoLib's emissive RenderType
	 */
	public static class EmissiveRenderType {
		private static final Map<Entry, RenderLayer> CACHE = new ConcurrentHashMap<>();

		/**
		 * Get or create a GeckoLib emissive RenderType instance for the given input variables
		 */
		public static RenderLayer getRenderType(Identifier texture, boolean outline, boolean respectLighting, boolean zOffset) {
			return CACHE.computeIfAbsent(new Entry(texture, outline, respectLighting, zOffset), EmissiveRenderType::buildNewInstance);
		}

		/**
		 * Create GeckoLib's custom {@link RenderPipeline} for emissive rendering since {@code EYES} isn't quite right
		 */
		private static RenderPipeline createRenderPipeline() {
			return RenderPipeline.builder(RenderPipelines.TRANSFORMS_PROJECTION_FOG_LIGHTING_SNIPPET)
					.withLocation(GeckoLibConstants.id("pipeline/emissive"))
					.withVertexShader("core/entity")
					.withFragmentShader("core/entity")
					.withShaderDefine("EMISSIVE")
					.withShaderDefine("NO_OVERLAY")
					.withShaderDefine("NO_CARDINAL_LIGHTING")
					.withSampler("Sampler0")
					.withBlend(BlendFunction.TRANSLUCENT)
					.withDepthWrite(false)
					.withCull(false)
					.withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
					.build();
		}

		/**
		 * Create GeckoLib's custom {@link RenderLayer} for emissive rendering since {@code EYES} isn't quite right
		 */
		private static RenderLayer buildNewInstance(Entry entry) {
			final RenderSetup.Builder builder = RenderSetup.builder(RENDER_PIPELINE)
					.texture("Sampler0", entry.texture)
					.layeringTransform(entry.zOffset ? LayeringTransform.VIEW_OFFSET_Z_LAYERING : LayeringTransform.NO_LAYERING)
					.outlineMode(entry.outline ? RenderSetup.OutlineMode.AFFECTS_OUTLINE : RenderSetup.OutlineMode.NONE)
					.translucent();

			if (entry.respectLighting) {
				return RenderLayer.of(GeckoLibConstants.MODID + "_entity_translucent_emissive", builder
						.useOverlay()
						.crumbling()
						.build());
			}

			return RenderLayer.of(GeckoLibConstants.MODID + "_emissive", builder
					.build());
		}

		/**
		 * Cached entry key for given input variables and texture
		 */
		public record Entry(Identifier texture, boolean outline, boolean respectLighting, boolean zOffset) {
			@Override
			public int hashCode() {
				return Objects.hash(this.texture, this.outline, this.respectLighting, this.zOffset);
			}
		}
	}
}
