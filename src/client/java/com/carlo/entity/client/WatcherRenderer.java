package com.carlo.entity.client;

import com.carlo.entity.WatcherEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class WatcherRenderer extends GeoEntityRenderer<WatcherEntity> {
    public WatcherRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new WatcherModel());
        this.addRenderLayer(new EmissiveGeoLayer<>(this, new net.minecraft.util.Identifier("godeye", "textures/entity/frost_e.png")));
    }
}