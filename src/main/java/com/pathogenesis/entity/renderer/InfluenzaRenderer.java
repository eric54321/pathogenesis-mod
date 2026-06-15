package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.InfluenzaEntity;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renderer for Influenza. Reuses the VironModel shape for now.
 * Replace with a custom InfluenzaModel once designed in Blockbench.
 * Slightly larger shadow (0.3f vs 0.2f) to match the bigger hitbox.
 */
public class InfluenzaRenderer extends MobEntityRenderer<InfluenzaEntity, VironModel<InfluenzaEntity>> {

    // Shares the viron texture for now — swap for influenza.png once designed
    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/viron.png");

    public InfluenzaRenderer(EntityRendererFactory.Context context) {
        super(context, new VironModel<>(context.getPart(ModModelLayers.INFLUENZA)), 0.3f);
    }

    @Override
    public Identifier getTexture(InfluenzaEntity entity) {
        return TEXTURE;
    }
}
