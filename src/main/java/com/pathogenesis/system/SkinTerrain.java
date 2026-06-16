package com.pathogenesis.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Random;

public class SkinTerrain {

    private static final int RADIUS = 90; // 180x180 block area

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(SkinTerrain::onServerStart);
    }

    private static void onServerStart(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        ArenaPersistentState state = ArenaPersistentState.getOrCreate(world);
        if (state.isSkinTerrainBuilt()) return;

        BlockPos spawn = world.getSpawnPos();
        int cx = spawn.getX();
        int cz = spawn.getZ();
        int cy = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz);

        generate(world, cx, cy, cz);
        state.setSkinTerrainBuilt(true);
    }

    private static void generate(ServerWorld world, int cx, int cy, int cz) {
        Random rand = new Random(12345L);

        // ── Base terrain layers ─────────────────────────────────────────────
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {

                // Deep fat / subcutaneous layer (yellow)
                for (int dy = -6; dy <= -4; dy++) {
                    Block b = ((x + z) % 3 == 0) ? Blocks.HONEY_BLOCK : Blocks.YELLOW_TERRACOTTA;
                    place(world, cx + x, cy + dy, cz + z, b);
                }

                // Dermis (deep skin, reddish)
                place(world, cx + x, cy - 3, cz + z, Blocks.RED_TERRACOTTA);
                place(world, cx + x, cy - 2, cz + z, Blocks.RED_TERRACOTTA);

                // Epidermis top surface (pink, slight texture variation)
                place(world, cx + x, cy - 1, cz + z, Blocks.PINK_TERRACOTTA);
                Block surface = ((Math.abs(x) + Math.abs(z)) % 7 == 0)
                    ? Blocks.PINK_CONCRETE_POWDER : Blocks.PINK_TERRACOTTA;
                place(world, cx + x, cy, cz + z, surface);

                // Clear air above so the landscape is visible
                for (int dy = 1; dy <= 24; dy++) {
                    place(world, cx + x, cy + dy, cz + z, Blocks.AIR);
                }
            }
        }

        // ── Hair follicles (brown terracotta pillars) ──────────────────────
        for (int i = 0; i < 55; i++) {
            int hx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int hz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int height = 3 + rand.nextInt(5);
            for (int y = 1; y <= height; y++) {
                place(world, hx, cy + y, hz, Blocks.BROWN_TERRACOTTA);
            }
            // Optional dead bush tuft on top
            if (rand.nextBoolean()) {
                place(world, hx, cy + height + 1, hz, Blocks.DEAD_BUSH);
            }
        }

        // ── Pores (small surface holes exposing dermis below) ──────────────
        for (int i = 0; i < 35; i++) {
            int px = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int pz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int size = rand.nextBoolean() ? 0 : 1; // 1x1 or 2x2
            for (int dx = 0; dx <= size; dx++) {
                for (int dz = 0; dz <= size; dz++) {
                    place(world, px + dx, cy,     pz + dz, Blocks.AIR);
                    place(world, px + dx, cy - 1, pz + dz, Blocks.AIR);
                    // Exposed dermis at bottom
                    place(world, px + dx, cy - 2, pz + dz, Blocks.RED_TERRACOTTA);
                }
            }
        }

        // ── Infection wounds (crimson patches — bacteria entry points) ──────
        for (int i = 0; i < 7; i++) {
            int wx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int wz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int r = 4 + rand.nextInt(6);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        place(world, wx + dx, cy,     wz + dz, Blocks.CRIMSON_NYLIUM);
                        place(world, wx + dx, cy - 1, wz + dz, Blocks.NETHERRACK);
                        // Sparse crimson roots on surface
                        if (rand.nextInt(3) == 0) {
                            place(world, wx + dx, cy + 1, wz + dz, Blocks.CRIMSON_ROOTS);
                        }
                        // Some wounds have a festering center (magma)
                        if (dx * dx + dz * dz <= (r / 2) * (r / 2) && rand.nextInt(4) == 0) {
                            place(world, wx + dx, cy - 2, wz + dz, Blocks.MAGMA_BLOCK);
                        }
                    }
                }
            }
        }

        // ── Sebaceous glands (slime chambers just below surface) ───────────
        for (int i = 0; i < 14; i++) {
            int gx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int gz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            // 3x3 shell, hollow inside
            for (int dx = 0; dx <= 2; dx++) {
                for (int dz = 0; dz <= 2; dz++) {
                    boolean isWall = (dx == 0 || dx == 2 || dz == 0 || dz == 2);
                    place(world, gx + dx, cy - 2, gz + dz, isWall ? Blocks.SLIME_BLOCK : Blocks.AIR);
                    place(world, gx + dx, cy - 3, gz + dz, isWall ? Blocks.SLIME_BLOCK : Blocks.AIR);
                }
            }
        }

        // ── Cell nuclei lighting (sea lanterns deep below — cells glowing) ─
        for (int i = 0; i < 40; i++) {
            int lx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int lz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            place(world, lx, cy - 5, lz, Blocks.SEA_LANTERN);
        }

        // ── Surface veins (dark red terracotta lines running across skin) ──
        // Two veins crossing the terrain (one N-S, one E-W)
        for (int t = -RADIUS; t <= RADIUS; t++) {
            // N-S vein at x=+15, width 2
            for (int dx = -1; dx <= 1; dx++) {
                place(world, cx + 15 + dx, cy,     cz + t, Blocks.RED_CONCRETE);
                place(world, cx + 15 + dx, cy - 1, cz + t, Blocks.RED_TERRACOTTA);
            }
            // E-W vein at z=-10, width 2
            for (int dz = -1; dz <= 1; dz++) {
                place(world, cx + t, cy,     cz - 10 + dz, Blocks.RED_CONCRETE);
                place(world, cx + t, cy - 1, cz - 10 + dz, Blocks.RED_TERRACOTTA);
            }
        }

        // ── Sweat gland openings (blue-green tinted holes) ─────────────────
        for (int i = 0; i < 10; i++) {
            int sx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int sz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            place(world, sx, cy,     sz, Blocks.CYAN_TERRACOTTA);
            place(world, sx, cy - 1, sz, Blocks.CYAN_TERRACOTTA);
            place(world, sx, cy - 2, sz, Blocks.CYAN_TERRACOTTA);
        }
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
