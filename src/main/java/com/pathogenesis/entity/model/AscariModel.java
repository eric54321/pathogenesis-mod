package com.pathogenesis.entity.model;

import com.pathogenesis.entity.AscariEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Segmented upright roundworm for Ascaris.
 * Wide intimidating head tapering to a narrower tail — rears up like a cobra.
 * GENERIC_SCALE makes the whole model grow larger as the entity ages.
 * Texture 64x32.
 */
public class AscariModel<T extends AscariEntity> extends EntityModel<T> {

    private final ModelPart head;
    private final ModelPart ring1;
    private final ModelPart ring2;
    private final ModelPart tail;

    public AscariModel(ModelPart root) {
        this.head  = root.getChild("head");
        this.ring1 = root.getChild("ring1");
        this.ring2 = root.getChild("ring2");
        this.tail  = root.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();

        // Head — wide and menacing at the top
        root.addChild("head",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4f, 0f, -4f, 8, 6, 8),
            ModelTransform.pivot(0f, 4f, 0f));

        // First body ring — slightly narrower
        root.addChild("ring1",
            ModelPartBuilder.create().uv(0, 14).cuboid(-3f, 0f, -3f, 6, 5, 6),
            ModelTransform.pivot(0f, 10f, 0f));

        // Second body ring — same width, continues the taper feel
        root.addChild("ring2",
            ModelPartBuilder.create().uv(0, 14).cuboid(-3f, 0f, -3f, 6, 5, 6),
            ModelTransform.pivot(0f, 15f, 0f));

        // Tail — narrows to a point
        root.addChild("tail",
            ModelPartBuilder.create().uv(24, 14).cuboid(-2f, 0f, -2f, 4, 4, 4),
            ModelTransform.pivot(0f, 20f, 0f));

        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Subtle sinusoidal sway — worms undulate
        float sway = (float) Math.sin(animationProgress * 0.05f) * 0.15f;
        ring1.pitch = sway;
        ring2.pitch = -sway;
        head.pitch  = sway * 0.5f;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        head.render(matrices, vertices, light, overlay, color);
        ring1.render(matrices, vertices, light, overlay, color);
        ring2.render(matrices, vertices, light, overlay, color);
        tail.render(matrices, vertices, light, overlay, color);
    }
}
