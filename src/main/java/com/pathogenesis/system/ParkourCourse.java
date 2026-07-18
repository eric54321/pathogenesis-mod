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

            // Build relative to world spawn — NOT the player's live position — because
            // the intro cutscene (registered separately) teleports the player up into
            // a sky room and then drops them back at world spawn facing south (yaw 0)
            // once it finishes. Anchoring to their pre-cutscene position meant the course
            // ended up wherever they happened to be standing before the TP, not where
            // they actually land afterward.
            BlockPos pos = world.getSpawnPos();
            Direction facing = Direction.SOUTH;

            build(world, pos.getX(), pos.getY(), pos.getZ(), facing);
            state.setParkourBuilt(true);

            player.sendMessage(Text.literal(
                "§b§lPARKOUR COURSE BUILT!§r §fLook " + facing.asString().toUpperCase() +
                " right in front of you at spawn — glowing gold path leads to a reward chest!"
            ), false);
        });
    }

    private static void build(ServerWorld world, int cx, int cy, int cz, Direction facing) {
        int fx = facing.getOffsetX();
        int fz = facing.getOffsetZ();
        // sideways vector, perpendicular to facing, for platform width
        int sx = -fz;
        int sz = fx;

        // {forward distance, sideways offset, height gain, size} — mostly 1x1 platforms
        // with long sprint-jump gaps (3-4 blocks) and lateral zigzags for real difficulty.
        // Starts 20 blocks ahead of the player instead of right at their feet.
        int[][] steps = {
            {20, 0, 0, 4},   // Start — big platform 20 blocks ahead of the player
            {24, 0, 0, 2},   // warm-up hop
            {28, 2, 1, 1},   // side jump + rise — 1x1
            {32, 0, 1, 1},   // 1x1
            {36, -2, 2, 1},  // side jump the other way — 1x1
            {39, -2, 3, 1},  // tight gap — 1x1
            {43, 0, 3, 1},   // 1x1
            {47, 2, 4, 1},   // side jump — 1x1
            {50, 2, 5, 1},   // tight gap — 1x1
            {54, 0, 5, 1},   // 1x1
            {58, -2, 6, 1},  // side jump — 1x1
            {61, -2, 7, 1},  // tight gap — 1x1
            {65, 0, 8, 1},   // 1x1
            {69, 2, 9, 1},   // side jump — 1x1
            {72, 2, 10, 1},  // tight gap — 1x1
            {76, 0, 11, 1},  // 1x1
            {80, 0, 12, 1},  // 1x1 — final stretch, highest point
            {84, 0, 12, 3},  // finish — big landing pad
        };

        int[][] platforms = new int[steps.length][3];
        for (int i = 0; i < steps.length; i++) {
            int dist = steps[i][0];
            int side = steps[i][1];
            int rise = steps[i][2];
            platforms[i][0] = fx * dist + sx * side; // relX
            platforms[i][1] = rise;                  // relY
            platforms[i][2] = fz * dist + sz * side; // relZ
        }

        // Build each platform — clear a tall column first so nothing is buried,
        // and always clear the player's own start position too.
        for (int i = 0; i < platforms.length; i++) {
            int px = cx + platforms[i][0];
            int py = cy + platforms[i][1];
            int pz = cz + platforms[i][2];
            int size = steps[i][3];

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

        // Sign just before the start platform
        int[] start = platforms[0];
        BlockPos signPos = new BlockPos(cx + start[0] - fx, cy + 1, cz + start[2] - fz);
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
    }

    private static void place(ServerWorld world, int x, int y, int z, Block block) {
        world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 2);
    }
}
