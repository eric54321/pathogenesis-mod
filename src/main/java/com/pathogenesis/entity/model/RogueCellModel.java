package com.pathogenesis.entity.model;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Vertical disc model — stands upright like a coin floating in the air.
 * Octagonal cross-section for maximum circularity.
 * Texture 64x32 with distinct color zones per part.
 */
public class RogueCellModel extends EntityModel<RogueCellEntity> {

    private final ModelPart core;
    private final ModelPart bumpTop;
    private final ModelPart bumpBot;
    private final ModelPart bumpLeft;
    private final ModelPart bumpRight;
    private final ModelPart cornerTL;
    private final ModelPart cornerTR;
    private final ModelPart cornerBL;
    private final ModelPart cornerBR;
    private final ModelPart nucleus;

    public RogueCellModel(ModelPart root) {
        this.core      = root.getChild("core");
        this.bumpTop   = root.getChild("bump_top");
        this.bumpBot   = root.getChild("bump_bot");
        this.bumpLeft  = root.getChild("bump_left");
        this.bumpRight = root.getChild("bump_right");
        this.cornerTL  = root.getChild("corner_tl");
        this.cornerTR  = root.getChild("corner_tr");
        this.cornerBL  = root.getChild("corner_bl");
        this.cornerBR  = root.getChild("corner_br");
        this.nucleus   = root.getChild("nucleus");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        // Core: wide and tall but thin — vertical disc
        r.addChild("core",
            ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4f, -4f, -1.5f, 8, 8, 3),
            ModelTransform.pivot(0f, 20f, 0f));

        // Nucleus — bright center dot
        r.addChild("nucleus",
            ModelPartBuilder.create().uv(22, 0)
                .cuboid(-1.5f, -1.5f, -2f, 3, 3, 3),
            ModelTransform.pivot(0f, 20f, 0f));

        // Top bump — extends upward
        r.addChild("bump_top",
            ModelPartBuilder.create().uv(0, 12)
                .cuboid(-3f, -2f, -1.5f, 6, 2, 3),
            ModelTransform.pivot(0f, 16f, 0f));

        // Bottom bump
        r.addChild("bump_bot",
            ModelPartBuilder.create().uv(18, 12)
                .cuboid(-3f, 0f, -1.5f, 6, 2, 3),
            ModelTransform.pivot(0f, 24f, 0f));

        // Left bump
        r.addChild("bump_left",
            ModelPartBuilder.create().uv(0, 18)
                .cuboid(-2f, -3f, -1.5f, 2, 6, 3),
            ModelTransform.pivot(-4f, 20f, 0f));

        // Right bump
        r.addChild("bump_right",
            ModelPartBuilder.create().uv(10, 18)
                .cuboid(0f, -3f, -1.5f, 2, 6, 3),
            ModelTransform.pivot(4f, 20f, 0f));

        // Diagonal corners — fill the octagon gaps
        r.addChild("corner_tl",
            ModelPartBuilder.create().uv(20, 18)
                .cuboid(-2f, -2f, -1.5f, 2, 2, 3),
            ModelTransform.pivot(-3f, 18f, 0f));

        r.addChild("corner_tr",
            ModelPartBuilder.create().uv(30, 18)
                .cuboid(0f, -2f, -1.5f, 2, 2, 3),
            ModelTransform.pivot(3f, 18f, 0f));

        r.addChild("corner_bl",
            ModelPartBuilder.create().uv(40, 18)
                .cuboid(-2f, 0f, -1.5f, 2, 2, 3),
            ModelTransform.pivot(-3f, 22f, 0f));

        r.addChild("corner_br",
            ModelPartBuilder.create().uv(50, 18)
                .cuboid(0f, 0f, -1.5f, 2, 2, 3),
            ModelTransform.pivot(3f, 22f, 0f));

        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(RogueCellEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Slow spin around Y axis
        float spin = animationProgress * 0.04f;
        core.yaw      = spin;
        bumpTop.yaw   = spin;
        bumpBot.yaw   = spin;
        bumpLeft.yaw  = spin;
        bumpRight.yaw = spin;
        cornerTL.yaw  = spin;
        cornerTR.yaw  = spin;
        cornerBL.yaw  = spin;
        cornerBR.yaw  = spin;
        nucleus.yaw   = spin;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        core.render(matrices, vertices, light, overlay, color);
        bumpTop.render(matrices, vertices, light, overlay, color);
        bumpBot.render(matrices, vertices, light, overlay, color);
        bumpLeft.render(matrices, vertices, light, overlay, color);
        bumpRight.render(matrices, vertices, light, overlay, color);
        cornerTL.render(matrices, vertices, light, overlay, color);
        cornerTR.render(matrices, vertices, light, overlay, color);
        cornerBL.render(matrices, vertices, light, overlay, color);
        cornerBR.render(matrices, vertices, light, overlay, color);
        nucleus.render(matrices, vertices, light, overlay, color);
    }
}
