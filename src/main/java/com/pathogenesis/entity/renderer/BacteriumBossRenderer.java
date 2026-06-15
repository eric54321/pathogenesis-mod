package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.BacteriumBossEntity;
import com.pathogenesis.entity.model.BacteriumBossModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class BacteriumBossRenderer extends MobEntityRenderer<BacteriumBossEntity, BacteriumBossModel> {

    private static final Identifier TEXTURE_P1 =
        Identifier.of("pathogenesis", "textures/entity/bacterium_boss.png");
    private static final Identifier TEXTURE_P2 =
        Identifier.of("pathogenesis", "textures/entity/bacterium_boss_p2.png");

    public BacteriumBossRenderer(EntityRendererFactory.Context context) {
        super(context, new BacteriumBossModel(context.getPart(ModModelLayers.BACTERIUM_BOSS)), 6.0f);
    }

    @Override
    public Identifier getTexture(BacteriumBossEntity entity) {
        return entity.isPhase2() ? TEXTURE_P2 : TEXTURE_P1;
    }
}
