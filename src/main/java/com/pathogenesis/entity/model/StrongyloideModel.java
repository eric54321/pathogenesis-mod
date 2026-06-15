package com.pathogenesis.entity.model;

import com.pathogenesis.entity.StrongyloideEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Thin sinuous threadworm model for Strongyloides.
 * A wide head on a very slim elongated body — looks like a sinister needle.
 * Larvae use the same model but GENERIC_SCALE renders them smaller.
 * GENERIC_SCALE grows the adult larger over time.
 * Texture 32x32.
 */
public class StrongyloideModel<T extends StrongyloideEntity> extends EntityModel<T> {

    private final ModelPart head;
    private final ModelPart body;

    public StrongyloideModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();

        // Wide head — slightly alarming compared to the thin body
        root.addChild("head",
            ModelPartBuilder.create().uv(0, 0).cuboid(-3f, 0f, -3f, 6, 5, 6),
            ModelTransform.pivot(0f, 5f, 0f));

        // Very thin elongated body — thread-like
        root.addChild("body",
            ModelPartBuilder.create().uv(0, 11).cuboid(-1f, 0f, -1f, 3, 14, 3),
            ModelTransform.pivot(0f, 10f, 0f));

        return TexturedModelData.of(md, 32, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Fast sinuous whipping motion — threadworms move erratically
        float whip = (float) Math.sin(animationProgress * 0.1f) * 0.25f;
        body.pitch = whip;
        head.pitch = -whip * 0.5f;
        body.roll  = (float) Math.sin(animationProgress * 0.07f) * 0.1f;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        head.render(matrices, vertices, light, overlay, color);
        body.render(matrices, vertices, light, overlay, color);
    }
}
