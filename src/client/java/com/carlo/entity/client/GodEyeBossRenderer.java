package com.carlo.entity.client;

import com.carlo.entity.GodEyeBossEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class GodEyeBossRenderer extends GeoEntityRenderer<GodEyeBossEntity, net.minecraft.client.render.entity.state.LivingEntityRenderState> {
    public GodEyeBossRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GodEyeBossModel());
        this.withScale(4.0f);
        this.withRenderLayer(new software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer<>(this) {
            protected net.minecraft.util.Identifier getTextureResource(net.minecraft.client.render.entity.state.LivingEntityRenderState state) {
                return net.minecraft.util.Identifier.of("godeye", "textures/entity/pupil_v2_e.png");
            }
        });
    }
}
