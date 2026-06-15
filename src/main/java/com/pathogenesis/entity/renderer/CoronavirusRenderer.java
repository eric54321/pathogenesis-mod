package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.CoronavirusEntity;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renderer for Coronavirus. Reuses VironModel until a crown-shaped
 * Blockbench model is created. Larger shadow (0.4f) matches bigger hitbox.
 * TODO: create coronavirus.bbmodel with crown spike arrangement.
 */
public class CoronavirusRenderer extends MobEntityRenderer<CoronavirusEntity, VironModel> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/viron.png");

    public CoronavirusRenderer(EntityRendererFactory.Context context) {
        super(context, new VironModel(context.getPart(ModModelLayers.CORONAVIRUS)), 0.4f);
    }

    @Override
    public Identifier getTexture(CoronavirusEntity entity) {
        return TEXTURE;
    }
}
