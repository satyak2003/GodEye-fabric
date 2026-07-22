package com.carlo.entity.client;

import com.carlo.entity.GodEyeBossEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import net.minecraft.util.Identifier;

public class GodEyeBossModel extends GeoModel<GodEyeBossEntity> {

    @Override
    public Identifier getModelResource(GodEyeBossEntity object) {
        return new Identifier("godeye", "geo/godeye_boss_v2.geo.json");
    }

    @Override
    public Identifier getTextureResource(GodEyeBossEntity object) {
        return new Identifier("godeye", "textures/entity/pupil_v2.png");
    }

    @Override
    public Identifier getAnimationResource(GodEyeBossEntity animatable) {
        return new Identifier("godeye", "animations/godeye_boss_v2.animation.json");
    }

    @Override
    public void setCustomAnimations(GodEyeBossEntity animatable, long instanceId, AnimationState<GodEyeBossEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // Fetch the pupil bone exactly as it is named in your .geo.json
        CoreGeoBone pupil = getAnimationProcessor().getBone("pupil");

        if (pupil != null) {
            // Get the entity's current head pitch and yaw directly from the entity
            float yaw = animatable.getHeadYaw();
            float pitch = animatable.getPitch();

            // Apply it to the pupil bone so it constantly stares at its target
            // Math.PI / 180F converts degrees to radians for GeckoLib
            pupil.setRotX(pitch * ((float) Math.PI / 180F));
            pupil.setRotY(yaw * ((float) Math.PI / 180F));
        }
    }
}