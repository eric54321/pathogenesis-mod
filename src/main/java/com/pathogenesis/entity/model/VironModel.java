package com.pathogenesis.entity.model;

import com.pathogenesis.entity.VironEntity;
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
 * 3D model for the Viron — 8x8x8 body with 6 cardinal spike proteins.
 *
 * Texture layout (32x32):
 *   Left  16x16 (u 0-16):  body membrane — golden yellow with gradient
 *   Right 16x16 (u 16-32): spike protein  — dark red to orange
 *
 * The whole model rotates slowly to simulate drifting through the bloodstream.
 */
public class VironModel<T extends VironEntity> extends EntityModel<T> {

    private final ModelPart body;
    private final ModelPart spikeTop;
    private final ModelPart spikeBottom;
    private final ModelPart spikeNorth;
    private final ModelPart spikeSouth;
    private final ModelPart spikeEast;
    private final ModelPart spikeWest;

    public VironModel(ModelPart root) {
        this.body        = root.getChild("body");
        this.spikeTop    = root.getChild("spike_top");
        this.spikeBottom = root.getChild("spike_bottom");
        this.spikeNorth  = root.getChild("spike_north");
        this.spikeSouth  = root.getChild("spike_south");
        this.spikeEast   = root.getChild("spike_east");
        this.spikeWest   = root.getChild("spike_west");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Central body — 8x8x8 cube, UV maps to left half of texture (body colour)
        root.addChild("body",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -4f, -4f, 8, 8, 8),
            ModelTransform.pivot(0f, 20f, 0f));

        // Top spike — extends upward from body top face
        root.addChild("spike_top",
            ModelPartBuilder.create().uv(16, 0).cuboid(-1f, -5f, -1f, 2, 5, 2),
            ModelTransform.pivot(0f, 16f, 0f));

        // Bottom spike — extends downward from body bottom face
        root.addChild("spike_bottom",
            ModelPartBuilder.create().uv(16, 0).cuboid(-1f, 0f, -1f, 2, 5, 2),
            ModelTransform.pivot(0f, 24f, 0f));

        // North spike — extends north (negative Z)
        root.addChild("spike_north",
            ModelPartBuilder.create().uv(16, 0).cuboid(-1f, -2f, -5f, 2, 4, 5),
            ModelTransform.pivot(0f, 20f, -4f));

        // South spike — extends south (positive Z)
        root.addChild("spike_south",
            ModelPartBuilder.create().uv(16, 0).cuboid(-1f, -2f, 0f, 2, 4, 5),
            ModelTransform.pivot(0f, 20f, 4f));

        // East spike — extends east (positive X)
        root.addChild("spike_east",
            ModelPartBuilder.create().uv(16, 0).cuboid(0f, -2f, -1f, 5, 4, 2),
            ModelTransform.pivot(4f, 20f, 0f));

        // West spike — extends west (negative X)
        root.addChild("spike_west",
            ModelPartBuilder.create().uv(16, 0).cuboid(-5f, -2f, -1f, 5, 4, 2),
            ModelTransform.pivot(-4f, 20f, 0f));

        return TexturedModelData.of(modelData, 32, 32);
    }

    /**
     * Rotates the entire model slowly each frame — simulates a viral particle
     * tumbling freely through the bloodstream with no preferred orientation.
     */
    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 0.03f;
        body.yaw = spin; body.pitch = spin * 0.7f; body.roll = spin * 0.4f;
        // Spikes are children of root (not body) so rotate them separately
        for (ModelPart spike : new ModelPart[]{spikeTop, spikeBottom, spikeNorth, spikeSouth, spikeEast, spikeWest}) {
            spike.yaw = spin; spike.pitch = spin * 0.7f; spike.roll = spin * 0.4f;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        body.render(matrices, vertices, light, overlay, color);
        spikeTop.render(matrices, vertices, light, overlay, color);
        spikeBottom.render(matrices, vertices, light, overlay, color);
        spikeNorth.render(matrices, vertices, light, overlay, color);
        spikeSouth.render(matrices, vertices, light, overlay, color);
        spikeEast.render(matrices, vertices, light, overlay, color);
        spikeWest.render(matrices, vertices, light, overlay, color);
    }
}
