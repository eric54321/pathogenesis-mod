package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.StrongyloideEntity;
import com.pathogenesis.entity.model.StrongyloideModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class StrongyloideRenderer extends MobEntityRenderer<StrongyloideEntity, StrongyloideModel<StrongyloideEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/strongyloide.png");

    public StrongyloideRenderer(EntityRendererFactory.Context context) {
        super(context, new StrongyloideModel<>(context.getPart(ModModelLayers.STRONGYLOIDE)), 0.4f);
    }

    @Override
    public Identifier getTexture(StrongyloideEntity entity) {
        return TEXTURE;
    }
}
