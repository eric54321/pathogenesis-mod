package com.pathogenesis.entity.model;

import com.pathogenesis.entity.PhageEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Bacteriophage model — the iconic alien lander shape.
 *
 * Structure (top to bottom):
 *   HEAD     — 8x8x8 icosahedral protein capsid filled with DNA
 *   COLLAR   — 4x2x4 connector between head and tail
 *   TAIL     — 2x10x2 hollow tail shaft (injects DNA through this)
 *   BASEPLATE— 6x2x6 landing platform at bottom of tail
 *   4 LEGS   — thin struts angling outward-downward from the base plate,
 *               like a lunar lander or a spider's legs
 *
 * Texture layout (32x32):
 *   Left  16x16 (u 0-16):  head — deep blue-purple with faint hexagonal pattern
 *   Right 16x16 (u 16-32): tail/collar/legs — dark silver/grey metallic
 *
 * Animation: the whole model slowly rotates around Y axis (yaw) to give
 * the impression of a hovering probe scanning for targets.
 */
public class PhageModel extends EntityModel<PhageEntity> {

    private final ModelPart head;
    private final ModelPart collar;
    private final ModelPart tail;
    private final ModelPart basePlate;
    private final ModelPart legFront;
    private final ModelPart legBack;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public PhageModel(ModelPart root) {
        this.head      = root.getChild("head");
        this.collar    = root.getChild("collar");
        this.tail      = root.getChild("tail");
        this.basePlate = root.getChild("base_plate");
        this.legFront  = root.getChild("leg_front");
        this.legBack   = root.getChild("leg_back");
        this.legLeft   = root.getChild("leg_left");
        this.legRight  = root.getChild("leg_right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // HEAD — 8x8x8 protein capsid, sits at the top of the model
        // Pivot at (0, 14, 0) in model space — model space y increases downward
        root.addChild("head",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8, 8, 8),
            ModelTransform.pivot(0f, 14f, 0f));

        // COLLAR — narrow band connecting head to tail
        root.addChild("collar",
            ModelPartBuilder.create().uv(16, 0).cuboid(-2f, 0f, -2f, 4, 2, 4),
            ModelTransform.pivot(0f, 14f, 0f));

        // TAIL SHAFT — long thin tube below the collar
        root.addChild("tail",
            ModelPartBuilder.create().uv(16, 6).cuboid(-1f, 0f, -1f, 2, 10, 2),
            ModelTransform.pivot(0f, 16f, 0f));

        // BASE PLATE — wider platform at the bottom of the tail
        root.addChild("base_plate",
            ModelPartBuilder.create().uv(16, 0).cuboid(-3f, 0f, -3f, 6, 2, 6),
            ModelTransform.pivot(0f, 26f, 0f));

        // LEGS — 4 struts angling outward at ~45 degrees from the base plate
        // Each leg is a thin 1x5x1 rod, pivoted at the base plate edge, rotated outward-downward
        // Pitch of ~0.6 rad (~35 deg) angles them outward like a lander's legs

        // Front leg (negative Z direction)
        root.addChild("leg_front",
            ModelPartBuilder.create().uv(16, 10).cuboid(-0.5f, 0f, -0.5f, 1, 5, 1),
            ModelTransform.of(0f, 28f, -3f, 0.6f, 0f, 0f));

        // Back leg (positive Z direction)
        root.addChild("leg_back",
            ModelPartBuilder.create().uv(16, 10).cuboid(-0.5f, 0f, -0.5f, 1, 5, 1),
            ModelTransform.of(0f, 28f, 3f, -0.6f, 0f, 0f));

        // Left leg (negative X direction)
        root.addChild("leg_left",
            ModelPartBuilder.create().uv(16, 10).cuboid(-0.5f, 0f, -0.5f, 1, 5, 1),
            ModelTransform.of(-3f, 28f, 0f, 0f, 0f, -0.6f));

        // Right leg (positive X direction)
        root.addChild("leg_right",
            ModelPartBuilder.create().uv(16, 10).cuboid(-0.5f, 0f, -0.5f, 1, 5, 1),
            ModelTransform.of(3f, 28f, 0f, 0f, 0f, 0.6f));

        return TexturedModelData.of(modelData, 32, 32);
    }

    /**
     * Slow Y-axis rotation — the phage hovers and rotates like a drone
     * scanning for a target cell to land on.
     */
    @Override
    public void setAngles(PhageEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 0.02f;
        // Rotate all parts together around Y so the whole lander spins
        head.yaw      = spin;
        collar.yaw    = spin;
        tail.yaw      = spin;
        basePlate.yaw = spin;
        legFront.yaw  = spin;
        legBack.yaw   = spin;
        legLeft.yaw   = spin;
        legRight.yaw  = spin;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        head.render(matrices, vertices, light, overlay, color);
        collar.render(matrices, vertices, light, overlay, color);
        tail.render(matrices, vertices, light, overlay, color);
        basePlate.render(matrices, vertices, light, overlay, color);
        legFront.render(matrices, vertices, light, overlay, color);
        legBack.render(matrices, vertices, light, overlay, color);
        legLeft.render(matrices, vertices, light, overlay, color);
        legRight.render(matrices, vertices, light, overlay, color);
    }
}
