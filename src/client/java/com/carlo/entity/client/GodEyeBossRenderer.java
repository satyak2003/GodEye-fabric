package com.carlo.entity.client;

import com.carlo.entity.GodEyeBossEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class GodEyeBossRenderer extends GeoEntityRenderer<GodEyeBossEntity> {
    public GodEyeBossRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GodEyeBossModel());
        this.addRenderLayer(new EmissiveGeoLayer<>(this, new net.minecraft.util.Identifier("godeye", "textures/entity/pupil_v2_e.png")));
    }

    @Override
    public void render(GodEyeBossEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, net.minecraft.client.render.VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();

        // Scale the boss up! Change these numbers to make him bigger or smaller.
        // 4.0f means he is 4x larger than his Blockbench model.
        poseStack.scale(4.0f, 4.0f, 4.0f);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}