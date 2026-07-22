package com.carlo.entity.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

// THE FIX: Import GeoAnimatable instead of GeoEntity!
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

// Allow ANY GeckoLib object to use this layer, not just entities!
public class EmissiveGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final Identifier glowTexture;

    public EmissiveGeoLayer(GeoRenderer<T> entityRenderer, Identifier glowTexture) {
        super(entityRenderer);
        this.glowTexture = glowTexture;
    }

    @Override
    public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderLayer emissiveRenderLayer = RenderLayer.getEyes(this.glowTexture);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderLayer,
                bufferSource.getBuffer(emissiveRenderLayer), partialTick,
                15728880, packedOverlay, 1f, 1f, 1f, 1f);
    }
}