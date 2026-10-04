package com.carlo.item.client;

import com.carlo.item.NightfallStaffItem;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class NightfallStaffModel extends GeoModel<NightfallStaffItem> {
    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "geo/godeye_nightfall.geo.json");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "textures/item/nightfall.png");
    }

    @Override
    public Identifier getAnimationResource(NightfallStaffItem animatable) {
        return Identifier.of("godeye", "animations/godeye_nightfall.animation.json");
    }
}
