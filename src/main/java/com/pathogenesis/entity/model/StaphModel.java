package com.pathogenesis.entity.model;

import com.pathogenesis.entity.StaphEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Spherical coccus model for Staphylococcus aureus.
 * Single 8x8x8 cube — best sphere approximation using Minecraft cuboids.
 * Texture 32x32.
 */
public class StaphModel<T extends StaphEntity> extends EntityModel<T> {

    private final ModelPart body;

    public StaphModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();
        // Pivot Y=20: bottom of cube at Y=24 (ground), top at Y=16
        root.addChild("body",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -4f, -4f, 8, 8, 8),
            ModelTransform.pivot(0f, 20f, 0f));
        return TexturedModelData.of(md, 32, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        body.render(matrices, vertices, light, overlay, color);
    }
}
