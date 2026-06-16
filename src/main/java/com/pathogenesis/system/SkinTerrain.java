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

    private static final int RADIUS      = 300; // 600x600 skin (patient body)
    private static final int DRAPE       = 320; // light blue surgical draping border
    private static final int ROOM_RADIUS = 380; // surgery room walls
    private static final int ROOM_HEIGHT = 30;  // wall + ceiling height

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

        generateSkin(world, cx, cy, cz);
        buildSurgeryRoom(world, cx, cy, cz);
        state.setSkinTerrainBuilt(true);
    }

    // ── Step 1: build the fleshy skin terrain (patient body) ───────────────
    private static void generateSkin(ServerWorld world, int cx, int cy, int cz) {
        Random rand = new Random(12345L);

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                // Fat layer
                for (int dy = -6; dy <= -4; dy++) {
                    place(world, cx + x, cy + dy, cz + z,
                        ((x + z) % 3 == 0) ? Blocks.HONEY_BLOCK : Blocks.YELLOW_TERRACOTTA);
                }
                // Dermis
                place(world, cx + x, cy - 3, cz + z, Blocks.RED_TERRACOTTA);
                place(world, cx + x, cy - 2, cz + z, Blocks.RED_TERRACOTTA);
                // Epidermis
                place(world, cx + x, cy - 1, cz + z, Blocks.PINK_TERRACOTTA);
                place(world, cx + x, cy,     cz + z,
                    ((Math.abs(x) + Math.abs(z)) % 7 == 0)
                        ? Blocks.PINK_CONCRETE_POWDER : Blocks.PINK_TERRACOTTA);
            }
        }

        // Hair follicles
        for (int i = 0; i < 180; i++) {
            int hx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int hz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int height = 3 + rand.nextInt(5);
            for (int y = 1; y <= height; y++) place(world, hx, cy + y, hz, Blocks.BROWN_TERRACOTTA);
            if (rand.nextBoolean()) place(world, hx, cy + height + 1, hz, Blocks.DEAD_BUSH);
        }

        // Pores
        for (int i = 0; i < 110; i++) {
            int px = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int pz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int size = rand.nextBoolean() ? 0 : 1;
            for (int dx = 0; dx <= size; dx++) for (int dz = 0; dz <= size; dz++) {
                place(world, px + dx, cy,     pz + dz, Blocks.AIR);
                place(world, px + dx, cy - 1, pz + dz, Blocks.AIR);
                place(world, px + dx, cy - 2, pz + dz, Blocks.RED_TERRACOTTA);
            }
        }

        // Infection wounds
        for (int i = 0; i < 22; i++) {
            int wx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int wz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            int r = 4 + rand.nextInt(6);
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    place(world, wx + dx, cy,     wz + dz, Blocks.CRIMSON_NYLIUM);
                    place(world, wx + dx, cy - 1, wz + dz, Blocks.NETHERRACK);
                    if (rand.nextInt(3) == 0) place(world, wx + dx, cy + 1, wz + dz, Blocks.CRIMSON_ROOTS);
                    if (dx * dx + dz * dz <= (r / 2) * (r / 2) && rand.nextInt(4) == 0)
                        place(world, wx + dx, cy - 2, wz + dz, Blocks.MAGMA_BLOCK);
                }
            }
        }

        // Sebaceous glands
        for (int i = 0; i < 45; i++) {
            int gx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int gz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            for (int dx = 0; dx <= 2; dx++) for (int dz = 0; dz <= 2; dz++) {
                boolean wall = (dx == 0 || dx == 2 || dz == 0 || dz == 2);
                place(world, gx + dx, cy - 2, gz + dz, wall ? Blocks.SLIME_BLOCK : Blocks.AIR);
                place(world, gx + dx, cy - 3, gz + dz, wall ? Blocks.SLIME_BLOCK : Blocks.AIR);
            }
        }

        // Cell nuclei lighting
        for (int i = 0; i < 130; i++) {
            int lx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int lz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            place(world, lx, cy - 5, lz, Blocks.SEA_LANTERN);
        }

        // Surface veins
        for (int t = -RADIUS; t <= RADIUS; t++) {
            for (int dx = -1; dx <= 1; dx++) {
                place(world, cx + 15 + dx, cy,     cz + t, Blocks.RED_CONCRETE);
                place(world, cx + 15 + dx, cy - 1, cz + t, Blocks.RED_TERRACOTTA);
            }
            for (int dz = -1; dz <= 1; dz++) {
                place(world, cx + t, cy,     cz - 10 + dz, Blocks.RED_CONCRETE);
                place(world, cx + t, cy - 1, cz - 10 + dz, Blocks.RED_TERRACOTTA);
            }
        }

        // Sweat glands
        for (int i = 0; i < 32; i++) {
            int sx = cx + rand.nextInt(RADIUS * 2) - RADIUS;
            int sz = cz + rand.nextInt(RADIUS * 2) - RADIUS;
            place(world, sx, cy,     sz, Blocks.CYAN_TERRACOTTA);
            place(world, sx, cy - 1, sz, Blocks.CYAN_TERRACOTTA);
            place(world, sx, cy - 2, sz, Blocks.CYAN_TERRACOTTA);
        }
    }

    // ── Step 3: build the operating room around the skin ───────────────────
    private static void buildSurgeryRoom(ServerWorld world, int cx, int cy, int cz) {
        int R = ROOM_RADIUS;
        int H = ROOM_HEIGHT;

        // ── Clear the room interior and lay floor in one pass ──────────────
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                // Clear air above from surface to ceiling
                for (int y = cy + 1; y <= cy + H; y++) {
                    place(world, cx + x, y, cz + z, Blocks.AIR);
                }

                if (Math.abs(x) <= RADIUS && Math.abs(z) <= RADIUS) {
                    // Inside skin area — skin generation handles the floor
                } else if (Math.abs(x) <= DRAPE && Math.abs(z) <= DRAPE) {
                    // Surgical draping border
                    place(world, cx + x, cy,     cz + z, Blocks.LIGHT_BLUE_CONCRETE);
                    place(world, cx + x, cy - 1, cz + z, Blocks.LIGHT_BLUE_CONCRETE);
                } else {
                    // OR floor tiles
                    boolean isGrout = (x % 4 == 0 || z % 4 == 0);
                    place(world, cx + x, cy, cz + z,
                        isGrout ? Blocks.LIGHT_GRAY_CONCRETE : Blocks.WHITE_CONCRETE);
                }
            }
        }

        // ── Walls (north, south, east, west)
        for (int t = -R; t <= R; t++) {
            for (int y = 1; y <= H; y++) {
                // North (z = -R) and South (z = +R)
                place(world, cx + t, cy + y, cz - R, wallBlock(t, y, H));
                place(world, cx + t, cy + y, cz + R, wallBlock(t, y, H));
                // East (x = +R) and West (x = -R)
                place(world, cx + R, cy + y, cz + t, wallBlock(t, y, H));
                place(world, cx - R, cy + y, cz + t, wallBlock(t, y, H));
            }
        }

        // ── Ceiling (white concrete)
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                place(world, cx + x, cy + H + 1, cz + z, Blocks.WHITE_CONCRETE);
            }
        }

        // ── Surgical overhead lights — 25 lights in a 5x5 grid, widely spread
        for (int gx = -2; gx <= 2; gx++) {
            for (int gz = -2; gz <= 2; gz++) {
                int lx = cx + gx * 140;
                int lz = cz + gz * 140;
                // Wide disc on ceiling (radius 20)
                for (int dx = -20; dx <= 20; dx++) {
                    for (int dz = -20; dz <= 20; dz++) {
                        if (dx * dx + dz * dz <= 400) {
                            place(world, lx + dx, cy + H + 1, lz + dz, Blocks.SEA_LANTERN);
                        }
                    }
                }
                // Hanging tier 1 (radius 14)
                for (int dx = -14; dx <= 14; dx++) {
                    for (int dz = -14; dz <= 14; dz++) {
                        if (dx * dx + dz * dz <= 196) {
                            place(world, lx + dx, cy + H, lz + dz, Blocks.SEA_LANTERN);
                        }
                    }
                }
                // Hanging tier 2 / bright core (radius 8)
                for (int dx = -8; dx <= 8; dx++) {
                    for (int dz = -8; dz <= 8; dz++) {
                        if (dx * dx + dz * dz <= 64) {
                            place(world, lx + dx, cy + H - 1, lz + dz, Blocks.SEA_LANTERN);
                        }
                    }
                }
            }
        }

        // ── Quartz pillar columns at corners (3x3 cross-section)
        int[][] corners = {{-R + 2, -R + 2}, {R - 4, -R + 2}, {-R + 2, R - 4}, {R - 4, R - 4}};
        for (int[] c : corners) {
            for (int dy = 0; dy <= H + 1; dy++) {
                for (int dx = 0; dx <= 2; dx++) {
                    for (int dz = 0; dz <= 2; dz++) {
                        place(world, cx + c[0] + dx, cy + dy, cz + c[1] + dz, Blocks.QUARTZ_PILLAR);
                    }
                }
            }
            // Glowstone cap on each pillar
            place(world, cx + c[0] + 1, cy + H + 2, cz + c[1] + 1, Blocks.GLOWSTONE);
        }

        // ── Medical equipment stations along walls (iron block + dispenser)
        int[] stations = {-300, -150, 0, 150, 300};
        for (int t : stations) {
            // North and south walls
            for (int side : new int[]{-1, 1}) {
                int wz = cz + side * (R - 1);
                place(world, cx + t,     cy + 1, wz, Blocks.IRON_BLOCK);
                place(world, cx + t,     cy + 2, wz, Blocks.IRON_BLOCK);
                place(world, cx + t,     cy + 3, wz, Blocks.IRON_BLOCK);
                place(world, cx + t - 1, cy + 1, wz, Blocks.IRON_BLOCK);
                place(world, cx + t + 1, cy + 1, wz, Blocks.IRON_BLOCK);
            }
            // East and west walls
            for (int side : new int[]{-1, 1}) {
                int wx = cx + side * (R - 1);
                place(world, wx, cy + 1, cz + t,     Blocks.IRON_BLOCK);
                place(world, wx, cy + 2, cz + t,     Blocks.IRON_BLOCK);
                place(world, wx, cy + 3, cz + t,     Blocks.IRON_BLOCK);
                place(world, wx, cy + 1, cz + t - 1, Blocks.IRON_BLOCK);
                place(world, wx, cy + 1, cz + t + 1, Blocks.IRON_BLOCK);
            }
        }

        // ── Iron bar drain grates in the floor corners
        for (int[] c : corners) {
            for (int dx = 4; dx <= 14; dx++) {
                for (int dz = 4; dz <= 14; dz++) {
                    if ((dx + dz) % 2 == 0) {
                        place(world, cx + c[0] + dx, cy, cz + c[1] + dz, Blocks.IRON_BARS);
                    }
                }
            }
        }
    }

    // Wall block: white concrete tiles with light gray grout + cyan glass windows
    private static Block wallBlock(int t, int y, int H) {
        // Grout lines every 4 blocks across and every 6 blocks up
        if (t % 4 == 0 || y % 6 == 0) return Blocks.LIGHT_GRAY_CONCRETE;
        // Observation windows: upper 20% of wall, every ~80 blocks, 25-block wide spans
        if (y >= H - 14 && y <= H - 2) {
            int slot = ((t % 80) + 80) % 80;
            if (slot >= 8 && slot <= 32) return Blocks.CYAN_STAINED_GLASS;
        }
        return Blocks.WHITE_CONCRETE;
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
