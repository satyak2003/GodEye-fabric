package com.carlo.entity.client;

import com.carlo.entity.WatcherEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

public class WatcherRenderer extends GeoEntityRenderer<WatcherEntity, LivingEntityRenderState> {
    public WatcherRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new WatcherModel());
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
        withScale(1.5f, 1.5f);
    }
}

