package com.pathogenesis.system;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ParkourCourse {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ServerWorld world = player.getServerWorld();
            ArenaPersistentState state = ArenaPersistentState.getOrCreate(world);
            if (state.isParkourBuilt()) return;

            // Build directly at the player's feet, extending in the direction they are
            // currently facing — guarantees the course starts somewhere they can see
            // right now, instead of a computed offset that might be behind a wall.
            BlockPos pos = player.getBlockPos();
            Direction facing = player.getHorizontalFacing();

            build(world, pos.getX(), pos.getY(), pos.getZ(), facing);
            state.setParkourBuilt(true);

            player.sendMessage(Text.literal(
                "§b§lPARKOUR COURSE BUILT!§r §fLook " + facing.asString().toUpperCase() +
                " right in front of you — glowing gold path leads to a reward chest!"
            ), false);
        });
    }

    private static void build(ServerWorld world, int cx, int cy, int cz, Direction facing) {
        int fx = facing.getOffsetX();
        int fz = facing.getOffsetZ();
        // sideways vector, perpendicular to facing, for platform width
        int sx = -fz;
        int sz = fx;

        // {forward distance, height gain, size} — all steps go straight ahead, flat first
        int[][] steps = {
            {0, 0, 4},   // Start — big platform right at the player's feet
            {4, 0, 3},   // flat hop forward
            {8, 0, 3},   // flat hop forward
            {12, 1, 2},  // slight rise
            {16, 1, 2},
            {20, 2, 2},  // slight rise
            {24, 2, 1},  // harder — 1x1
            {28, 3, 2},
            {32, 3, 1},  // harder — 1x1
            {36, 4, 2},
            {40, 4, 1},  // hardest — 1x1
            {44, 5, 3},  // finish — big landing pad
        };

        int[][] platforms = new int[steps.length][3];
        for (int i = 0; i < steps.length; i++) {
            int dist = steps[i][0];
            int rise = steps[i][1];
            int size = steps[i][2];
            platforms[i][0] = fx * dist; // relX
            platforms[i][1] = rise;      // relY
            platforms[i][2] = fz * dist; // relZ (using facing axis; combined below)
        }

        // Build each platform — clear a tall column first so nothing is buried,
        // and always clear the player's own start position too.
        for (int i = 0; i < platforms.length; i++) {
            int px = cx + platforms[i][0];
            int py = cy + platforms[i][1];
            int pz = cz + platforms[i][2];
            int size = steps[i][2];

            for (int w = -1; w < size + 1; w++) {
                for (int l = -1; l < size + 1; l++) {
                    for (int dy = -1; dy <= 6; dy++) {
                        int bx = px + sx * w + fx * l;
                        int bz = pz + sz * w + fz * l;
                        place(world, bx, py + dy, bz, Blocks.AIR);
                    }
                }
            }
            for (int w = 0; w < size; w++) {
                for (int l = 0; l < size; l++) {
                    Block block = (size == 1) ? Blocks.GOLD_BLOCK :
                                  (size >= 4) ? Blocks.SEA_LANTERN :
                                  Blocks.GOLD_BLOCK;
                    int bx = px + sx * w + fx * l;
                    int bz = pz + sz * w + fz * l;
                    place(world, bx, py, bz, block);
                }
            }
        }

        // Sign at the start
        BlockPos signPos = new BlockPos(cx - fx, cy + 1, cz - fz);
        place(world, signPos.getX(), signPos.getY(), signPos.getZ(), Blocks.OAK_SIGN);

        // Chest on the final platform
        int[] finish = platforms[platforms.length - 1];
        BlockPos chestPos = new BlockPos(cx + finish[0], cy + finish[1] + 1, cz + finish[2]);
        place(world, chestPos.getX(), chestPos.getY(), chestPos.getZ(), Blocks.CHEST);

        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            RegistryKey<LootTable> lootTable = RegistryKey.of(
                RegistryKeys.LOOT_TABLE,
                Identifier.of("pathogenesis", "chests/parkour_reward")
            );
            chest.setLootTable(lootTable);
        }

        // Continuous glow trail directly beneath the path, one block below every platform,
        // plus a glowstone marker above every platform so it's visible from a distance.
        for (int i = 0; i < platforms.length - 1; i++) {
            int[] a = platforms[i];
            int[] b = platforms[i + 1];
            int aDist = steps[i][0];
            int bDist = steps[i + 1][0];
            int stepCount = Math.max(Math.abs(bDist - aDist), 1);

            for (int s = 0; s <= stepCount; s++) {
                double t = (double) s / stepCount;
                int gx = cx + (int) Math.round(a[0] + (b[0] - a[0]) * t);
                int gy = cy + (int) Math.round(a[1] + (b[1] - a[1]) * t);
                int gz = cz + (int) Math.round(a[2] + (b[2] - a[2]) * t);
                place(world, gx, gy - 1, gz, Blocks.GLOWSTONE);
                place(world, gx, gy + 3, gz, Blocks.GLOWSTONE);
            }
        }
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
