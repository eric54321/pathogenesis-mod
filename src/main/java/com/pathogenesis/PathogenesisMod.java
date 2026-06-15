package com.pathogenesis;

import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModItems;
import com.pathogenesis.system.HostHealth;
import com.pathogenesis.system.WaveSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
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
import net.minecraft.village.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PathogenesisMod implements ModInitializer {

    public static final String MOD_ID = "pathogenesis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private record ScheduledTask(long targetTick, Runnable action) {}
    private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    static void scheduleAt(MinecraftServer server, int delayTicks, Runnable action) {
        scheduledTasks.add(new ScheduledTask(server.getTicks() + delayTicks, action));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Pathogenesis mod initializing...");

        ModEntities.register();
        ModItems.register();
        WaveSpawner.register();
        HostHealth.register();

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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld world = player.getServerWorld();

            // Darkness for the whole cutscene
            player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.DARKNESS, 260, 0, false, false));

            // Spawn two villagers facing each other in front of the player
            double yawRad = Math.toRadians(player.getYaw());
            double fwdX  = -Math.sin(yawRad);
            double fwdZ  =  Math.cos(yawRad);
            double rgtX  =  Math.cos(yawRad);
            double rgtZ  =  Math.sin(yawRad);

            // Doc432 — cleric (medical), stands to the LEFT
            VillagerEntity doc = new VillagerEntity(EntityType.VILLAGER, world);
            doc.setPos(player.getX() + fwdX * 4 - rgtX * 1.5,
                       player.getY(),
                       player.getZ() + fwdZ * 4 - rgtZ * 1.5);
            doc.setCustomName(Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD));
            doc.setCustomNameVisible(true);
            doc.setAiDisabled(true);
            doc.setInvulnerable(true);
            doc.setSilent(true);
            doc.getVillagerData().withProfession(VillagerProfession.CLERIC);
            float docFacing = player.getYaw() + 90f; // faces right (toward Biotech)
            doc.setYaw(docFacing);
            doc.setBodyYaw(docFacing);
            doc.setHeadYaw(docFacing);
            world.spawnEntity(doc);

            // Biotech92130 — cartographer (scientist), stands to the RIGHT
            VillagerEntity bio = new VillagerEntity(EntityType.VILLAGER, world);
            bio.setPos(player.getX() + fwdX * 4 + rgtX * 1.5,
                       player.getY(),
                       player.getZ() + fwdZ * 4 + rgtZ * 1.5);
            bio.setCustomName(Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD));
            bio.setCustomNameVisible(true);
            bio.setAiDisabled(true);
            bio.setInvulnerable(true);
            bio.setSilent(true);
            bio.getVillagerData().withProfession(VillagerProfession.CARTOGRAPHER);
            float bioFacing = player.getYaw() - 90f; // faces left (toward Doc)
            bio.setYaw(bioFacing);
            bio.setBodyYaw(bioFacing);
            bio.setHeadYaw(bioFacing);
            world.spawnEntity(bio);

            // Dialogue lines — title = speaker name, subtitle = their line
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
                        Text.literal("There's only one thing left to do.").formatted(Formatting.YELLOW)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.MASTER, 0.8f, 1.1f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("I'm going in.").formatted(Formatting.WHITE)));
                    world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.MASTER, 0.8f, 0.85f);
                },
                () -> {
                    // Despawn the two villagers just before the big title
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

            int[] delays = {5, 60, 115, 170, 230};
            for (int i = 0; i < lines.length; i++) {
                final Runnable line = lines[i];
                scheduleAt(server, delays[i], line);
            }
        });

        LOGGER.info("Pathogenesis mod ready. Pathogens incoming!");
    }
}
