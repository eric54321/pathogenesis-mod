package com.pathogenesis.system;

import com.pathogenesis.PathogenesisMod;
import com.pathogenesis.entity.CoronavirusEntity;
import com.pathogenesis.entity.InfluenzaEntity;
import com.pathogenesis.entity.PhageEntity;
import com.pathogenesis.entity.RogueCellEntity;
import com.pathogenesis.entity.VironEntity;
import com.pathogenesis.init.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Heightmap;

import java.util.List;

/**
 * WaveSpawner — the core game loop for Pathogenesis.
 *
 * Every 2 minutes (2400 server ticks) a new wave of pathogens is spawned.
 * Enemy count scales with online player count so the game is equally hard
 * whether 2 players or 10 players are online.
 *
 * Scaling formula: BASE_ENEMIES + (playerCount x ENEMIES_PER_PLAYER) + wave bonus
 *
 * Example — Wave 3 with 4 players:
 *   5 + (4 x 3) + (3-1)*2 = 5 + 12 + 4 = 21 enemies
 *
 * This class uses static state because there is exactly one wave timer per server.
 * All methods are static so they can be referenced easily from the event registration.
 */
public class WaveSpawner {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** 20 ticks per second x 60 seconds x 2 minutes = 2400 ticks between waves */
    private static final int WAVE_INTERVAL_TICKS = 20 * 60 * 2;

    /** Minimum enemies per wave regardless of player count */
    private static final int BASE_ENEMIES = 5;

    /** Extra enemies added per online player */
    private static final int ENEMIES_PER_PLAYER = 3;

    /** Extra enemies added per completed wave (difficulty ramp) */
    private static final int ENEMIES_PER_WAVE_BONUS = 2;

    /** How far (in blocks) enemies spawn from the target player — min radius */
    private static final double SPAWN_RADIUS_MIN = 10.0;

    /** How far (in blocks) enemies spawn from the target player — max radius */
    private static final double SPAWN_RADIUS_MAX = 20.0;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Current wave number — increments each time a wave fires */
    private static int waveNumber = 0;

    /** Ticks remaining until the next wave fires */
    private static int ticksUntilNextWave = WAVE_INTERVAL_TICKS;

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers the tick event listener with Fabric.
     * Called once from PathogenesisMod.onInitialize().
     * After this, Minecraft will call onServerTick() every single server tick.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WaveSpawner::onServerTick);
        PathogenesisMod.LOGGER.info("WaveSpawner registered. First wave in 2 minutes.");
    }

    // -------------------------------------------------------------------------
    // Tick handler
    // -------------------------------------------------------------------------

    /**
     * Called by Fabric at the end of every server tick (20 times per second).
     * Decrements the countdown timer and fires a wave when it reaches zero.
     *
     * @param server The running Minecraft server instance
     */
    private static void onServerTick(MinecraftServer server) {
        // Skip counting if nobody is online — pause the game between sessions
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }

        ticksUntilNextWave--;

        if (ticksUntilNextWave <= 0) {
            // Reset the timer BEFORE spawning so lag during spawning doesn't shorten the next interval
            ticksUntilNextWave = WAVE_INTERVAL_TICKS;
            triggerWave(server);
        }
    }

    // -------------------------------------------------------------------------
    // Wave logic
    // -------------------------------------------------------------------------

    /**
     * Fires a new wave: increments the wave counter, calculates enemy count,
     * broadcasts the warning message to all players, then spawns the enemies.
     *
     * @param server The running Minecraft server instance
     */
    private static void triggerWave(MinecraftServer server) {
        waveNumber++;

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        int playerCount = players.size();

        // Calculate total enemies for this wave using the scaling formula
        int enemyCount = BASE_ENEMIES
            + (playerCount * ENEMIES_PER_PLAYER)
            + ((waveNumber - 1) * ENEMIES_PER_WAVE_BONUS);

        PathogenesisMod.LOGGER.info(
            "Wave {} starting: {} players online, {} enemies spawning.",
            waveNumber, playerCount, enemyCount
        );

        // Broadcast wave start message to all players in chat
        broadcastWaveAlert(server, enemyCount);

        // Spawn the enemies distributed among the players
        spawnEnemies(server, players, enemyCount);
    }

    /**
     * Sends a formatted wave alert message to every online player.
     * The message appears in the chat so players have a moment to prepare.
     *
     * @param server      The running Minecraft server instance
     * @param enemyCount  Total number of enemies that will spawn this wave
     */
    private static void broadcastWaveAlert(MinecraftServer server, int enemyCount) {
        Text message = Text.literal(
            "⚠ WAVE " + waveNumber + " INCOMING! " + enemyCount + " pathogens detected! ⚠"
        ).formatted(Formatting.RED, Formatting.BOLD);

        // false = send as a chat message (not a system message), so it shows in chat history
        server.getPlayerManager().broadcast(message, false);
    }

    /**
     * Spawns the calculated number of enemies spread across the map.
     * Enemies are distributed round-robin among players so the spawning
     * stays near the action rather than clustering around one player.
     *
     * @param server       The running Minecraft server instance
     * @param players      List of currently online players
     * @param totalEnemies Total enemies to spawn this wave
     */
    private static void spawnEnemies(
        MinecraftServer server,
        List<ServerPlayerEntity> players,
        int totalEnemies
    ) {
        for (int i = 0; i < totalEnemies; i++) {
            // Cycle through players so enemies are spread evenly
            ServerPlayerEntity targetPlayer = players.get(i % players.size());
            ServerWorld world = targetPlayer.getServerWorld();

            // Pick a random direction and distance to spawn the enemy
            double angle = Math.random() * 2.0 * Math.PI;
            double distance = SPAWN_RADIUS_MIN + Math.random() * (SPAWN_RADIUS_MAX - SPAWN_RADIUS_MIN);

            double spawnX = targetPlayer.getX() + Math.cos(angle) * distance;
            double spawnZ = targetPlayer.getZ() + Math.sin(angle) * distance;

            // Use the surface Y so enemies always spawn on solid ground, not in the air or underground
            double spawnY = world.getTopY(
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (int) spawnX,
                (int) spawnZ
            );

            // Pick enemy type based on wave number:
            //   Wave 1-2:  RogueCell + Viron
            //   Wave 3-4:  add Influenza
            //   Wave 5-6:  add Coronavirus
            //   Wave 7+:   add Bacteriophage (precision, high damage)
            net.minecraft.entity.mob.MobEntity enemy;
            int type = pickEnemyType(i, totalEnemies);
            enemy = switch (type) {
                case 1 -> new VironEntity(ModEntities.VIRON, world);
                case 2 -> new InfluenzaEntity(ModEntities.INFLUENZA, world);
                case 3 -> new CoronavirusEntity(ModEntities.CORONAVIRUS, world);
                case 4 -> new PhageEntity(ModEntities.PHAGE, world);
                default -> new RogueCellEntity(ModEntities.ROGUE_CELL, world);
            };
            enemy.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0.0f, 0.0f);
            world.spawnEntity(enemy);
        }
    }

    // -------------------------------------------------------------------------
    // Enemy type selection
    // -------------------------------------------------------------------------

    /**
     * Returns an enemy type code based on wave progression and enemy index.
     *   0 = RogueCell, 1 = Viron, 2 = Influenza, 3 = Coronavirus
     *
     * Wave 1-2: 60% RogueCell, 40% Viron
     * Wave 3-4: 40% RogueCell, 30% Viron, 30% Influenza
     * Wave 5+:  25% each of all four types
     */
    private static int pickEnemyType(int enemyIndex, int totalEnemies) {
        // Use modulo of enemy index for deterministic spread within a wave
        int slot = enemyIndex % 20;
        if (waveNumber <= 2) {
            return slot < 12 ? 0 : 1;            // 60% RogueCell, 40% Viron
        } else if (waveNumber <= 4) {
            if (slot < 8) return 0;              // 40% RogueCell
            if (slot < 14) return 1;             // 30% Viron
            return 2;                            // 30% Influenza
        } else if (waveNumber <= 6) {
            if (slot < 5)  return 0;             // 25% RogueCell
            if (slot < 10) return 1;             // 25% Viron
            if (slot < 15) return 2;             // 25% Influenza
            return 3;                            // 25% Coronavirus
        } else {
            if (slot < 5)  return 0;             // 25% RogueCell
            if (slot < 10) return 1;             // 25% Viron
            if (slot < 14) return 3;             // 20% Coronavirus
            if (slot < 18) return 2;             // 20% Influenza
            return 4;                            // 10% Bacteriophage — rare, devastating
        }
    }

    // -------------------------------------------------------------------------
    // Public utilities
    // -------------------------------------------------------------------------

    /**
     * Returns the current wave number.
     * Useful for other systems (e.g., biome darkening) that need to know how far into the game we are.
     */
    public static int getWaveNumber() {
        return waveNumber;
    }

    /**
     * Resets the wave state back to the beginning.
     * Call this when players win (all enemies defeated) or when a new game starts.
     */
    public static void reset() {
        waveNumber = 0;
        ticksUntilNextWave = WAVE_INTERVAL_TICKS;
        PathogenesisMod.LOGGER.info("WaveSpawner reset. Game restarting.");
    }
}
