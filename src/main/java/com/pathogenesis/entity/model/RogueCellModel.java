package com.pathogenesis.entity.model;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Rounder blob model — approximates a sphere using a cross of overlapping cubes.
 * Texture layout (64x64):
 *   core      uv(0,0)  — 8x8x8
 *   topCap    uv(0,16) — 6x2x6
 *   botCap    uv(24,16)— 6x2x6
 *   frontBump uv(0,24) — 6x6x2
 *   backBump  uv(20,24)— 6x6x2
 *   leftBump  uv(0,32) — 2x6x6
 *   rightBump uv(16,32)— 2x6x6
 *   nucleus   uv(40,0) — 4x4x4
 */
public class RogueCellModel extends EntityModel<RogueCellEntity> {

    private final ModelPart core;
    private final ModelPart topCap;
    private final ModelPart botCap;
    private final ModelPart frontBump;
    private final ModelPart backBump;
    private final ModelPart leftBump;
    private final ModelPart rightBump;
    private final ModelPart nucleus;

    public RogueCellModel(ModelPart root) {
        this.core      = root.getChild("core");
        this.topCap    = root.getChild("top_cap");
        this.botCap    = root.getChild("bot_cap");
        this.frontBump = root.getChild("front_bump");
        this.backBump  = root.getChild("back_bump");
        this.leftBump  = root.getChild("left_bump");
        this.rightBump = root.getChild("right_bump");
        this.nucleus   = root.getChild("nucleus");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Central cube — the widest part of the sphere
        root.addChild("core",
            ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4f, -4f, -4f, 8, 8, 8),
            ModelTransform.pivot(0f, 20f, 0f));

        // Top cap — narrows the top like a sphere
        root.addChild("top_cap",
            ModelPartBuilder.create().uv(0, 16)
                .cuboid(-3f, -2f, -3f, 6, 2, 6),
            ModelTransform.pivot(0f, 16f, 0f));

        // Bottom cap — narrows the bottom
        root.addChild("bot_cap",
            ModelPartBuilder.create().uv(24, 16)
                .cuboid(-3f, 0f, -3f, 6, 2, 6),
            ModelTransform.pivot(0f, 24f, 0f));

        // Front bump — rounds the front face
        root.addChild("front_bump",
            ModelPartBuilder.create().uv(0, 24)
                .cuboid(-3f, -3f, -2f, 6, 6, 2),
            ModelTransform.pivot(0f, 20f, -4f));

        // Back bump
        root.addChild("back_bump",
            ModelPartBuilder.create().uv(20, 24)
                .cuboid(-3f, -3f, 0f, 6, 6, 2),
            ModelTransform.pivot(0f, 20f, 4f));

        // Left bump
        root.addChild("left_bump",
            ModelPartBuilder.create().uv(0, 32)
                .cuboid(-2f, -3f, -3f, 2, 6, 6),
            ModelTransform.pivot(-4f, 20f, 0f));

        // Right bump
        root.addChild("right_bump",
            ModelPartBuilder.create().uv(16, 32)
                .cuboid(0f, -3f, -3f, 2, 6, 6),
            ModelTransform.pivot(4f, 20f, 0f));

        // Nucleus — small inner sphere offset slightly
        root.addChild("nucleus",
            ModelPartBuilder.create().uv(40, 0)
                .cuboid(-2f, -2f, -2f, 4, 4, 4),
            ModelTransform.pivot(1f, 20f, 1f));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(RogueCellEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Slow gentle pulsing — the whole blob breathes
        float pulse = (float) Math.sin(animationProgress * 0.08f) * 0.08f;
        core.xScale = 1f + pulse;
        core.yScale = 1f - pulse * 0.5f;
        core.zScale = 1f + pulse;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        core.render(matrices, vertices, light, overlay, color);
        topCap.render(matrices, vertices, light, overlay, color);
        botCap.render(matrices, vertices, light, overlay, color);
        frontBump.render(matrices, vertices, light, overlay, color);
        backBump.render(matrices, vertices, light, overlay, color);
        leftBump.render(matrices, vertices, light, overlay, color);
        rightBump.render(matrices, vertices, light, overlay, color);
        nucleus.render(matrices, vertices, light, overlay, color);
    }
}
