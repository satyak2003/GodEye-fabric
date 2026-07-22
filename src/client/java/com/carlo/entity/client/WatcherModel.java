package com.carlo.entity.client;

import com.carlo.entity.WatcherEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class WatcherModel extends GeoModel<WatcherEntity> {

    @Override
    public Identifier getModelResource(WatcherEntity object) {
        // Updated to your new Frost model
        return new Identifier("godeye", "geo/godeye_frost.geo.json");
    }

    @Override
    public Identifier getTextureResource(WatcherEntity object) {
        // Updated to your new Frost texture
        return new Identifier("godeye", "textures/entity/frost.png");
    }

    @Override
    public Identifier getAnimationResource(WatcherEntity animatable) {
        // Updated to your new Frost animation file
        return new Identifier("godeye", "animations/godeye_frost.animation.json");
    }
}