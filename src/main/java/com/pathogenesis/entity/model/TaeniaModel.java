package com.pathogenesis.entity.model;

import com.pathogenesis.entity.TaeniaEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Segmented ribbon tapeworm for Taenia.
 * Wide flat scolex (head with hooks) leading into proglottid segments — grows
 * wider at the gravid (rear) end as the tapeworm matures. Looks like a grotesque
 * flat ribbon standing upright. GENERIC_SCALE grows the whole model over time.
 * Texture 64x32.
 */
public class TaeniaModel<T extends TaeniaEntity> extends EntityModel<T> {

    private final ModelPart scolex;
    private final ModelPart neck;
    private final ModelPart proglottid1;
    private final ModelPart proglottid2;
    private final ModelPart gravid;

    public TaeniaModel(ModelPart root) {
        this.scolex      = root.getChild("scolex");
        this.neck        = root.getChild("neck");
        this.proglottid1 = root.getChild("proglottid1");
        this.proglottid2 = root.getChild("proglottid2");
        this.gravid      = root.getChild("gravid");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();

        // Scolex — hooked head, widest part at top
        root.addChild("scolex",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4f, 0f, -3f, 8, 5, 6),
            ModelTransform.pivot(0f, 4f, 0f));

        // Neck — narrowed connection
        root.addChild("neck",
            ModelPartBuilder.create().uv(0, 11).cuboid(-3f, 0f, -2f, 6, 3, 5),
            ModelTransform.pivot(0f, 9f, 0f));

        // Proglottid 1 — mature segment
        root.addChild("proglottid1",
            ModelPartBuilder.create().uv(0, 19).cuboid(-4f, 0f, -2f, 8, 4, 5),
            ModelTransform.pivot(0f, 12f, 0f));

        // Proglottid 2 — another mature segment
        root.addChild("proglottid2",
            ModelPartBuilder.create().uv(0, 19).cuboid(-4f, 0f, -2f, 8, 4, 5),
            ModelTransform.pivot(0f, 16f, 0f));

        // Gravid — widest, egg-filled rear segment
        root.addChild("gravid",
            ModelPartBuilder.create().uv(26, 19).cuboid(-5f, 0f, -3f, 10, 4, 6),
            ModelTransform.pivot(0f, 20f, 0f));

        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Tapeworms ripple — alternating pitch on each segment
        float wave = (float) Math.sin(animationProgress * 0.04f) * 0.12f;
        scolex.pitch      =  wave;
        neck.pitch        = -wave;
        proglottid1.pitch =  wave;
        proglottid2.pitch = -wave;
        gravid.pitch      =  wave;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices,
                       int light, int overlay, int color) {
        scolex.render(matrices, vertices, light, overlay, color);
        neck.render(matrices, vertices, light, overlay, color);
        proglottid1.render(matrices, vertices, light, overlay, color);
        proglottid2.render(matrices, vertices, light, overlay, color);
        gravid.render(matrices, vertices, light, overlay, color);
    }
}
