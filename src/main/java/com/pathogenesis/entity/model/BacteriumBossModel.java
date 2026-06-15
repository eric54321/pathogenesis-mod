package com.pathogenesis.entity.model;

import com.pathogenesis.entity.BacteriumBossEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Bacillus (rod-shaped bacterium) model.
 *
 * Main rod body with rounded end caps and 4 flagella (whip-like appendages)
 * that wave to show the boss is alive and moving.
 * Texture sheet: 64x64.
 */
public class BacteriumBossModel extends EntityModel<BacteriumBossEntity> {

    private final ModelPart body;
    private final ModelPart capNorth;
    private final ModelPart capSouth;
    private final ModelPart flag1;
    private final ModelPart flag2;
    private final ModelPart flag3;
    private final ModelPart flag4;

    public BacteriumBossModel(ModelPart root) {
        this.body     = root.getChild("body");
        this.capNorth = root.getChild("cap_north");
        this.capSouth = root.getChild("cap_south");
        this.flag1    = root.getChild("flag1");
        this.flag2    = root.getChild("flag2");
        this.flag3    = root.getChild("flag3");
        this.flag4    = root.getChild("flag4");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        // Main rod body: 10 wide, 10 tall, 18 long — elongated bacillus shape
        // Pivot at (0, 14, 0) so the bottom of the body is near ground (y=24)
        r.addChild("body",
            ModelPartBuilder.create().uv(0, 0).cuboid(-5f, -5f, -9f, 10, 10, 18),
            ModelTransform.pivot(0f, 14f, 0f));

        // Rounded north cap: 8x8x4 cap at the north end of the rod
        r.addChild("cap_north",
            ModelPartBuilder.create().uv(0, 28).cuboid(-4f, -4f, -13f, 8, 8, 4),
            ModelTransform.pivot(0f, 14f, 0f));

        // Rounded south cap: 8x8x4 cap at the south end of the rod
        r.addChild("cap_south",
            ModelPartBuilder.create().uv(24, 28).cuboid(-4f, -4f, 9f, 8, 8, 4),
            ModelTransform.pivot(0f, 14f, 0f));

        // 4 flagella — thin whip-like appendages from the south end, angled outward
        // They extend in the -Z direction so yaw rotation spreads them around
        r.addChild("flag1",
            ModelPartBuilder.create().uv(0, 40).cuboid(-1f, -1f, -16f, 2, 2, 16),
            ModelTransform.of(0f, 14f, 9f, 0f, 0f, 0.5f));

        r.addChild("flag2",
            ModelPartBuilder.create().uv(0, 40).cuboid(-1f, -1f, -16f, 2, 2, 16),
            ModelTransform.of(0f, 14f, 9f, 0f, (float)(Math.PI / 2), 0.5f));

        r.addChild("flag3",
            ModelPartBuilder.create().uv(0, 40).cuboid(-1f, -1f, -16f, 2, 2, 16),
            ModelTransform.of(0f, 14f, 9f, 0f, (float)Math.PI, 0.5f));

        r.addChild("flag4",
            ModelPartBuilder.create().uv(0, 40).cuboid(-1f, -1f, -16f, 2, 2, 16),
            ModelTransform.of(0f, 14f, 9f, 0f, (float)(3 * Math.PI / 2), 0.5f));

        return TexturedModelData.of(md, 64, 64);
    }

    @Override
    public void setAngles(BacteriumBossEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Slow rotation of the rod body (bacteria tumble when moving)
        float roll = animationProgress * 0.03f;
        body.roll     = roll;
        capNorth.roll = roll;
        capSouth.roll = roll;

        // Flagella wave like whips — each on a slightly offset sine wave
        float baseWave = (float)Math.sin(animationProgress * 0.12f) * 0.6f;
        flag1.pitch = baseWave;
        flag2.pitch = (float)Math.sin(animationProgress * 0.12f + Math.PI * 0.5) * 0.6f;
        flag3.pitch = (float)Math.sin(animationProgress * 0.12f + Math.PI) * 0.6f;
        flag4.pitch = (float)Math.sin(animationProgress * 0.12f + Math.PI * 1.5) * 0.6f;

        // Boss faces its target
        body.yaw     = headYaw * ((float)Math.PI / 180f);
        capNorth.yaw = body.yaw;
        capSouth.yaw = body.yaw;
        flag1.yaw    = body.yaw;
        flag2.yaw    = body.yaw + (float)(Math.PI / 2);
        flag3.yaw    = body.yaw + (float)Math.PI;
        flag4.yaw    = body.yaw + (float)(3 * Math.PI / 2);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        body.render(matrices, vertices, light, overlay, color);
        capNorth.render(matrices, vertices, light, overlay, color);
        capSouth.render(matrices, vertices, light, overlay, color);
        flag1.render(matrices, vertices, light, overlay, color);
        flag2.render(matrices, vertices, light, overlay, color);
        flag3.render(matrices, vertices, light, overlay, color);
        flag4.render(matrices, vertices, light, overlay, color);
    }
}
