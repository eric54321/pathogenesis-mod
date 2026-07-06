package com.pathogenesis.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class ParkourCourse {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(ParkourCourse::onServerStart);
    }

    private static void onServerStart(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        ArenaPersistentState state = ArenaPersistentState.getOrCreate(world);
        if (state.isParkourBuilt()) return;

        BlockPos center = state.getCenter();
        int cx = center.getX();
        int cz = center.getZ();
        // Use saved skin floor Y; fall back to heightmap if terrain not yet built
        int cy = (center.getY() != 0) ? center.getY()
                : world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz);

        build(world, cx, cy, cz);
        state.setParkourBuilt(true);
    }

    private static void build(ServerWorld world, int cx, int cy, int cz) {
        // Parkour starts on the east side of the skin, inside the surgery room
        // Platforms zigzag upward, chest at the top

        int[][] platforms = {
            // {relX, relY, relZ, size}  — starts 5 blocks north of spawn, goes east
            { 5,  1, -5, 4},  // Start — large 4x4 platform right next to spawn
            { 5,  2,  2, 3},  // easy
            {11,  4,  6, 2},
            { 5,  6, 12, 2},
            {11,  8, 18, 2},
            { 5, 10, 24, 2},
            {12, 13, 28, 1},  // harder — 1x1
            { 5, 15, 32, 2},
            {12, 18, 28, 1},  // harder — 1x1
            {18, 20, 32, 2},
            {24, 23, 26, 1},  // hardest — 1x1
            {18, 25, 32, 3},  // finish — nice big landing pad
        };

        // Build each platform — clear a 6-block tall column first so nothing is buried
        for (int[] p : platforms) {
            int px = cx + p[0];
            int py = cy + p[1];
            int pz = cz + p[2];
            int size = p[3];

            for (int dx = -1; dx < size + 1; dx++) {
                for (int dz = -1; dz < size + 1; dz++) {
                    for (int dy = -1; dy <= 6; dy++) {
                        place(world, px + dx, py + dy, pz + dz, Blocks.AIR);
                    }
                }
            }
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    Block block = (p[3] == 1) ? Blocks.GOLD_BLOCK :
                                  (p[3] >= 4) ? Blocks.SEA_LANTERN :
                                  Blocks.WHITE_CONCRETE;
                    place(world, px + dx, py, pz + dz, block);
                }
            }
        }

        // Sign at the start
        int[] start = platforms[0];
        BlockPos signPos = new BlockPos(cx + start[0], cy + start[1] + 1, cz + start[2] - 1);
        place(world, signPos.getX(), signPos.getY(), signPos.getZ(), Blocks.OAK_SIGN);

        // Chest on the final platform
        int[] finish = platforms[platforms.length - 1];
        BlockPos chestPos = new BlockPos(
            cx + finish[0] + 1,
            cy + finish[1] + 1,
            cz + finish[2] + 1
        );
        place(world, chestPos.getX(), chestPos.getY(), chestPos.getZ(), Blocks.CHEST);

        // Assign loot table to the chest — contents generate when first opened
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            RegistryKey<LootTable> lootTable = RegistryKey.of(
                RegistryKeys.LOOT_TABLE,
                Identifier.of("pathogenesis", "chests/parkour_reward")
            );
            chest.setLootTable(lootTable);
        }

        // Glowstone torches along the parkour path as guides
        for (int[] p : platforms) {
            place(world, cx + p[0] - 1, cy + p[1] + 1, cz + p[2], Blocks.GLOWSTONE);
        }
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
