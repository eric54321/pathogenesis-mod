package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.TaeniaEntity;
import com.pathogenesis.entity.model.TaeniaModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class TaeniaRenderer extends MobEntityRenderer<TaeniaEntity, TaeniaModel<TaeniaEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/taenia.png");

    public TaeniaRenderer(EntityRendererFactory.Context context) {
        super(context, new TaeniaModel<>(context.getPart(ModModelLayers.TAENIA)), 0.5f);
    }

    @Override
    public Identifier getTexture(TaeniaEntity entity) {
        return TEXTURE;
    }
}
