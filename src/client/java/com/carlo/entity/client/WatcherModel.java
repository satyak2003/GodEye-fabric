package com.carlo.entity.client;

import com.carlo.entity.WatcherEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class WatcherModel extends GeoModel<WatcherEntity> {

    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "godeye_watcher");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "textures/entity/watcher.png");
    }

    @Override
    public Identifier getAnimationResource(WatcherEntity animatable) {
        return Identifier.of("godeye", "godeye_watcher");
    }
}
