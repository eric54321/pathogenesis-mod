package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.BacteriumBossEntity;
import com.pathogenesis.entity.model.BacteriumBossModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class BacteriumBossRenderer extends MobEntityRenderer<BacteriumBossEntity, BacteriumBossModel> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/bacterium_boss.png");

    public BacteriumBossRenderer(EntityRendererFactory.Context context) {
        super(context, new BacteriumBossModel(context.getPart(ModModelLayers.BACTERIUM_BOSS)), 1.2f);
    }

    @Override
    public Identifier getTexture(BacteriumBossEntity entity) {
        return TEXTURE;
    }
}
