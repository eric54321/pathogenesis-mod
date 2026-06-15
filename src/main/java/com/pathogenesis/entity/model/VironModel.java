package com.pathogenesis.entity.model;

import com.pathogenesis.entity.VironEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 3D model for the Viron — a small viral particle with a central body and spikes.
 *
 * Biology note: Real virions have a protein shell (capsid) studded with spike
 * proteins that latch onto host cells. The spikes here represent those proteins.
 * The slow rotation makes it look like it is drifting through the bloodstream.
 *
 * Texture map is 32x32 pixels:
 *   body   → UV (0,0)  6x6x6 cube
 *   spikes → UV (0,12) 2x2x2 cubes
 */
public class VironModel extends EntityModel<VironEntity> {

    private final ModelPart body;
    private final ModelPart spikeTop;
    private final ModelPart spikeBottom;
    private final ModelPart spikeNorth;
    private final ModelPart spikeSouth;
    private final ModelPart spikeEast;
    private final ModelPart spikeWest;

    public VironModel(ModelPart root) {
        this.body       = root.getChild("body");
        this.spikeTop    = root.getChild("spike_top");
        this.spikeBottom = root.getChild("spike_bottom");
        this.spikeNorth  = root.getChild("spike_north");
        this.spikeSouth  = root.getChild("spike_south");
        this.spikeEast   = root.getChild("spike_east");
        this.spikeWest   = root.getChild("spike_west");
    }

    /**
     * Defines the shape of the model.
     * Called once at startup to build the texture/geometry data.
     */
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Central capsid body — a small cube sitting at head height
        root.addChild("body",
            ModelPartBuilder.create().uv(0, 0).cuboid(-3f, -3f, -3f, 6, 6, 6),
            ModelTransform.pivot(0f, 20f, 0f));

        // Six spike proteins pointing out from each face of the body
        root.addChild("spike_top",
            ModelPartBuilder.create().uv(0, 12).cuboid(-1f, -2f, -1f, 2, 2, 2),
            ModelTransform.pivot(0f, 14f, 0f));

        root.addChild("spike_bottom",
            ModelPartBuilder.create().uv(8, 12).cuboid(-1f, 0f, -1f, 2, 2, 2),
            ModelTransform.pivot(0f, 23f, 0f));

        root.addChild("spike_north",
            ModelPartBuilder.create().uv(0, 16).cuboid(-1f, -1f, -2f, 2, 2, 2),
            ModelTransform.pivot(0f, 20f, -3f));

        root.addChild("spike_south",
            ModelPartBuilder.create().uv(8, 16).cuboid(-1f, -1f, 0f, 2, 2, 2),
            ModelTransform.pivot(0f, 20f, 3f));

        root.addChild("spike_east",
            ModelPartBuilder.create().uv(0, 20).cuboid(0f, -1f, -1f, 2, 2, 2),
            ModelTransform.pivot(3f, 20f, 0f));

        root.addChild("spike_west",
            ModelPartBuilder.create().uv(8, 20).cuboid(-2f, -1f, -1f, 2, 2, 2),
            ModelTransform.pivot(-3f, 20f, 0f));

        return TexturedModelData.of(modelData, 32, 32);
    }

    /**
     * Animates the model each frame.
     * Slow rotation on all axes makes the Viron look like it is tumbling
     * through the bloodstream — matching the behaviour of real viral particles.
     */
    @Override
    public void setAngles(VironEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 0.04f;
        this.body.yaw   = spin;
        this.body.pitch = spin * 0.7f;
        this.body.roll  = spin * 0.5f;

        // Spikes rotate with the body
        this.spikeTop.yaw    = spin;
        this.spikeBottom.yaw = spin;
        this.spikeNorth.yaw  = spin;
        this.spikeSouth.yaw  = spin;
        this.spikeEast.yaw   = spin;
        this.spikeWest.yaw   = spin;
    }

    /** Renders every part of the model. */
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
