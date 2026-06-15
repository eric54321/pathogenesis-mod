package com.pathogenesis.system;

import com.pathogenesis.PathogenesisMod;
import com.pathogenesis.entity.CoronavirusEntity;
import com.pathogenesis.entity.AscariEntity;
import com.pathogenesis.entity.DermatophyteEntity;
import com.pathogenesis.entity.StrongyloideEntity;
import com.pathogenesis.entity.TaeniaEntity;
import com.pathogenesis.entity.InfluenzaEntity;
import com.pathogenesis.entity.PhageEntity;
import com.pathogenesis.entity.RogueCellEntity;
import com.pathogenesis.entity.StaphEntity;
import com.pathogenesis.entity.StreptococcusEntity;
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

            spawnAtPosition(world, pickEnemyType(i), spawnX, spawnY, spawnZ);
        }
    }

    // -------------------------------------------------------------------------
    // Enemy type selection and spawning
    // -------------------------------------------------------------------------

    /**
     * Spawns one "unit" at the given position. Staph spawns as a cluster of 3;
     * all other types spawn a single entity.
     *
     * Type codes:
     *   0 = Staph (cluster)   1 = Streptococcus   2 = Dermatophyte
     *   3 = RogueCell         4 = Viron            5 = Influenza
     *   6 = Coronavirus       7 = Phage
     *   8 = Ascari            9 = Taenia           10 = Strongyloide
     */
    private static void spawnAtPosition(ServerWorld world, int type, double x, double y, double z) {
        if (type == 0) {
            // Staph spawns as a tight cluster of 3 — forces AoE play
            for (int c = 0; c < 3; c++) {
                double a = (2 * Math.PI / 3) * c;
                StaphEntity s = new StaphEntity(ModEntities.STAPH, world);
                s.refreshPositionAndAngles(x + Math.cos(a) * 1.2, y, z + Math.sin(a) * 1.2, 0, 0);
                world.spawnEntity(s);
            }
            return;
        }
        net.minecraft.entity.mob.MobEntity enemy = switch (type) {
            case 1  -> new StreptococcusEntity(ModEntities.STREPTOCOCCUS, world);
            case 2  -> new DermatophyteEntity(ModEntities.DERMATOPHYTE, world);
            case 3  -> new RogueCellEntity(ModEntities.ROGUE_CELL, world);
            case 4  -> new VironEntity(ModEntities.VIRON, world);
            case 5  -> new InfluenzaEntity(ModEntities.INFLUENZA, world);
            case 6  -> new CoronavirusEntity(ModEntities.CORONAVIRUS, world);
            case 8  -> new AscariEntity(ModEntities.ASCARI, world);
            case 9  -> new TaeniaEntity(ModEntities.TAENIA, world);
            case 10 -> new StrongyloideEntity(ModEntities.STRONGYLOIDE, world);
            default -> new PhageEntity(ModEntities.PHAGE, world);
        };
        enemy.refreshPositionAndAngles(x, y, z, 0, 0);
        world.spawnEntity(enemy);
    }

    /**
     * Returns an enemy type code based on wave number.
     * Stage 1 (waves 1-3): skin pathogens only — Staph, Strep, Dermatophyte.
     * Wave 4+: later-stage enemies start mixing in as the infection advances.
     */
    private static int pickEnemyType(int enemyIndex) {
        int slot = enemyIndex % 20;
        if (waveNumber <= 3) {
            // Stage 1 — skin: 60% Staph clusters, 25% Strep, 15% Dermatophyte
            if (slot < 12) return 0;
            if (slot < 17) return 1;
            return 2;
        } else if (waveNumber <= 5) {
            // Transition: skin enemies plus RogueCell appearing
            if (slot < 6)  return 0;   // 30% Staph
            if (slot < 10) return 1;   // 20% Strep
            if (slot < 12) return 2;   // 10% Dermatophyte
            if (slot < 17) return 3;   // 25% RogueCell
            return 4;                  // 15% Viron
        } else if (waveNumber <= 8) {
            // Mid-game — full roster plus worms start appearing
            if (slot < 4)  return 0;   // 20% Staph
            if (slot < 6)  return 1;   // 10% Strep
            if (slot < 7)  return 2;   //  5% Dermatophyte
            if (slot < 10) return 3;   // 15% RogueCell
            if (slot < 13) return 4;   // 15% Viron
            if (slot < 15) return 5;   // 10% Influenza
            if (slot < 17) return 6;   // 10% Coronavirus
            if (slot < 18) return 8;   //  5% Ascari — growing nightmare
            if (slot < 19) return 9;   //  5% Taenia — segmented horror
            return 7;                  //  5% Phage
        } else {
            // Late game — worms are common, everything escalates
            if (slot < 3)  return 0;   // 15% Staph
            if (slot < 5)  return 3;   // 10% RogueCell
            if (slot < 7)  return 4;   // 10% Viron
            if (slot < 9)  return 6;   // 10% Coronavirus
            if (slot < 12) return 8;   // 15% Ascari — growing nightmare
            if (slot < 15) return 9;   // 15% Taenia — segmented horror
            if (slot < 17) return 10;  // 10% Strongyloide — self-replicating threat
            if (slot < 18) return 5;   //  5% Influenza
            if (slot < 19) return 7;   //  5% Phage
            return 10;                 //  5% extra Strongyloide
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
