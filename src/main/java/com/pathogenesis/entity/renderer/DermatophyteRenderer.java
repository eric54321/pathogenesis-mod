package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.DermatophyteEntity;
import com.pathogenesis.entity.model.DermatophyteModel;
import com.pathogenesis.init.ModModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class DermatophyteRenderer extends MobEntityRenderer<DermatophyteEntity, DermatophyteModel<DermatophyteEntity>> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/dermatophyte.png");

    public DermatophyteRenderer(EntityRendererFactory.Context context) {
        super(context, new DermatophyteModel<>(context.getPart(ModModelLayers.DERMATOPHYTE)), 0.25f);
    }

    @Override
    public Identifier getTexture(DermatophyteEntity entity) {
        return TEXTURE;
    }
}
