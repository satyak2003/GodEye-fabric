package com.carlo.item.client;

import com.carlo.item.NightfallStaffItem;

import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.minecraft.util.Identifier;

public class NightfallStaffRenderer extends GeoItemRenderer<NightfallStaffItem> {
    public NightfallStaffRenderer() {
        super(new NightfallStaffModel());

        // Adds the glowing crystal texture layer!
        this.withRenderLayer(new software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer<>(this) { protected net.minecraft.util.Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState state) { return net.minecraft.util.Identifier.of("godeye", "textures/item/nightfall_e.png"); } });
    }
}
