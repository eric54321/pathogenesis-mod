package com.pathogenesis;

import com.pathogenesis.entity.model.PhageModel;
import com.pathogenesis.entity.model.RogueCellModel;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.entity.renderer.CoronavirusRenderer;
import com.pathogenesis.entity.renderer.InfluenzaRenderer;
import com.pathogenesis.entity.renderer.PhageRenderer;
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

        // Influenza (reuses Viron model until custom Blockbench model is made)
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.INFLUENZA, VironModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.INFLUENZA, InfluenzaRenderer::new);

        // Coronavirus (reuses Viron model until crown-shaped Blockbench model is made)
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.CORONAVIRUS, VironModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.CORONAVIRUS, CoronavirusRenderer::new);

        // Phage — alien lander shape with icosahedral head, tail shaft, and 4 landing legs
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.PHAGE, PhageModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.PHAGE, PhageRenderer::new);

        PathogenesisMod.LOGGER.info("Pathogenesis client initialized.");
    }
}
