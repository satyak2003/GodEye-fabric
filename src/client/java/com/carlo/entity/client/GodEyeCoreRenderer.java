package com.carlo.entity.client;

import com.carlo.entity.GodEyeCoreEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;


public class GodEyeCoreRenderer extends GeoEntityRenderer<GodEyeCoreEntity, net.minecraft.client.render.entity.state.LivingEntityRenderState> {
    public GodEyeCoreRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GodEyeCoreModel());
        this.withScale(2.5f);
        this.withRenderLayer(new software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer<>(this) {
            protected net.minecraft.util.Identifier getTextureResource(net.minecraft.client.render.entity.state.LivingEntityRenderState state) {
                return net.minecraft.util.Identifier.of("godeye", "textures/entity/core_e.png");
            }
        });
    }
}
