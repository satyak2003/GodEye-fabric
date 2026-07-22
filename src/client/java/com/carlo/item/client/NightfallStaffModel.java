package com.carlo.item.client;

import com.carlo.item.NightfallStaffItem;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class NightfallStaffModel extends GeoModel<NightfallStaffItem> {
    @Override
    public Identifier getModelResource(NightfallStaffItem object) {
        return new Identifier("godeye", "geo/godeye_nightfall.geo.json");
    }

    @Override
    public Identifier getTextureResource(NightfallStaffItem object) {
        return new Identifier("godeye", "textures/item/nightfall.png");
    }

    @Override
    public Identifier getAnimationResource(NightfallStaffItem animatable) {
        return new Identifier("godeye", "animations/godeye_nightfall.animation.json");
    }
}