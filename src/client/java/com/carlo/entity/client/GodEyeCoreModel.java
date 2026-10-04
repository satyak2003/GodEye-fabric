package com.carlo.entity.client;

import com.carlo.entity.GodEyeCoreEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class GodEyeCoreModel extends GeoModel<GodEyeCoreEntity> {

    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "geo/godeye_core.geo.json");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "textures/entity/core.png");
    }

    @Override
    public Identifier getAnimationResource(GodEyeCoreEntity animatable) {
        return Identifier.of("godeye", "animations/godeye_core.animation.json");
    }
}
