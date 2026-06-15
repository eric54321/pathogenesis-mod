package com.pathogenesis;

import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModItems;
import com.pathogenesis.system.HostHealth;
import com.pathogenesis.system.WaveSpawner;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Pathogenesis mod.
 * Fabric calls onInitialize() once when the game starts up (server or client).
 * All registration — entities, items, events — happens here.
 */
public class PathogenesisMod implements ModInitializer {

    // Mod ID must match the "id" field in fabric.mod.json exactly
    public static final String MOD_ID = "pathogenesis";

    // Logger so we can print messages to the game console for debugging
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Pathogenesis mod initializing...");

        // Register all custom entities (mobs) with Minecraft's registry
        ModEntities.register();

        // Register all custom items with Minecraft's registry
        ModItems.register();

        // Register the wave spawner tick event so waves fire every 2 minutes
        WaveSpawner.register();

        // Register host health system (boss bar + join message + game over)
        HostHealth.register();

        LOGGER.info("Pathogenesis mod ready. Pathogens incoming!");
    }
}
