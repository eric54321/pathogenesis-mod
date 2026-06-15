package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.RogueCellEntity;
import com.pathogenesis.entity.model.RogueCellModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RogueCellRenderer extends MobEntityRenderer<RogueCellEntity, RogueCellModel> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/rogue_cell.png");

    public RogueCellRenderer(EntityRendererFactory.Context context) {
        super(context,
            new RogueCellModel(context.getPart(ModModelLayers.ROGUE_CELL)),
            0.4f);
    }

    @Override
    public Identifier getTexture(RogueCellEntity entity) {
        return TEXTURE;
    }
}
