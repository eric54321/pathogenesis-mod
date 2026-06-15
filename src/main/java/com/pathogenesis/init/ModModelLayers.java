package com.pathogenesis.init;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModModelLayers {
    public static final EntityModelLayer ROGUE_CELL =
        new EntityModelLayer(Identifier.of("pathogenesis", "rogue_cell"), "main");

    public static final EntityModelLayer VIRON =
        new EntityModelLayer(Identifier.of("pathogenesis", "viron"), "main");

    public static final EntityModelLayer INFLUENZA =
        new EntityModelLayer(Identifier.of("pathogenesis", "influenza"), "main");

    public static final EntityModelLayer CORONAVIRUS =
        new EntityModelLayer(Identifier.of("pathogenesis", "coronavirus"), "main");
}
