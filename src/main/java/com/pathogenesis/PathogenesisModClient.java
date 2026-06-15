package com.pathogenesis;

import com.pathogenesis.entity.model.RogueCellModel;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.entity.renderer.RogueCellRenderer;
import com.pathogenesis.entity.renderer.VironRenderer;
import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PathogenesisModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // RogueCell
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ROGUE_CELL, RogueCellModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.ROGUE_CELL, RogueCellRenderer::new);

        // Viron
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.VIRON, VironModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.VIRON, VironRenderer::new);

        PathogenesisMod.LOGGER.info("Pathogenesis client initialized.");
    }
}
