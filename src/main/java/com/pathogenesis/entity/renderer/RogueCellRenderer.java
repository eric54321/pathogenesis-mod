package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * Tells Minecraft how to draw the RogueCell on screen.
 * BipedEntityModel works with any LivingEntity subclass (unlike ZombieEntityModel
 * which is restricted to ZombieEntity). Uses the zombie layer for the humanoid shape.
 * Custom 3D models (via Blockbench) can replace this later.
 */
public class RogueCellRenderer extends MobEntityRenderer<RogueCellEntity, BipedEntityModel<RogueCellEntity>> {

    // Placeholder texture — uses the zombie skin until a custom texture is made
    private static final Identifier TEXTURE =
        Identifier.of("minecraft", "textures/entity/zombie/zombie.png");

    public RogueCellRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(RogueCellEntity entity) {
        return TEXTURE;
    }
}
