package com.pathogenesis.entity.model;

import com.pathogenesis.entity.DermatophyteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Fungal hyphae model for Dermatophyte — a narrow upright shaft with a wider cap.
 * Real dermatophytes grow as branching filaments (hyphae) that penetrate through
 * the keratin layers of skin. The cap represents the fruiting body that releases spores.
 * Texture 32x32.
 */
public class DermatophyteModel<T extends DermatophyteEntity> extends EntityModel<T> {

    private final ModelPart shaft;
    private final ModelPart cap;

    public DermatophyteModel(ModelPart root) {
        this.shaft = root.getChild("shaft");
        this.cap   = root.getChild("cap");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();

        // Narrow upright shaft: 4x12x4, Y from 12 to 24 (ground)
        root.addChild("shaft",
            ModelPartBuilder.create().uv(0, 0).cuboid(-2f, 0f, -2f, 4, 12, 4),
            ModelTransform.pivot(0f, 12f, 0f));

        // Wider cap sitting on top of the shaft: 6x2x6, Y from 10 to 12
        root.addChild("cap",
            ModelPartBuilder.create().uv(0, 16).cuboid(-3f, 0f, -3f, 6, 2, 6),
            ModelTransform.pivot(0f, 10f, 0f));

        return TexturedModelData.of(md, 32, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        shaft.render(matrices, vertices, light, overlay, color);
        cap.render(matrices, vertices, light, overlay, color);
    }
}
