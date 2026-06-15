package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.VironEntity;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renderer for the Viron — wires together the VironModel and its texture.
 * Shadow radius 0.2f matches the small hitbox of the entity.
 */
public class VironRenderer extends MobEntityRenderer<VironEntity, VironModel<VironEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/viron.png");

    public VironRenderer(EntityRendererFactory.Context context) {
        super(context, new VironModel<>(context.getPart(ModModelLayers.VIRON)), 0.2f);
    }

    @Override
    public Identifier getTexture(VironEntity entity) {
        return TEXTURE;
    }
}
