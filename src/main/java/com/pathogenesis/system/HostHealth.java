package com.pathogenesis.system;

import com.pathogenesis.PathogenesisMod;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;

public class HostHealth {

    private static final int MAX_HEALTH = 100;
    private static final int DAMAGE_PER_HIT = 5;

    private static int currentHealth = MAX_HEALTH;

    private static final ServerBossBar BOSS_BAR = new ServerBossBar(
        Text.literal("Host Body: HEALTHY (100/100)"),
        BossBar.Color.GREEN,
        BossBar.Style.PROGRESS
    );

    public static void register() {
        // Add/remove players from the boss bar as they join and leave
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            BOSS_BAR.addPlayer(handler.player);
            sendJoinMessage(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            BOSS_BAR.removePlayer(handler.player)
        );

        // Drain host HP whenever one of our pathogens hits a player
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, appliedDamage, blocked) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            Entity attacker = source.getAttacker();
            if (attacker == null) return;
            Identifier typeId = Registries.ENTITY_TYPE.getId(attacker.getType());
            if (typeId != null && PathogenesisMod.MOD_ID.equals(typeId.getNamespace())) {
                damage(DAMAGE_PER_HIT, player.getServer());
            }
        });

        PathogenesisMod.LOGGER.info("HostHealth system registered.");
    }

    public static void damage(int amount, MinecraftServer server) {
        currentHealth = Math.max(0, currentHealth - amount);
        updateBossBar();
        if (currentHealth <= 0) {
            triggerGameOver(server);
        }
    }

    public static void triggerVictory(MinecraftServer server) {
        server.getPlayerManager().broadcast(
            Text.literal("★ THE HOST SURVIVED! Infection repelled! ★").formatted(Formatting.GOLD, Formatting.BOLD),
            false
        );
        reset();
    }

    private static void triggerGameOver(MinecraftServer server) {
        server.getPlayerManager().broadcast(
            Text.literal("✗ THE HOST HAS DIED. The infection won.").formatted(Formatting.DARK_RED, Formatting.BOLD),
            false
        );
        reset();
        WaveSpawner.reset();
    }

    public static void reset() {
        currentHealth = MAX_HEALTH;
        updateBossBar();
    }

    private static void updateBossBar() {
        float percent = (float) currentHealth / MAX_HEALTH;
        BOSS_BAR.setPercent(percent);

        BossBar.Color color;
        String status;
        if (percent > 0.6f) {
            color = BossBar.Color.GREEN;
            status = "HEALTHY";
        } else if (percent > 0.3f) {
            color = BossBar.Color.YELLOW;
            status = "INFECTED";
        } else {
            color = BossBar.Color.RED;
            status = "CRITICAL";
        }

        BOSS_BAR.setName(Text.literal("Host Body: " + status + " (" + currentHealth + "/" + MAX_HEALTH + ")"));
        BOSS_BAR.setColor(color);
    }

    private static void sendJoinMessage(ServerPlayerEntity player) {
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("⚠ PATHOGENESIS ⚠").formatted(Formatting.RED, Formatting.BOLD));
        player.sendMessage(Text.literal("You are an immune cell defending the human host.").formatted(Formatting.GRAY));
        player.sendMessage(Text.literal("Waves of pathogens attack every 2 minutes.").formatted(Formatting.GRAY));
        player.sendMessage(Text.literal("If the host body reaches 0 HP, the infection wins.").formatted(Formatting.GRAY));
        player.sendMessage(Text.literal("Watch the boss bar above — that is the host's health.").formatted(Formatting.YELLOW));
        player.sendMessage(Text.literal(""));
    }

    public static int getCurrentHealth() { return currentHealth; }
}
