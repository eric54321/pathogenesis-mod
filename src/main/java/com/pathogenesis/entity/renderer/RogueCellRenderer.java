package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.RogueCellEntity;
import com.pathogenesis.entity.model.RogueCellModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RogueCellRenderer extends MobEntityRenderer<RogueCellEntity, RogueCellModel> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/rogue_cell.png");

    // Scale factor: model blob is ~0.625 blocks, 2.5x makes it ~1.56 blocks — big and threatening
    private static final float SCALE = 2.5f;

    public RogueCellRenderer(EntityRendererFactory.Context context) {
        super(context,
            new RogueCellModel(context.getPart(ModModelLayers.ROGUE_CELL)),
            0.9f);
    }

    @Override
    protected void scale(RogueCellEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTexture(RogueCellEntity entity) {
        return TEXTURE;
    }
}
