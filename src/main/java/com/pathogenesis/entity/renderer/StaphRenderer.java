package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.StaphEntity;
import com.pathogenesis.entity.model.StaphModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class StaphRenderer extends MobEntityRenderer<StaphEntity, StaphModel<StaphEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/staph.png");

    public StaphRenderer(EntityRendererFactory.Context context) {
        super(context, new StaphModel<>(context.getPart(ModModelLayers.STAPH)), 0.3f);
    }

    @Override
    public Identifier getTexture(StaphEntity entity) {
        return TEXTURE;
    }
}
