package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.AscariEntity;
import com.pathogenesis.entity.model.AscariModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class AscariRenderer extends MobEntityRenderer<AscariEntity, AscariModel<AscariEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/ascari.png");

    public AscariRenderer(EntityRendererFactory.Context context) {
        super(context, new AscariModel<>(context.getPart(ModModelLayers.ASCARI)), 0.6f);
    }

    @Override
    public Identifier getTexture(AscariEntity entity) {
        return TEXTURE;
    }
}
