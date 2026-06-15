package com.pathogenesis;

import com.pathogenesis.init.ModEntities;
import com.pathogenesis.init.ModItems;
import com.pathogenesis.system.HostHealth;
import com.pathogenesis.system.WaveSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PathogenesisMod implements ModInitializer {

    public static final String MOD_ID = "pathogenesis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Simple tick-based task scheduler: each entry fires once when server tick >= targetTick
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

        // Tick event to fire scheduled tasks
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

        // Intro cutscene on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // Darkness throughout the whole cutscene
            player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.DARKNESS, 220, 0, false, false));

            // Helper to send a dialogue line as title+subtitle
            // fade in 5t, hold 45t, fade out 5t = 55 ticks per line
            Runnable[] lines = {
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("Patient vitals are dropping fast...").formatted(Formatting.WHITE)));
                    player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.6f, 1.2f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("The pathogens are multiplying. We're losing control.").formatted(Formatting.WHITE)));
                    player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.MASTER, 0.6f, 0.9f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Doc432").formatted(Formatting.AQUA, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("There's only one thing left to do.").formatted(Formatting.YELLOW)));
                    player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.MASTER, 0.6f, 1.1f);
                },
                () -> {
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 45, 5));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Biotech92130").formatted(Formatting.GREEN, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("I'm going in.").formatted(Formatting.WHITE)));
                    player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.MASTER, 0.6f, 0.85f);
                },
                () -> {
                    // Big PATHOGENESIS title drop
                    player.networkHandler.sendPacket(new TitleFadeS2CPacket(20, 80, 20));
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("PATHOGENESIS").formatted(Formatting.DARK_RED, Formatting.BOLD)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("A war rages within...").formatted(Formatting.RED)));
                    player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 1.0f, 0.8f);
                }
            };

            // Fire each line 55 ticks apart, then the big title at the end
            int[] delays = {5, 60, 115, 170, 230};
            for (int i = 0; i < lines.length; i++) {
                final Runnable line = lines[i];
                scheduleAt(server, delays[i], line);
            }
        });

        LOGGER.info("Pathogenesis mod ready. Pathogens incoming!");
    }
}
