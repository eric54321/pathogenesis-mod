package com.pathogenesis.system;

import com.pathogenesis.entity.BacteriumBossEntity;
import com.pathogenesis.init.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class BossArena {

    private static BlockPos arenaCenter = null;
    private static boolean bossAlive = false;
    private static int checkTimer = 0;

    public static BlockPos getArenaCenter() { return arenaCenter; }
    public static void setBossDefeated() { bossAlive = false; }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(BossArena::onServerStart);
        ServerTickEvents.END_SERVER_TICK.register(BossArena::onTick);
    }

    private static void onServerStart(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        ArenaPersistentState state = ArenaPersistentState.getOrCreate(world);
        if (!state.isBuilt()) {
            BlockPos spawn = world.getSpawnPos();
            int x = spawn.getX();
            int z = spawn.getZ() - 600;
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            arenaCenter = new BlockPos(x, y, z);
            buildArena(world, arenaCenter);
            state.setCenter(arenaCenter);
            state.setBuilt(true);
        } else {
            arenaCenter = state.getCenter();
        }
    }

    private static void onTick(MinecraftServer server) {
        if (arenaCenter == null || bossAlive) return;
        if (++checkTimer < 20) return;
        checkTimer = 0;

        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getBlockPos().isWithinDistance(arenaCenter, 35)) {
                spawnBoss(world);
                break;
            }
        }
    }

    private static void spawnBoss(ServerWorld world) {
        bossAlive = true;
        BacteriumBossEntity boss = new BacteriumBossEntity(ModEntities.BACTERIUM_BOSS, world);
        boss.refreshPositionAndAngles(
            arenaCenter.getX() + 0.5,
            arenaCenter.getY() + 3,
            arenaCenter.getZ() + 0.5,
            0, 0
        );
        world.spawnEntity(boss);

        Text msg = Text.literal("☣ THE PATHOGEN HAS AWAKENED! ☣")
            .formatted(Formatting.DARK_GREEN, Formatting.BOLD);
        for (PlayerEntity p : world.getPlayers()) {
            p.sendMessage(msg, false);
        }
    }

    // =========================================================================
    // Structure builder
    // =========================================================================

    private static void buildArena(ServerWorld world, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int RADIUS = 24;
        int WALL_H = 12;

        // --- Floor: circular, alternating blackstone / nether bricks ---
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (dist(x, z) <= RADIUS) {
                    Block b = ((Math.abs(x) + Math.abs(z)) % 2 == 0)
                        ? Blocks.BLACKSTONE : Blocks.NETHER_BRICKS;
                    place(world, cx + x, cy, cz + z, b);
                    // Clear the air above the floor
                    for (int y = 1; y <= WALL_H + 4; y++) {
                        place(world, cx + x, cy + y, cz + z, Blocks.AIR);
                    }
                }
            }
        }

        // --- Sea lantern ring in floor at radius 14 ---
        for (int deg = 0; deg < 360; deg += 24) {
            double rad = Math.toRadians(deg);
            int lx = (int) Math.round(Math.cos(rad) * 14);
            int lz = (int) Math.round(Math.sin(rad) * 14);
            place(world, cx + lx, cy, cz + lz, Blocks.SEA_LANTERN);
        }

        // --- Slime blobs at radius 8 (organic decoration) ---
        for (int deg = 0; deg < 360; deg += 45) {
            double rad = Math.toRadians(deg);
            int lx = (int) Math.round(Math.cos(rad) * 8);
            int lz = (int) Math.round(Math.sin(rad) * 8);
            place(world, cx + lx, cy + 1, cz + lz, Blocks.SLIME_BLOCK);
            place(world, cx + lx, cy + 2, cz + lz, Blocks.SLIME_BLOCK);
        }

        // --- Lime concrete patches (biological matter) ---
        for (int deg = 15; deg < 360; deg += 45) {
            double rad = Math.toRadians(deg);
            int lx = (int) Math.round(Math.cos(rad) * 11);
            int lz = (int) Math.round(Math.sin(rad) * 11);
            place(world, cx + lx, cy, cz + lz, Blocks.LIME_CONCRETE);
            place(world, cx + lx + 1, cy, cz + lz, Blocks.LIME_CONCRETE);
            place(world, cx + lx, cy, cz + lz + 1, Blocks.LIME_CONCRETE);
        }

        // --- Circular wall ---
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                double d = dist(x, z);
                if (d >= RADIUS - 1.5 && d <= RADIUS) {
                    // South entrance gap: skip when x in [-3,3] and z is positive extreme
                    if (z >= RADIUS - 2 && Math.abs(x) <= 3) continue;

                    for (int y = 1; y <= WALL_H; y++) {
                        Block b = Blocks.NETHER_BRICKS;
                        // Purple glass windows at y 4-7, roughly every 30 degrees of arc
                        if (y >= 4 && y <= 7) {
                            double angle = Math.toDegrees(Math.atan2(z, x));
                            int slot = (int)((angle + 180) / 30);
                            if (slot % 2 == 0) b = Blocks.PURPLE_STAINED_GLASS;
                        }
                        place(world, cx + x, cy + y, cz + z, b);
                    }
                    // Crenellation top
                    if ((x + z) % 3 == 0) {
                        place(world, cx + x, cy + WALL_H + 1, cz + z, Blocks.NETHER_BRICK_WALL);
                    }
                }
            }
        }

        // --- 4 massive corner pillars (polished blackstone, 3x3, height 15) ---
        int[][] pillarPos = {{-17, -17}, {17, -17}, {-17, 17}, {17, 17}};
        for (int[] p : pillarPos) {
            for (int y = 0; y <= WALL_H + 3; y++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block b = (y == 0 || y == WALL_H + 3)
                            ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE;
                        place(world, cx + p[0] + dx, cy + y, cz + p[1] + dz, b);
                    }
                }
            }
            // Glowstone cap
            place(world, cx + p[0], cy + WALL_H + 4, cz + p[1], Blocks.GLOWSTONE);
            // Crying obsidian ring around base
            for (int dx = -2; dx <= 2; dx++) {
                place(world, cx + p[0] + dx, cy, cz + p[1] - 2, Blocks.CRYING_OBSIDIAN);
                place(world, cx + p[0] + dx, cy, cz + p[1] + 2, Blocks.CRYING_OBSIDIAN);
            }
            for (int dz = -1; dz <= 1; dz++) {
                place(world, cx + p[0] - 2, cy, cz + p[1] + dz, Blocks.CRYING_OBSIDIAN);
                place(world, cx + p[0] + 2, cy, cz + p[1] + dz, Blocks.CRYING_OBSIDIAN);
            }
        }

        // --- Central altar (7x7 raised 2 blocks, red nether brick) ---
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                place(world, cx + x, cy + 1, cz + z, Blocks.RED_NETHER_BRICKS);
                place(world, cx + x, cy + 2, cz + z, Blocks.RED_NETHER_BRICKS);
            }
        }
        // Obsidian border around altar base
        for (int x = -4; x <= 4; x++) {
            place(world, cx + x, cy + 1, cz - 4, Blocks.OBSIDIAN);
            place(world, cx + x, cy + 1, cz + 4, Blocks.OBSIDIAN);
        }
        for (int z = -3; z <= 3; z++) {
            place(world, cx - 4, cy + 1, cz + z, Blocks.OBSIDIAN);
            place(world, cx + 4, cy + 1, cz + z, Blocks.OBSIDIAN);
        }
        // Sea lanterns in the altar corners
        place(world, cx - 3, cy + 3, cz - 3, Blocks.SEA_LANTERN);
        place(world, cx + 3, cy + 3, cz - 3, Blocks.SEA_LANTERN);
        place(world, cx - 3, cy + 3, cz + 3, Blocks.SEA_LANTERN);
        place(world, cx + 3, cy + 3, cz + 3, Blocks.SEA_LANTERN);

        // --- Bone block spires at cardinal positions (radius 18) ---
        int[][] spirePos = {{0, -18}, {0, 18}, {-18, 0}, {18, 0}};
        for (int[] sp : spirePos) {
            for (int y = 1; y <= 8; y++) {
                place(world, cx + sp[0], cy + y, cz + sp[1], Blocks.BONE_BLOCK);
            }
            // Skull-like top (green concrete)
            place(world, cx + sp[0], cy + 9,  cz + sp[1], Blocks.GREEN_CONCRETE);
            place(world, cx + sp[0], cy + 10, cz + sp[1], Blocks.LIME_CONCRETE);
        }

        // --- Iron bars ring at radius 20 between wall and spires (cage feel) ---
        for (int deg = 0; deg < 360; deg += 6) {
            double rad = Math.toRadians(deg);
            int lx = (int) Math.round(Math.cos(rad) * 20);
            int lz = (int) Math.round(Math.sin(rad) * 20);
            for (int y = 1; y <= 5; y++) {
                place(world, cx + lx, cy + y, cz + lz, Blocks.IRON_BARS);
            }
        }
    }

    private static double dist(int x, int z) {
        return Math.sqrt((double) x * x + (double) z * z);
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
