package com.pathogenesis.entity.model;

import com.pathogenesis.entity.StreptococcusEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Diplococcus model for Streptococcus — two 6x6x6 spheres side by side.
 * In biology, Streptococcus grows in chains; the diplococcus pair is its most
 * recognisable microscope form and immediately distinguishes it from Staph.
 * Texture 32x32 (both spheres share the same UV region).
 */
public class StreptococcusModel<T extends StreptococcusEntity> extends EntityModel<T> {

    private final ModelPart sphereLeft;
    private final ModelPart sphereRight;

    public StreptococcusModel(ModelPart root) {
        this.sphereLeft  = root.getChild("sphere_left");
        this.sphereRight = root.getChild("sphere_right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();

        // Left sphere: X from -6 to 0, bottom at Y=24
        root.addChild("sphere_left",
            ModelPartBuilder.create().uv(0, 0).cuboid(-3f, -3f, -3f, 6, 6, 6),
            ModelTransform.pivot(-3f, 21f, 0f));

        // Right sphere: X from 0 to 6, touching the left sphere
        root.addChild("sphere_right",
            ModelPartBuilder.create().uv(0, 0).cuboid(-3f, -3f, -3f, 6, 6, 6),
            ModelTransform.pivot(3f, 21f, 0f));

        return TexturedModelData.of(md, 32, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        sphereLeft.render(matrices, vertices, light, overlay, color);
        sphereRight.render(matrices, vertices, light, overlay, color);
    }
}
