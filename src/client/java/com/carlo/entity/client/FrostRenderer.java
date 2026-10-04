package com.carlo.entity.client;

import com.carlo.entity.FrostEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

public class FrostRenderer extends GeoEntityRenderer<FrostEntity, net.minecraft.client.render.entity.state.LivingEntityRenderState> {
    public FrostRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new FrostModel());
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
