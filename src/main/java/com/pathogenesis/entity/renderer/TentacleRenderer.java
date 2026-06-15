package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.TentacleProjectile;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class TentacleRenderer extends EntityRenderer<TentacleProjectile> {

    private static final Identifier TEXTURE =
        Identifier.of("pathogenesis", "textures/entity/tentacle.png");

    public TentacleRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(TentacleProjectile entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Point in the direction of travel
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(
            MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90f));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(
            MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));

        // Draw a thin elongated box (the tentacle)
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        Matrix4f m = matrices.peek().getPositionMatrix();
        float w = 0.05f;   // thin
        float len = 0.6f;  // length
        // simple quad — front face
        vc.vertex(m, -len, -w, w).color(180, 20, 180, 255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1);
        vc.vertex(m, -len,  w, w).color(180, 20, 180, 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1);
        vc.vertex(m,  len,  w, w).color(220, 60, 220, 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1);
        vc.vertex(m,  len, -w, w).color(220, 60, 220, 255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1);
        // back face
        vc.vertex(m,  len, -w, -w).color(220, 60, 220, 255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1);
        vc.vertex(m,  len,  w, -w).color(220, 60, 220, 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1);
        vc.vertex(m, -len,  w, -w).color(180, 20, 180, 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1);
        vc.vertex(m, -len, -w, -w).color(180, 20, 180, 255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(TentacleProjectile entity) {
        return TEXTURE;
    }
}
