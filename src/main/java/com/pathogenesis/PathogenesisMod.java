package com.pathogenesis;

import com.pathogenesis.init.ModArmorMaterials;
import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModItems;
import com.pathogenesis.system.ArenaPersistentState;
import com.pathogenesis.system.BossArena;
import com.pathogenesis.system.HostHealth;
import com.pathogenesis.system.ParkourCourse;
import com.pathogenesis.system.SkinTerrain;
import com.pathogenesis.system.WaveSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PathogenesisMod implements ModInitializer {

    public static final String MOD_ID = "pathogenesis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private record ScheduledTask(long targetTick, UUID owner, Runnable action) {}
    private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    // Tracks the active intro cutscene for each player so it can be skipped on demand.
    private record CutsceneState(ServerWorld world, List<BlockPos> roomBlocks,
                                  VillagerEntity doc, VillagerEntity bio) {}
    private static final Map<UUID, CutsceneState> activeCutscenes = new HashMap<>();

    static void scheduleAt(MinecraftServer server, int delayTicks, Runnable action) {
        scheduledTasks.add(new ScheduledTask(server.getTicks() + delayTicks, null, action));
    }

    static void scheduleAt(MinecraftServer server, int delayTicks, UUID owner, Runnable action) {
        scheduledTasks.add(new ScheduledTask(server.getTicks() + delayTicks, owner, action));
    }

    // Where the player actually lands after the cutscene. Prefers the skin terrain's
    // saved floor height over world.getSpawnPos(), whose Y is the stale
    // world-generation surface height from before SkinTerrain regenerated the ground.
    private static BlockPos landingSpot(ServerWorld world) {
        ArenaPersistentState state = ArenaPersistentState.getOrCreate(world);
        return state.isSkinTerrainBuilt() ? state.getCenter() : world.getSpawnPos();
    }

    // Instantly cleans up a player's cutscene: removes the room, clears effects/titles,
    // cancels any remaining scheduled dialogue, and teleports them to world spawn.
    private static void skipCutscene(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        CutsceneState state = activeCutscenes.remove(id);
        if (state == null) {
            player.sendMessage(Text.literal("No cutscene is currently playing.").formatted(Formatting.GRAY), false);
            return;
        }

        scheduledTasks.removeIf(task -> id.equals(task.owner()));

        if (state.doc().isAlive()) state.doc().discard();
        if (state.bio().isAlive()) state.bio().discard();

        for (BlockPos pos : state.roomBlocks()) {
            state.world().setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }

        player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
        player.removeStatusEffect(StatusEffects.DARKNESS);
        player.removeStatusEffect(StatusEffects.SLOWNESS);

        BlockPos spawnPos = landingSpot(state.world());
        player.networkHandler.requestTeleport(
            spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0f, 0f);

        player.sendMessage(Text.literal("Cutscene skipped.").formatted(Formatting.GRAY), false);
    }

    // Places a block and tracks it for later cleanup
    private static void place(ServerWorld world, int x, int y, int z, Block block, List<BlockPos> track) {
        BlockPos pos = new BlockPos(x, y, z);
        world.setBlockState(pos, block.getDefaultState(), 2);
        track.add(pos);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Pathogenesis mod initializing...");

        ModEntities.register();
        ModArmorMaterials.register();
        ModItems.register();
        WaveSpawner.register();
        HostHealth.register();
        BossArena.register();
        SkinTerrain.register();
        ParkourCourse.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = server.getTicks();
            Iterator<ScheduledTask> it = scheduledTasks.iterator();
            while (it.hasNext()) {
                ScheduledTask task = it.next();
                if (now >= task.targetTick()) {
                    task.action().run();
                    it.remove();
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("skipcutscene")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendError(Text.literal("Only players can run this command."));
                        return 0;
                    }
                    skipCutscene(player);
                    return 1;
                }));
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld world = player.getServerWorld();

            // Save original position to TP back after cutscene
            double origX = player.getX(), origY = player.getY(), origZ = player.getZ();
            float origYaw = player.getYaw(), origPitch = player.getPitch();

            // Hospital room is built high in the sky above the player's X/Z
            int rx = player.getBlockX();
            int ry = 280;
            int rz = player.getBlockZ();

            List<BlockPos> roomBlocks = new ArrayList<>();

            // Bigger room — outer walls at ±7, height 7 (ceiling at ry+6)
            int W = 7; // half-width

            // Checkered floor: white + light gray concrete
            for (int x = rx-W; x <= rx+W; x++) {
                for (int z = rz-W; z <= rz+W; z++) {
                    Block b = ((x + z) % 2 == 0) ? Blocks.WHITE_CONCRETE : Blocks.LIGHT_GRAY_CONCRETE;
                    place(world, x, ry-1, z, b, roomBlocks);
                }
            }

            // White concrete ceiling
            for (int x = rx-W; x <= rx+W; x++) {
                for (int z = rz-W; z <= rz+W; z++) {
                    place(world, x, ry+6, z, Blocks.WHITE_CONCRETE, roomBlocks);
                }
            }

            // Sea lantern ceiling lights (6 of them spread out)
            for (int[] off : new int[][]{{-4,-4},{0,-4},{4,-4},{-4,4},{0,4},{4,4}}) {
                place(world, rx+off[0], ry+6, rz+off[1], Blocks.SEA_LANTERN, roomBlocks);
            }

            // White concrete walls
            for (int y = ry; y <= ry+5; y++) {
                for (int x = rx-W; x <= rx+W; x++) {
                    place(world, x, y, rz-W, Blocks.WHITE_CONCRETE, roomBlocks); // north
                    place(world, x, y, rz+W, Blocks.WHITE_CONCRETE, roomBlocks); // south
                }
                for (int z = rz-(W-1); z <= rz+(W-1); z++) {
                    place(world, rx+W, y, z, Blocks.WHITE_CONCRETE, roomBlocks); // east
                    place(world, rx-W, y, z, Blocks.WHITE_CONCRETE, roomBlocks); // west
                }
            }

            // Large glass windows in south wall
            for (int x = rx-3; x <= rx+3; x++) {
                for (int y = ry+1; y <= ry+4; y++) {
                    place(world, x, y, rz+W, Blocks.GLASS_PANE, roomBlocks);
                }
            }

            // Also small windows on east and west walls
            for (int y = ry+2; y <= ry+3; y++) {
                place(world, rx+W, y, rz-2, Blocks.GLASS_PANE, roomBlocks);
                place(world, rx+W, y, rz+2, Blocks.GLASS_PANE, roomBlocks);
                place(world, rx-W, y, rz-2, Blocks.GLASS_PANE, roomBlocks);
                place(world, rx-W, y, rz+2, Blocks.GLASS_PANE, roomBlocks);
            }

            // Hospital beds (white wool) in north corners — bigger beds
            for (int dz = -6; dz <= -4; dz++) {
                place(world, rx-5, ry,   rz+dz, Blocks.WHITE_WOOL, roomBlocks);
                place(world, rx-4, ry,   rz+dz, Blocks.WHITE_WOOL, roomBlocks);
                place(world, rx+5, ry,   rz+dz, Blocks.WHITE_WOOL, roomBlocks);
                place(world, rx+4, ry,   rz+dz, Blocks.WHITE_WOOL, roomBlocks);
            }
            // Raised headboard
            place(world, rx-5, ry+1, rz-6, Blocks.WHITE_WOOL, roomBlocks);
            place(world, rx-4, ry+1, rz-6, Blocks.WHITE_WOOL, roomBlocks);
            place(world, rx+5, ry+1, rz-6, Blocks.WHITE_WOOL, roomBlocks);
            place(world, rx+4, ry+1, rz-6, Blocks.WHITE_WOOL, roomBlocks);

            // Iron block medical equipment columns on east/west walls
            for (int y = ry; y <= ry+3; y++) {
                place(world, rx+W, y, rz,   Blocks.IRON_BLOCK, roomBlocks);
                place(world, rx-W, y, rz,   Blocks.IRON_BLOCK, roomBlocks);
                place(world, rx+W, y, rz+4, Blocks.IRON_BLOCK, roomBlocks);
                place(world, rx-W, y, rz+4, Blocks.IRON_BLOCK, roomBlocks);
            }

            // Glass observation platform for the player to stand on (3x3, elevated)
            for (int x = rx-1; x <= rx+1; x++) {
                for (int z = rz+3; z <= rz+5; z++) {
                    place(world, x, ry+3, z, Blocks.BARRIER, roomBlocks);
                }
            }

            // TP player onto the platform, looking down at the villagers
            // yaw=180 (facing north), pitch=40 (looking down ~40 degrees)
            player.networkHandler.requestTeleport(rx + 0.5, ry + 4, rz + 4, 180f, 40f);

            // Darkness + Slowness 255 (freezes movement) for the whole cutscene
            player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.DARKNESS, 620, 0, false, false));
            player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOWNESS, 620, 127, false, false));

            // Spawn villagers facing each other north of center
            // Doc432 — WEST side, facing EAST (toward Biotech)
            VillagerEntity doc = new VillagerEntity(EntityType.VILLAGER, world);
            doc.setPos(rx - 1.5, ry, rz - 1.0);
            doc.setCustomName(Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD));
            doc.setCustomNameVisible(true);
            doc.setAiDisabled(true);
            doc.setInvulnerable(true);
            doc.setSilent(true);
            doc.setYaw(-90f); doc.setBodyYaw(-90f); doc.setHeadYaw(-90f); // face east
            world.spawnEntity(doc);

            // Biotech92130 — EAST side, facing WEST (toward Doc)
            VillagerEntity bio = new VillagerEntity(EntityType.VILLAGER, world);
            bio.setPos(rx + 1.5, ry, rz - 1.0);
            bio.setCustomName(Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD));
            bio.setCustomNameVisible(true);
            bio.setAiDisabled(true);
            bio.setInvulnerable(true);
            bio.setSilent(true);
            bio.setYaw(90f); bio.setBodyYaw(90f); bio.setHeadYaw(90f); // face west
            world.spawnEntity(bio);

            activeCutscenes.put(player.getUuid(), new CutsceneState(world, roomBlocks, doc, bio));

            // Dialogue lines — each shown for ~55 ticks (~2.75 seconds)
            Runnable[] lines = {
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("Patient vitals are dropping fast...").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.8f, 1.2f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("The pathogens are multiplying. We're losing control.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.8f, 0.9f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("We've tried everything. Nothing is working.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.8f, 1.0f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("The host won't survive another hour at this rate.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.8f, 0.85f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 55, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("...There's only one thing left to do.").formatted(Formatting.YELLOW)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.MASTER, 0.8f, 1.1f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("You can't be serious. Nobody has ever come back from that.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.8f, 0.8f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("Someone has to. Suit up.").formatted(Formatting.YELLOW)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.MASTER, 0.8f, 1.3f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 55, 10));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("...I'm going in.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.MASTER, 0.8f, 0.75f);
                },
                () -> {
                    // Villagers disappear, PATHOGENESIS title drops
                    doc.discard();
                    bio.discard();
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(20, 80, 20));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("PATHOGENESIS").formatted(Formatting.DARK_RED, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("A war rages within...").formatted(Formatting.RED)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 1.0f, 0.8f);
                }
            };

            int[] delays = {5, 60, 115, 170, 235, 300, 355, 415, 480};
            for (int i = 0; i < lines.length; i++) {
                final Runnable line = lines[i];
                scheduleAt(server, delays[i], player.getUuid(), line);
            }

            // After the PATHOGENESIS title fades out: remove the room and TP player to world spawn
            scheduleAt(server, 605, player.getUuid(), () -> {
                if (!player.isAlive()) return;
                activeCutscenes.remove(player.getUuid());
                for (BlockPos pos : roomBlocks) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                }
                BlockPos spawnPos = landingSpot(world);
                player.networkHandler.requestTeleport(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0f, 0f);
            });
        });

        LOGGER.info("Pathogenesis mod ready. Pathogens incoming!");
    }
}
