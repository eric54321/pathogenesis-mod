package com.pathogenesis.entity.model;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class RogueCellModel extends EntityModel<RogueCellEntity> {

    private final ModelPart core;
    private final ModelPart segN, segNE, segE, segSE, segS, segSW, segW, segNW;
    private final ModelPart innerNE, innerNW, innerSE, innerSW;
    // Each tentacle has 3 tapered segments: base (thick), mid, tip (thin)
    private final ModelPart tentNb, tentNm, tentNt;
    private final ModelPart tentSb, tentSm, tentSt;
    private final ModelPart tentEb, tentEm, tentEt;
    private final ModelPart tentWb, tentWm, tentWt;

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
        this.tentNb = root.getChild("tent_n_b"); this.tentNm = root.getChild("tent_n_m"); this.tentNt = root.getChild("tent_n_t");
        this.tentSb = root.getChild("tent_s_b"); this.tentSm = root.getChild("tent_s_m"); this.tentSt = root.getChild("tent_s_t");
        this.tentEb = root.getChild("tent_e_b"); this.tentEm = root.getChild("tent_e_m"); this.tentEt = root.getChild("tent_e_t");
        this.tentWb = root.getChild("tent_w_b"); this.tentWm = root.getChild("tent_w_m"); this.tentWt = root.getChild("tent_w_t");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        // Disc parts
        r.addChild("core",     ModelPartBuilder.create().uv(0,0) .cuboid(-3f,-3f,-1f,6,6,2),  ModelTransform.pivot(0f,20f,0f));
        r.addChild("seg_n",    ModelPartBuilder.create().uv(20,0).cuboid(-1f,-2f,-1f,2,2,2),  ModelTransform.pivot(0f,17f,0f));
        r.addChild("seg_s",    ModelPartBuilder.create().uv(20,0).cuboid(-1f, 0f,-1f,2,2,2),  ModelTransform.pivot(0f,23f,0f));
        r.addChild("seg_e",    ModelPartBuilder.create().uv(20,0).cuboid( 0f,-1f,-1f,2,2,2),  ModelTransform.pivot(3f,20f,0f));
        r.addChild("seg_w",    ModelPartBuilder.create().uv(20,0).cuboid(-2f,-1f,-1f,2,2,2),  ModelTransform.pivot(-3f,20f,0f));
        r.addChild("seg_ne",   ModelPartBuilder.create().uv(28,0).cuboid( 0f,-2f,-1f,2,2,2),  ModelTransform.pivot( 2f,18f,0f));
        r.addChild("seg_nw",   ModelPartBuilder.create().uv(28,0).cuboid(-2f,-2f,-1f,2,2,2),  ModelTransform.pivot(-2f,18f,0f));
        r.addChild("seg_se",   ModelPartBuilder.create().uv(28,0).cuboid( 0f, 0f,-1f,2,2,2),  ModelTransform.pivot( 2f,22f,0f));
        r.addChild("seg_sw",   ModelPartBuilder.create().uv(28,0).cuboid(-2f, 0f,-1f,2,2,2),  ModelTransform.pivot(-2f,22f,0f));
        r.addChild("inner_ne", ModelPartBuilder.create().uv(36,0).cuboid( 0f,-3f,-1f,3,3,2),  ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_nw", ModelPartBuilder.create().uv(36,0).cuboid(-3f,-3f,-1f,3,3,2),  ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_se", ModelPartBuilder.create().uv(36,0).cuboid( 0f, 0f,-1f,3,3,2),  ModelTransform.pivot(0f,20f,0f));
        r.addChild("inner_sw", ModelPartBuilder.create().uv(36,0).cuboid(-3f, 0f,-1f,3,3,2),  ModelTransform.pivot(0f,20f,0f));

        // Tentacles: 3 tapered segments per direction, all pivoting at disc center (0,20,0).
        // Box extends in -Z direction; yaw in setAngles points each one outward.
        // Segment sizes: base=4x4x10, mid=3x3x10, tip=2x2x10 → 30 units total (~4.7 blocks at 2.5x scale)
        // All use uv(0,0) — solid color texture, UV overlap is fine.
        for (String dir : new String[]{"n","s","e","w"}) {
            r.addChild("tent_"+dir+"_b", ModelPartBuilder.create().uv(0,0).cuboid(-2f,-2f,-10f,4,4,10), ModelTransform.pivot(0f,20f,0f));
            r.addChild("tent_"+dir+"_m", ModelPartBuilder.create().uv(0,0).cuboid(-1.5f,-1.5f,-20f,3,3,10), ModelTransform.pivot(0f,20f,0f));
            r.addChild("tent_"+dir+"_t", ModelPartBuilder.create().uv(0,0).cuboid(-1f,-1f,-30f,2,2,10), ModelTransform.pivot(0f,20f,0f));
        }

        return TexturedModelData.of(md, 64, 32);
    }

    private void setTentacleAngles(ModelPart base, ModelPart mid, ModelPart tip,
                                    float yaw, float pitch) {
        base.yaw = yaw; base.pitch = pitch;
        mid.yaw  = yaw; mid.pitch  = pitch;
        tip.yaw  = yaw; tip.pitch  = pitch;
    }

    @Override
    public void setAngles(RogueCellEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 0.8f;

        for (ModelPart p : new ModelPart[]{core, segN, segS, segE, segW,
                segNE, segNW, segSE, segSW, innerNE, innerNW, innerSE, innerSW}) {
            p.yaw = spin;
        }

        // Continuous slow wave so tentacles are always moving
        float wave = (float)Math.sin(animationProgress * 0.08f) * 0.35f;

        // Lash animation on 60-tick cycle — snap out hard, slowly retract
        float cycle = (animationProgress % 60f) / 60f;
        float lashFactor = (cycle < 0.10f) ? cycle / 0.10f
                         : (cycle < 0.30f) ? 1f - (cycle - 0.10f) / 0.20f
                         : 0f;
        // base droop + wave + dramatic lash snap
        float tentPitch = 0.5f + wave - lashFactor * 2.0f;

        setTentacleAngles(tentNb, tentNm, tentNt, spin,                              tentPitch);
        setTentacleAngles(tentSb, tentSm, tentSt, spin + (float)Math.PI,             tentPitch);
        setTentacleAngles(tentEb, tentEm, tentEt, spin - (float)(Math.PI / 2),       tentPitch);
        setTentacleAngles(tentWb, tentWm, tentWt, spin + (float)(Math.PI / 2),       tentPitch);
    }

    private void renderTentacle(ModelPart base, ModelPart mid, ModelPart tip,
                                 MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        base.render(matrices, vertices, light, overlay);
        mid.render(matrices, vertices, light, overlay);
        tip.render(matrices, vertices, light, overlay);
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
        tentNb.render(matrices, vertices, light, overlay, color);
        tentNm.render(matrices, vertices, light, overlay, color);
        tentNt.render(matrices, vertices, light, overlay, color);
        tentSb.render(matrices, vertices, light, overlay, color);
        tentSm.render(matrices, vertices, light, overlay, color);
        tentSt.render(matrices, vertices, light, overlay, color);
        tentEb.render(matrices, vertices, light, overlay, color);
        tentEm.render(matrices, vertices, light, overlay, color);
        tentEt.render(matrices, vertices, light, overlay, color);
        tentWb.render(matrices, vertices, light, overlay, color);
        tentWm.render(matrices, vertices, light, overlay, color);
        tentWt.render(matrices, vertices, light, overlay, color);
    }
}
