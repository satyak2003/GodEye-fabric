package com.carlo.entity.client;

import com.carlo.entity.GodEyeCoreEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class GodEyeCoreModel extends GeoModel<GodEyeCoreEntity> {

    @Override
    public Identifier getModelResource(GodEyeCoreEntity object) {
        return new Identifier("godeye", "geo/godeye_core.geo.json");
    }

    @Override
    public Identifier getTextureResource(GodEyeCoreEntity object) {
        return new Identifier("godeye", "textures/entity/core.png");
    }

    @Override
    public Identifier getAnimationResource(GodEyeCoreEntity animatable) {
        return new Identifier("godeye", "animations/godeye_core.animation.json");
    }
}