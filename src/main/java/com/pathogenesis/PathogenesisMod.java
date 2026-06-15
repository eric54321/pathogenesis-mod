package com.pathogenesis;

import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModItems;
import com.pathogenesis.system.HostHealth;
import com.pathogenesis.system.WaveSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

        // Intro cutscene on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // Darkness for a dramatic blackout effect
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0, false, false));

            // Title fade: 30 ticks fade in, 80 ticks hold, 30 ticks fade out
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(30, 80, 30));

            // Main title
            player.networkHandler.sendPacket(new TitleS2CPacket(
                Text.literal("PATHOGENESIS").formatted(Formatting.DARK_RED, Formatting.BOLD)));

            // Subtitle
            player.networkHandler.sendPacket(new SubtitleS2CPacket(
                Text.literal("A war rages within...").formatted(Formatting.RED)));

            // Ominous sound
            player.getWorld().playSound(null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_SPAWN,
                SoundCategory.MASTER, 1.0f, 0.8f);
        });

        LOGGER.info("Pathogenesis mod ready. Pathogens incoming!");
    }
}
