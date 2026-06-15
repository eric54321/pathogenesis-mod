package com.pathogenesis.entity.model;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Thin circular disc with 4 tentacles orbiting it.
 * Tentacles are root-level parts; their pivots are updated each frame to orbit the disc center.
 */
public class RogueCellModel extends EntityModel<RogueCellEntity> {

    private final ModelPart core;
    private final ModelPart segN, segNE, segE, segSE, segS, segSW, segW, segNW;
    private final ModelPart innerNE, innerNW, innerSE, innerSW;
    private final ModelPart tentN, tentS, tentE, tentW;

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
        this.tentN   = root.getChild("tent_n");
        this.tentS   = root.getChild("tent_s");
        this.tentE   = root.getChild("tent_e");
        this.tentW   = root.getChild("tent_w");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        // Core: 6x6x2 center disc
        r.addChild("core",
            ModelPartBuilder.create().uv(0, 0).cuboid(-3f, -3f, -1f, 6, 6, 2),
            ModelTransform.pivot(0f, 20f, 0f));

        // Cardinal edge segments
        r.addChild("seg_n",  ModelPartBuilder.create().uv(20,0).cuboid(-1f,-2f,-1f,2,2,2), ModelTransform.pivot(0f,17f,0f));
        r.addChild("seg_s",  ModelPartBuilder.create().uv(20,0).cuboid(-1f, 0f,-1f,2,2,2), ModelTransform.pivot(0f,23f,0f));
        r.addChild("seg_e",  ModelPartBuilder.create().uv(20,0).cuboid( 0f,-1f,-1f,2,2,2), ModelTransform.pivot(3f,20f,0f));
        r.addChild("seg_w",  ModelPartBuilder.create().uv(20,0).cuboid(-2f,-1f,-1f,2,2,2), ModelTransform.pivot(-3f,20f,0f));

        // Diagonal edge segments
        r.addChild("seg_ne", ModelPartBuilder.create().uv(28,0).cuboid( 0f,-2f,-1f,2,2,2), ModelTransform.pivot( 2f,18f,0f));
        r.addChild("seg_nw", ModelPartBuilder.create().uv(28,0).cuboid(-2f,-2f,-1f,2,2,2), ModelTransform.pivot(-2f,18f,0f));
        r.addChild("seg_se", ModelPartBuilder.create().uv(28,0).cuboid( 0f, 0f,-1f,2,2,2), ModelTransform.pivot( 2f,22f,0f));
        r.addChild("seg_sw", ModelPartBuilder.create().uv(28,0).cuboid(-2f, 0f,-1f,2,2,2), ModelTransform.pivot(-2f,22f,0f));

        // Inner diagonal fills
        r.addChild("inner_ne", ModelPartBuilder.create().uv(36,0).cuboid( 0f,-3f,-1f,3,3,2), ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_nw", ModelPartBuilder.create().uv(36,0).cuboid(-3f,-3f,-1f,3,3,2), ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_se", ModelPartBuilder.create().uv(36,0).cuboid( 0f, 0f,-1f,3,3,2), ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_sw", ModelPartBuilder.create().uv(36,0).cuboid(-3f, 0f,-1f,3,3,2), ModelTransform.pivot(0f,20f,0f));

        // Tentacles: 2x2x10 box extending in the -Z direction from the pivot.
        // Pivots start at the disc edge; setAngles orbits them by updating pivotX/Z each frame.
        // uv(0,16): 2x2x10 box → 24 wide, 12 tall in UV space — fits in lower half of 64x32 texture.
        r.addChild("tent_n", ModelPartBuilder.create().uv(0,16).cuboid(-1f,-1f,-10f,2,2,10), ModelTransform.pivot(0f,20f,-5f));
        r.addChild("tent_s", ModelPartBuilder.create().uv(0,16).cuboid(-1f,-1f,-10f,2,2,10), ModelTransform.pivot(0f,20f, 5f));
        r.addChild("tent_e", ModelPartBuilder.create().uv(0,16).cuboid(-1f,-1f,-10f,2,2,10), ModelTransform.pivot( 5f,20f,0f));
        r.addChild("tent_w", ModelPartBuilder.create().uv(0,16).cuboid(-1f,-1f,-10f,2,2,10), ModelTransform.pivot(-5f,20f,0f));

        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(RogueCellEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 0.03f;

        // Disc parts all spin on Y
        for (ModelPart p : new ModelPart[]{core, segN, segS, segE, segW,
                segNE, segNW, segSE, segSW, innerNE, innerNW, innerSE, innerSW}) {
            p.yaw = spin;
        }

        // Orbit each tentacle around disc center by updating its pivot each frame
        float r = 5f; // orbit radius in model units
        tentN.pivotX = -r * (float)Math.sin(spin);
        tentN.pivotZ = -r * (float)Math.cos(spin);
        tentN.yaw    = spin;

        tentS.pivotX =  r * (float)Math.sin(spin);
        tentS.pivotZ =  r * (float)Math.cos(spin);
        tentS.yaw    = spin + (float)Math.PI;

        tentE.pivotX =  r * (float)Math.cos(spin);
        tentE.pivotZ = -r * (float)Math.sin(spin);
        tentE.yaw    = spin - (float)(Math.PI / 2);

        tentW.pivotX = -r * (float)Math.cos(spin);
        tentW.pivotZ =  r * (float)Math.sin(spin);
        tentW.yaw    = spin + (float)(Math.PI / 2);

        // Lash animation: 60-tick cycle — snap outward then retract
        float cycle = (animationProgress % 60f) / 60f;
        float lashFactor;
        if (cycle < 0.25f) {
            lashFactor = cycle / 0.25f;
        } else if (cycle < 0.45f) {
            lashFactor = 1f - (cycle - 0.25f) / 0.20f;
        } else {
            lashFactor = 0f;
        }
        // droop at rest (+0.4), snap flat on lash (-0.15)
        float tentPitch = 0.4f - lashFactor * 0.55f;
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
        tentN.render(matrices, vertices, light, overlay, color);
        tentS.render(matrices, vertices, light, overlay, color);
        tentE.render(matrices, vertices, light, overlay, color);
        tentW.render(matrices, vertices, light, overlay, color);
    }
}
