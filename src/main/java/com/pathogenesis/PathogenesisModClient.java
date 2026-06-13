package com.pathogenesis;

import com.pathogenesis.entity.renderer.RogueCellRenderer;
import com.pathogenesis.init.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side mod initializer.
 * Fabric calls this only on the game client (not on a dedicated server).
 * Rendering code must live here — the server has no renderer classes loaded.
 */
public class PathogenesisModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register the renderer so Minecraft knows how to draw RogueCell
        EntityRendererRegistry.register(ModEntities.ROGUE_CELL, RogueCellRenderer::new);

        PathogenesisMod.LOGGER.info("Pathogenesis client initialized.");
    }
}
