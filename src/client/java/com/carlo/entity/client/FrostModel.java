package com.carlo.entity.client;

import com.carlo.entity.FrostEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FrostModel extends GeoModel<FrostEntity> {
    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "godeye_frost");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "textures/entity/frost.png");
    }

    @Override
    public Identifier getAnimationResource(FrostEntity animatable) {
        return Identifier.of("godeye", "godeye_frost");
    }
}
