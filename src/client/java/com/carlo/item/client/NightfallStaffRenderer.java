package com.carlo.item.client;

import com.carlo.item.NightfallStaffItem;
import com.carlo.entity.client.EmissiveGeoLayer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.minecraft.util.Identifier;

public class NightfallStaffRenderer extends GeoItemRenderer<NightfallStaffItem> {
    public NightfallStaffRenderer() {
        super(new NightfallStaffModel());

        // Adds the glowing crystal texture layer!
        this.addRenderLayer(new EmissiveGeoLayer<>(this, new Identifier("godeye", "textures/item/nightfall_e.png")));
    }
}