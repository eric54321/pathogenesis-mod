package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.PhageEntity;
import com.pathogenesis.entity.model.PhageModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class PhageRenderer extends MobEntityRenderer<PhageEntity, PhageModel> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/phage.png");

    public PhageRenderer(EntityRendererFactory.Context context) {
        super(context, new PhageModel(context.getPart(ModModelLayers.PHAGE)), 0.35f);
    }

    @Override
    public Identifier getTexture(PhageEntity entity) {
        return TEXTURE;
    }
}
