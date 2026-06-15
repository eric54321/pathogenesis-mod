package com.pathogenesis.entity.model;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Thin circular disc with 4 tentacles that orbit the disc and lash out every 3 seconds.
 * Tentacles are children of core so they spin with the disc automatically.
 * Texture 64x32.
 */
public class RogueCellModel extends EntityModel<RogueCellEntity> {

    private final ModelPart core;
    private final ModelPart segN;
    private final ModelPart segNE;
    private final ModelPart segE;
    private final ModelPart segSE;
    private final ModelPart segS;
    private final ModelPart segSW;
    private final ModelPart segW;
    private final ModelPart segNW;
    private final ModelPart innerNE;
    private final ModelPart innerNW;
    private final ModelPart innerSE;
    private final ModelPart innerSW;
    // 4 tentacles — children of core so they orbit with the spin
    private final ModelPart tentN;
    private final ModelPart tentS;
    private final ModelPart tentE;
    private final ModelPart tentW;

    public RogueCellModel(ModelPart root) {
        this.core    = root.getChild("core");
        this.segN    = root.getChild("seg_n");
        this.segNE   = root.getChild("seg_ne");
        this.segE    = root.getChild("seg_e");
        this.segSE   = root.getChild("seg_se");
        this.segS    = root.getChild("seg_s");
        this.segSW   = root.getChild("seg_sw");
        this.segW    = root.getChild("seg_w");
        this.segNW   = root.getChild("seg_nw");
        this.innerNE = root.getChild("inner_ne");
        this.innerNW = root.getChild("inner_nw");
        this.innerSE = root.getChild("inner_se");
        this.innerSW = root.getChild("inner_sw");
        // tentacles are children of core
        this.tentN   = core.getChild("tent_n");
        this.tentS   = core.getChild("tent_s");
        this.tentE   = core.getChild("tent_e");
        this.tentW   = core.getChild("tent_w");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        // Core: 6x6x2 center — tentacles are added as children
        ModelPartData coreData = r.addChild("core",
            ModelPartBuilder.create().uv(0, 0)
                .cuboid(-3f, -3f, -1f, 6, 6, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        // Tentacles: 2×2×8, pivoted at disc edge (~4 units out), initial yaw rotated to face outward
        // All tentacles droop down (positive pitch) normally, lash outward (negative pitch) when attacking
        // tent_n — points -Z (north), no extra yaw needed since box extends in -Z
        coreData.addChild("tent_n",
            ModelPartBuilder.create().uv(0, 16)
                .cuboid(-1f, -1f, -8f, 2, 2, 8),
            ModelTransform.of(0f, 0f, -4f, 0.4f, 0f, 0f));

        // tent_s — points +Z (south), yaw 180° so its -Z box faces south
        coreData.addChild("tent_s",
            ModelPartBuilder.create().uv(0, 16)
                .cuboid(-1f, -1f, -8f, 2, 2, 8),
            ModelTransform.of(0f, 0f, 4f, 0.4f, (float)Math.PI, 0f));

        // tent_e — points +X (east), yaw -90° so its -Z box faces east
        coreData.addChild("tent_e",
            ModelPartBuilder.create().uv(0, 16)
                .cuboid(-1f, -1f, -8f, 2, 2, 8),
            ModelTransform.of(4f, 0f, 0f, 0.4f, -(float)(Math.PI / 2), 0f));

        // tent_w — points -X (west), yaw +90° so its -Z box faces west
        coreData.addChild("tent_w",
            ModelPartBuilder.create().uv(0, 16)
                .cuboid(-1f, -1f, -8f, 2, 2, 8),
            ModelTransform.of(-4f, 0f, 0f, 0.4f, (float)(Math.PI / 2), 0f));

        // Cardinal edge segments (2 wide, 2 tall, 2 deep)
        r.addChild("seg_n",
            ModelPartBuilder.create().uv(20, 0)
                .cuboid(-1f, -2f, -1f, 2, 2, 2),
            ModelTransform.pivot(0f, 17f, 0f));

        r.addChild("seg_s",
            ModelPartBuilder.create().uv(20, 0)
                .cuboid(-1f, 0f, -1f, 2, 2, 2),
            ModelTransform.pivot(0f, 23f, 0f));

        r.addChild("seg_e",
            ModelPartBuilder.create().uv(20, 0)
                .cuboid(0f, -1f, -1f, 2, 2, 2),
            ModelTransform.pivot(3f, 20f, 0f));

        r.addChild("seg_w",
            ModelPartBuilder.create().uv(20, 0)
                .cuboid(-2f, -1f, -1f, 2, 2, 2),
            ModelTransform.pivot(-3f, 20f, 0f));

        // Diagonal edge segments (2x2x2) at 45° corners
        r.addChild("seg_ne",
            ModelPartBuilder.create().uv(28, 0)
                .cuboid(0f, -2f, -1f, 2, 2, 2),
            ModelTransform.pivot(2f, 18f, 0f));

        r.addChild("seg_nw",
            ModelPartBuilder.create().uv(28, 0)
                .cuboid(-2f, -2f, -1f, 2, 2, 2),
            ModelTransform.pivot(-2f, 18f, 0f));

        r.addChild("seg_se",
            ModelPartBuilder.create().uv(28, 0)
                .cuboid(0f, 0f, -1f, 2, 2, 2),
            ModelTransform.pivot(2f, 22f, 0f));

        r.addChild("seg_sw",
            ModelPartBuilder.create().uv(28, 0)
                .cuboid(-2f, 0f, -1f, 2, 2, 2),
            ModelTransform.pivot(-2f, 22f, 0f));

        // Inner diagonal fills (3x3x2) to smooth gaps between core and corners
        r.addChild("inner_ne",
            ModelPartBuilder.create().uv(36, 0)
                .cuboid(0f, -3f, -1f, 3, 3, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        r.addChild("inner_nw",
            ModelPartBuilder.create().uv(36, 0)
                .cuboid(-3f, -3f, -1f, 3, 3, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        r.addChild("inner_se",
            ModelPartBuilder.create().uv(36, 0)
                .cuboid(0f, 0f, -1f, 3, 3, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        r.addChild("inner_sw",
            ModelPartBuilder.create().uv(36, 0)
                .cuboid(-3f, 0f, -1f, 3, 3, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(RogueCellEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Slow spin on Y axis — tentacles spin with core since they're children
        float spin = animationProgress * 0.03f;
        for (ModelPart p : new ModelPart[]{core, segN, segS, segE, segW,
                segNE, segNW, segSE, segSW, innerNE, innerNW, innerSE, innerSW}) {
            p.yaw = spin;
        }

        // Tentacle lash animation on a 60-tick cycle
        // phase 0.0-0.25 → snap outward (droop→extend), 0.25-0.45 → snap back, 0.45-1.0 → rest drooping
        float cycle = (animationProgress % 60f) / 60f;
        float lashFactor;
        if (cycle < 0.25f) {
            lashFactor = cycle / 0.25f;                       // 0 → 1 (extending)
        } else if (cycle < 0.45f) {
            lashFactor = 1f - (cycle - 0.25f) / 0.20f;       // 1 → 0 (retracting)
        } else {
            lashFactor = 0f;                                   // resting
        }

        // droop = +0.4 rad (down), lash = -0.5 rad (outward/upward snap)
        float tentPitch = 0.4f + lashFactor * (-0.9f);
        tentN.pitch = tentPitch;
        tentS.pitch = tentPitch;
        tentE.pitch = tentPitch;
        tentW.pitch = tentPitch;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        core.render(matrices, vertices, light, overlay, color);
        segN.render(matrices, vertices, light, overlay, color);
        segS.render(matrices, vertices, light, overlay, color);
        segE.render(matrices, vertices, light, overlay, color);
        segW.render(matrices, vertices, light, overlay, color);
        segNE.render(matrices, vertices, light, overlay, color);
        segNW.render(matrices, vertices, light, overlay, color);
        segSE.render(matrices, vertices, light, overlay, color);
        segSW.render(matrices, vertices, light, overlay, color);
        innerNE.render(matrices, vertices, light, overlay, color);
        innerNW.render(matrices, vertices, light, overlay, color);
        innerSE.render(matrices, vertices, light, overlay, color);
        innerSW.render(matrices, vertices, light, overlay, color);
        // tentacles rendered via core (children) — no explicit call needed
    }
}
