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

        BlockPos spawn = world.getSpawnPos();
        int cx = spawn.getX();
        int cz = spawn.getZ();
        int cy = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz);

        build(world, cx, cy, cz);
        state.setParkourBuilt(true);
    }

    private static void build(ServerWorld world, int cx, int cy, int cz) {
        // Parkour starts on the east side of the skin, inside the surgery room
        // Platforms zigzag upward, chest at the top

        int[][] platforms = {
            // {relX, relY, relZ, size}  size=2 means 2x2, size=3 means 3x3, size=1 means 1x1
            {320,  1, -20, 4},  // Start — large 4x4 platform
            {320,  2, -13, 3},  // easy
            {326,  4,  -7, 2},
            {320,  6,   0, 2},
            {326,  8,   6, 2},
            {320, 10,  12, 2},
            {327, 13,  16, 1},  // harder — 1x1
            {320, 15,  20, 2},
            {327, 18,  16, 1},  // harder — 1x1
            {333, 20,  20, 2},
            {339, 23,  14, 1},  // hardest — 1x1
            {333, 25,  20, 3},  // finish — nice big landing pad
        };

        // Build each platform
        for (int[] p : platforms) {
            int px = cx + p[0];
            int py = cy + p[1];
            int pz = cz + p[2];
            int size = p[3];

            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    Block block = (p[3] == 1) ? Blocks.GOLD_BLOCK :
                                  (p[3] >= 4) ? Blocks.SEA_LANTERN :
                                  Blocks.WHITE_CONCRETE;
                    place(world, px + dx, py, pz + dz, block);
                    // Clear air above each platform
                    for (int dy = 1; dy <= 4; dy++) {
                        place(world, px + dx, py + dy, pz + dz, Blocks.AIR);
                    }
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
