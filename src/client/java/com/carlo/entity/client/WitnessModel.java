package com.carlo.entity.client;

import com.carlo.entity.WitnessEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WitnessModel extends GeoModel<WitnessEntity> {
    @Override
    public Identifier getModelResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "godeye_witness");
    }

    @Override
    public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.of("godeye", "textures/entity/witness.png");
    }

    @Override
    public Identifier getAnimationResource(WitnessEntity animatable) {
        // No animation file exists, returning the base identifier handles it gracefully in GeckoLib 5
        return Identifier.of("godeye", "godeye_witness");
    }
}
