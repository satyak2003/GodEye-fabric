package com.carlo.entity.client;

import com.carlo.entity.WitnessEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

public class WitnessRenderer extends GeoEntityRenderer<WitnessEntity, LivingEntityRenderState> {
    public WitnessRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new WitnessModel());
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
        withScale(0.6f, 0.6f);
    }
}

