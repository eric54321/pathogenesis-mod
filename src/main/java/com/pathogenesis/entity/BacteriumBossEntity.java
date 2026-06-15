package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * Bacterium Boss — Bacillus anthracis, the final boss of the wave system.
 *
 * Biology note: Bacillus anthracis produces deadly anthrax toxins that destroy
 * immune cells and prevent phagocytosis. It is one of the most resilient bacteria
 * known, forming protective spores to survive harsh conditions.
 *
 * Mechanics:
 *  - 200 HP, extremely tanky, slow but hits extremely hard
 *  - Boss health bar on screen for all nearby players
 *  - AoE acid slam every 4 seconds: Poison II + Weakness II to nearby players
 *  - Knockback resistance 0.95 — almost impossible to move
 *  - Spawns only on wave 10 (the final wave) as a singular boss
 */
public class BacteriumBossEntity extends HostileEntity {

    private final ServerBossBar bossBar = new ServerBossBar(
        Text.literal("Bacillus Anthracis").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
        BossBar.Color.GREEN,
        BossBar.Style.NOTCHED_10
    );

    private int stomptimer = 80;

    public BacteriumBossEntity(EntityType<? extends BacteriumBossEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,         200.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,     0.20)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,      14.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,       48.0)
            .add(EntityAttributes.GENERIC_ARMOR,              16.0)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,    8.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.95);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, LivingEntity.class, 24.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient()) {
            // Update boss bar health percentage
            bossBar.setPercent(this.getHealth() / this.getMaxHealth());

            // AoE acid stomp every 80 ticks (4 seconds)
            if (--stomptimer <= 0) {
                stomptimer = 80;
                doAcidStomp();
            }
        }
    }

    private void doAcidStomp() {
        this.getWorld().getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(8.0), p -> true)
            .forEach(player -> {
                player.damage(this.getWorld().getDamageSources().mobAttack(this), 8.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
            });
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        this.bossBar.clearPlayers();
    }
}
