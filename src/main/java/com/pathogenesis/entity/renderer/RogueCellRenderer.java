package com.pathogenesis.entity.renderer;

import com.pathogenesis.entity.RogueCellEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

/**
 * Tells Minecraft how to draw the RogueCell on screen.
 * We reuse the Zombie model as a placeholder — it gives the mob a humanoid shape.
 * The red glow comes from the Glowing status effect applied in RogueCellEntity.tick().
 * Custom 3D models (via Blockbench) can replace this later.
 */
public class RogueCellRenderer extends MobEntityRenderer<RogueCellEntity, ZombieEntityModel<RogueCellEntity>> {

    // Placeholder texture — uses the zombie skin until a custom texture is made
    private static final Identifier TEXTURE =
        Identifier.of("minecraft", "textures/entity/zombie/zombie.png");

    public RogueCellRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(RogueCellEntity entity) {
        return TEXTURE;
    }
}
