package com.carlo.entity.client;

import com.carlo.entity.GodEyeCoreEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;


public class GodEyeCoreRenderer extends GeoEntityRenderer<GodEyeCoreEntity> {
    public GodEyeCoreRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GodEyeCoreModel());
        this.addRenderLayer(new EmissiveGeoLayer<>(this, new net.minecraft.util.Identifier("godeye", "textures/entity/core_e.png")));
    }
    @Override
    public void render(com.carlo.entity.GodEyeCoreEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        // Scale X, Y, Z. Change 3.0F to whatever size fits your altar perfectly!
        poseStack.scale(3.0F, 3.0F, 3.0F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}

