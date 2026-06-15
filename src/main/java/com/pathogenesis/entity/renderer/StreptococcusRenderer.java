package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.StreptococcusEntity;
import com.pathogenesis.entity.model.StreptococcusModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class StreptococcusRenderer extends MobEntityRenderer<StreptococcusEntity, StreptococcusModel<StreptococcusEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/streptococcus.png");

    public StreptococcusRenderer(EntityRendererFactory.Context context) {
        super(context, new StreptococcusModel<>(context.getPart(ModModelLayers.STREPTOCOCCUS)), 0.4f);
    }

    @Override
    public Identifier getTexture(StreptococcusEntity entity) {
        return TEXTURE;
    }
}
