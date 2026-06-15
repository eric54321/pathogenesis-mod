package com.pathogenesis.system;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Spawns ambient particles and broadcasts stage title cards to give each
 * infection stage a distinct atmosphere without requiring custom dimensions.
 *
 * Stages by wave number:
 *   Waves  1-3  — Skin (epidermis/dermis): fleshy pink
 *   Waves  4-6  — Dermis / early bloodstream: deeper red-pink
 *   Waves  7-9  — Bloodstream: blood red
 *   Wave  10+   — Deep tissue / organs: dark crimson
 */
public class StageAmbience {

    // Particle colors per stage (RGB 0-1 range for DustParticleEffect)
    private static final Vector3f COLOR_SKIN        = new Vector3f(0.95f, 0.75f, 0.70f); // fleshy pink
    private static final Vector3f COLOR_DERMIS      = new Vector3f(0.85f, 0.40f, 0.45f); // deeper rose
    private static final Vector3f COLOR_BLOOD       = new Vector3f(0.80f, 0.10f, 0.10f); // blood red
    private static final Vector3f COLOR_DEEP_TISSUE = new Vector3f(0.45f, 0.05f, 0.08f); // dark crimson

    // How many ticks between particle bursts (every 3 seconds)
    private static final int PARTICLE_INTERVAL = 60;

    private static int tickCounter = 0;
    private static int lastStage = -1;

    public static void tick(MinecraftServer server) {
        if (server.getPlayerManager().getPlayerList().isEmpty()) return;

        int stage = getStage();
        int wave = WaveSpawner.getWaveNumber();

        // Announce stage change with a title card
        if (stage != lastStage && wave > 0) {
            broadcastStageTitle(server, stage);
            lastStage = stage;
        }

        // Spawn ambient particles on interval
        tickCounter++;
        if (tickCounter >= PARTICLE_INTERVAL) {
            tickCounter = 0;
            spawnParticles(server, stage);
        }
    }

    private static int getStage() {
        int wave = WaveSpawner.getWaveNumber();
        if (wave <= 3)  return 0; // skin
        if (wave <= 6)  return 1; // dermis
        if (wave <= 9)  return 2; // bloodstream
        return 3;                 // deep tissue
    }

    private static void broadcastStageTitle(MinecraftServer server, int stage) {
        String[] titles = {
            "Epidermis",
            "Dermis",
            "Bloodstream",
            "Deep Tissue"
        };
        String[] subtitles = {
            "The infection reaches the outer skin layer.",
            "Pathogens breach deeper into the flesh.",
            "The infection enters the bloodstream.",
            "The body's vital organs are under attack."
        };
        Formatting[] colors = {
            Formatting.YELLOW,
            Formatting.GOLD,
            Formatting.RED,
            Formatting.DARK_RED
        };

        Text title    = Text.literal(titles[stage]).formatted(colors[stage], Formatting.BOLD);
        Text subtitle = Text.literal(subtitles[stage]).formatted(Formatting.GRAY);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.TitleS2CPacket(title)
            );
            player.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitle)
            );
            // Show title for 3s, fade 0.5s in/out
            player.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 60, 10)
            );
        }
    }

    private static void spawnParticles(MinecraftServer server, int stage) {
        Vector3f color = switch (stage) {
            case 0  -> COLOR_SKIN;
            case 1  -> COLOR_DERMIS;
            case 2  -> COLOR_BLOOD;
            default -> COLOR_DEEP_TISSUE;
        };

        DustParticleEffect particle = new DustParticleEffect(color, 1.2f);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            Vec3d pos = player.getPos();

            // Scatter 12 particles in a sphere around the player
            for (int i = 0; i < 12; i++) {
                double angle  = Math.random() * 2 * Math.PI;
                double pitch  = (Math.random() - 0.5) * Math.PI;
                double radius = 4.0 + Math.random() * 6.0;

                double px = pos.x + radius * Math.cos(pitch) * Math.cos(angle);
                double py = pos.y + 1.0 + radius * Math.sin(pitch);
                double pz = pos.z + radius * Math.cos(pitch) * Math.sin(angle);

                world.spawnParticles(particle, px, py, pz, 1, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    public static void reset() {
        lastStage = -1;
        tickCounter = 0;
    }
}
