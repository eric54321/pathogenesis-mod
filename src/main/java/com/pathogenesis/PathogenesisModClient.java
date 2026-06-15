package com.pathogenesis;

import com.pathogenesis.entity.model.AscariModel;
import com.pathogenesis.entity.model.DermatophyteModel;
import com.pathogenesis.entity.model.StrongyloideModel;
import com.pathogenesis.entity.model.TaeniaModel;
import com.pathogenesis.entity.model.PhageModel;
import com.pathogenesis.entity.model.RogueCellModel;
import com.pathogenesis.entity.model.StaphModel;
import com.pathogenesis.entity.model.StreptococcusModel;
import com.pathogenesis.entity.model.VironModel;
import com.pathogenesis.entity.renderer.AscariRenderer;
import com.pathogenesis.entity.renderer.CoronavirusRenderer;
import com.pathogenesis.entity.renderer.DermatophyteRenderer;
import com.pathogenesis.entity.renderer.StrongyloideRenderer;
import com.pathogenesis.entity.renderer.TaeniaRenderer;
import com.pathogenesis.entity.renderer.TentacleRenderer;
import com.pathogenesis.entity.renderer.InfluenzaRenderer;
import com.pathogenesis.entity.renderer.PhageRenderer;
import com.pathogenesis.entity.renderer.RogueCellRenderer;
import com.pathogenesis.entity.renderer.StaphRenderer;
import com.pathogenesis.entity.renderer.StreptococcusRenderer;
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

        // Tentacle projectile
        EntityRendererRegistry.register(ModEntities.TENTACLE, TentacleRenderer::new);

        // Stage 1 — Skin pathogens
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.STAPH, StaphModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.STAPH, StaphRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.STREPTOCOCCUS, StreptococcusModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.STREPTOCOCCUS, StreptococcusRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.DERMATOPHYTE, DermatophyteModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.DERMATOPHYTE, DermatophyteRenderer::new);

        // GI Tract — food-borne parasites
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ASCARI, AscariModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.ASCARI, AscariRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.TAENIA, TaeniaModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TAENIA, TaeniaRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.STRONGYLOIDE, StrongyloideModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.STRONGYLOIDE, StrongyloideRenderer::new);

        PathogenesisMod.LOGGER.info("Pathogenesis client initialized.");
    }
}
