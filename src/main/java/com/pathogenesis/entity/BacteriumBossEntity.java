package com.pathogenesis.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
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
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Bacterium Boss — Bacillus anthracis.
 *
 * Phase 1 (green, >50% HP): slow physical attacks
 *   1. Toxin Cloud  — AoE + lingering poison/wither cloud
 *   2. Spore Burst  — 12 wither skulls fired in a ring
 *   3. Ground Slam  — leaps up, crashes down with huge knockback
 *   4. Charge       — rockets toward nearest player
 *
 * Phase 2 (blue, ≤50% HP): fast light-based attacks
 *   1. Lightning Storm  — 5 lightning bolts strike near players
 *   2. Frost Nova       — 20 snowballs in a ring applying Slowness IV + Blindness
 *   3. Radiant Pulse    — blinding flash then heavy AoE damage
 *   4. Plasma Volley    — fires 8 blue wither skulls straight at the target
 */
public class BacteriumBossEntity extends HostileEntity {

    // --- Boss bar ---
    private final ServerBossBar bossBar = new ServerBossBar(
        Text.literal("Bacillus Anthracis").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
        BossBar.Color.GREEN,
        BossBar.Style.NOTCHED_10
    );

    // --- Phase tracking ---
    private boolean phase2 = false;

    // --- Attack cycle ---
    private int attackPhase = 0;
    private int attackTimer = 100;

    // --- Slam state ---
    private boolean isSlamming = false;
    private int slamTick = 0;
    private double slamTargetY = 0;

    // --- Charge state ---
    private boolean isCharging = false;
    private int chargeTick = 0;
    private Vec3d chargeDir = Vec3d.ZERO;

    // --- Radiant Pulse: warn then strike ---
    private int radiantWarnTick = -1;

    public BacteriumBossEntity(EntityType<? extends BacteriumBossEntity> type, World world) {
        super(type, world);
    }

    public boolean isPhase2() { return phase2; }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,           200.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,       0.20)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,        14.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,         48.0)
            .add(EntityAttributes.GENERIC_ARMOR,                16.0)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,      8.0)
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
        if (this.getWorld().isClient()) return;

        ServerWorld world = (ServerWorld) this.getWorld();

        bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Check phase transition
        if (!phase2 && this.getHealth() <= this.getMaxHealth() * 0.5f) {
            enterPhase2(world);
        }

        // Radiant pulse strike fires after the warning tick
        if (radiantWarnTick >= 0) {
            radiantWarnTick--;
            if (radiantWarnTick == 0) {
                doRadiantStrike(world);
                radiantWarnTick = -1;
            }
            return;
        }

        if (isSlamming) { tickSlam(world); return; }
        if (isCharging) { tickCharge(world); return; }

        if (--attackTimer <= 0) {
            triggerNextAttack(world);
        }
    }

    // =========================================================================
    // Phase transition
    // =========================================================================

    private void enterPhase2(ServerWorld world) {
        phase2 = true;
        attackPhase = 0;
        attackTimer = 60;

        bossBar.setName(Text.literal("Bacillus Anthracis — PHASE 2").formatted(Formatting.AQUA, Formatting.BOLD));
        bossBar.setColor(BossBar.Color.BLUE);

        // Announce + dramatic sound
        for (PlayerEntity p : world.getPlayers()) {
            if (p instanceof ServerPlayerEntity sp) {
                sp.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 60, 10));
                sp.networkHandler.sendPacket(new TitleS2CPacket(
                    Text.literal("PHASE 2").formatted(Formatting.AQUA, Formatting.BOLD)));
                sp.networkHandler.sendPacket(new SubtitleS2CPacket(
                    Text.literal("It's mutating!").formatted(Formatting.WHITE)));
            }
        }

        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0f, 1.4f);

        // Burst of glowing particles
        world.spawnParticles(ParticleTypes.END_ROD,
            this.getX(), this.getY() + 1, this.getZ(), 80, 3.0, 3.0, 3.0, 0.2);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
            this.getX(), this.getY() + 1, this.getZ(), 60, 2.0, 2.0, 2.0, 0.3);
    }

    // =========================================================================
    // Attack dispatch
    // =========================================================================

    private void triggerNextAttack(ServerWorld world) {
        if (!phase2) {
            // Slam appears at slots 2 and 4 — twice as often as other attacks
            switch (attackPhase % 6) {
                case 0 -> { doToxinCloud(world);  attackTimer = 100; }
                case 1 -> { doSporeBurst(world);  attackTimer = 120; }
                case 2 -> { beginSlam(world);     attackTimer = 160; }
                case 3 -> { beginCharge(world);   attackTimer = 140; }
                case 4 -> { beginSlam(world);     attackTimer = 160; }
                case 5 -> { doToxinCloud(world);  attackTimer = 100; }
            }
        } else {
            switch (attackPhase % 4) {
                case 0 -> { doLightningStorm(world); attackTimer = 90;  }
                case 1 -> { doFrostNova(world);      attackTimer = 100; }
                case 2 -> { beginRadiantPulse(world);attackTimer = 150; }
                case 3 -> { doPlasmaVolley(world);   attackTimer = 80;  }
            }
        }
        attackPhase++;
    }

    // =========================================================================
    // Phase 1 attacks
    // =========================================================================

    private void doToxinCloud(ServerWorld world) {
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(12.0), p -> true)
            .forEach(player -> {
                player.damage(world.getDamageSources().mobAttack(this), 6.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,  120, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1));

                AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world,
                    player.getX(), player.getY(), player.getZ());
                cloud.setRadius(2.5f);
                cloud.setDuration(120);
                cloud.setRadiusGrowth(-cloud.getRadius() / cloud.getDuration());
                cloud.addEffect(new StatusEffectInstance(StatusEffects.POISON, 40, 1));
                cloud.addEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 0));
                world.spawnEntity(cloud);
            });

        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.5f);
        broadcastAttack(world, "☣ Toxin Release!", Formatting.GREEN);
    }

    private void doSporeBurst(ServerWorld world) {
        double cx = this.getX(), cy = this.getEyeY(), cz = this.getZ();
        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI / 12) * i;
            double dx = Math.cos(angle), dz = Math.sin(angle);
            WitherSkullEntity skull = new WitherSkullEntity(world, this, new Vec3d(dx, 0.05, dz));
            skull.setPos(cx + dx * 2, cy, cz + dz * 2);
            world.spawnEntity(skull);
        }
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0f, 0.7f);
        broadcastAttack(world, "☣ Spore Burst!", Formatting.DARK_GREEN);
    }

    private void beginSlam(ServerWorld world) {
        isSlamming = true;
        slamTick = 0;
        slamTargetY = this.getY();
        this.setVelocity(0, 1.8, 0);
        this.velocityDirty = true;
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 2.0f, 0.6f);
        broadcastAttack(world, "☣ Ground Slam!", Formatting.YELLOW);
    }

    private void tickSlam(ServerWorld world) {
        slamTick++;
        if (slamTick < 20) return;

        this.setVelocity(this.getVelocity().x, -3.5, this.getVelocity().z);
        this.velocityDirty = true;

        if (this.getY() <= slamTargetY + 1.0 && slamTick > 20) {
            isSlamming = false;
            world.getEntitiesByClass(PlayerEntity.class,
                    this.getBoundingBox().expand(10.0), p -> true)
                .forEach(player -> {
                    player.damage(world.getDamageSources().mobAttack(this), 12.0f);
                    player.setVelocity(player.getX() - this.getX(), 2.5, player.getZ() - this.getZ());
                    player.velocityDirty = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 3));
                    if (player instanceof ServerPlayerEntity sp) {
                        sp.networkHandler.sendPacket(new TitleFadeS2CPacket(2, 8, 2));
                        sp.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("").formatted(Formatting.RED)));
                        sp.networkHandler.sendPacket(new SubtitleS2CPacket(
                            Text.literal("SLAM!").formatted(Formatting.DARK_RED, Formatting.BOLD)));
                    }
                });
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY(), this.getZ(), 4, 2.0, 0.5, 2.0, 0.1);
            world.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 0.8f);
        }
    }

    private void beginCharge(ServerWorld world) {
        PlayerEntity target = world.getClosestPlayer(this, 48.0);
        if (target == null) return;
        isCharging = true;
        chargeTick = 0;
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.1) return;
        chargeDir = new Vec3d(dx / len * 2.2, 0, dz / len * 2.2);
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_RAVAGER_ATTACK, SoundCategory.HOSTILE, 2.0f, 0.8f);
        broadcastAttack(world, "☣ Charge!", Formatting.RED);
    }

    private void tickCharge(ServerWorld world) {
        chargeTick++;
        this.setVelocity(chargeDir.x, this.getVelocity().y, chargeDir.z);
        this.velocityDirty = true;
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(1.5), p -> true)
            .forEach(player -> {
                player.damage(world.getDamageSources().mobAttack(this), 10.0f);
                player.setVelocity(chargeDir.x * 1.5, 0.8, chargeDir.z * 1.5);
                player.velocityDirty = true;
            });
        if (chargeTick >= 25) {
            isCharging = false;
            this.setVelocity(0, this.getVelocity().y, 0);
        }
    }

    // =========================================================================
    // Phase 2 attacks — light-based
    // =========================================================================

    /** Calls 5 lightning bolts down near players */
    private void doLightningStorm(ServerWorld world) {
        for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(20.0), p -> true)) {
            for (int i = 0; i < 5; i++) {
                double offX = (Math.random() - 0.5) * 6;
                double offZ = (Math.random() - 0.5) * 6;
                BlockPos strikePos = BlockPos.ofFloored(player.getX() + offX, player.getY(), player.getZ() + offZ);
                LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                lightning.setPos(strikePos.getX() + 0.5, strikePos.getY(), strikePos.getZ() + 0.5);
                world.spawnEntity(lightning);
            }
        }
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 2.0f, 0.9f);
        broadcastAttack(world, "⚡ Lightning Storm!", Formatting.YELLOW);
    }

    /** Fires 20 snowballs in a ring that slow and blind */
    private void doFrostNova(ServerWorld world) {
        double cx = this.getX(), cy = this.getEyeY(), cz = this.getZ();
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI / 20) * i;
            double dx = Math.cos(angle), dz = Math.sin(angle);

            SnowballEntity snowball = new SnowballEntity(world, cx + dx * 2, cy, cz + dz * 2);
            snowball.setVelocity(dx * 1.5, 0.1, dz * 1.5);
            world.spawnEntity(snowball);
        }

        // Also hit nearby players with the chill effect directly
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(6.0), p -> true)
            .forEach(player -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,  100, 3));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,  40, 0));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 2));
            });

        world.spawnParticles(ParticleTypes.SNOWFLAKE,
            cx, cy, cz, 60, 3.0, 1.0, 3.0, 0.3);
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.HOSTILE, 2.0f, 0.6f);
        broadcastAttack(world, "❄ Frost Nova!", Formatting.AQUA);
    }

    /** Warning flash then heavy AoE damage */
    private void beginRadiantPulse(ServerWorld world) {
        // Warn all players
        for (PlayerEntity p : world.getPlayers()) {
            if (p instanceof ServerPlayerEntity sp) {
                sp.networkHandler.sendPacket(new TitleFadeS2CPacket(3, 25, 3));
                sp.networkHandler.sendPacket(new TitleS2CPacket(
                    Text.literal("⚠ RADIANT PULSE ⚠").formatted(Formatting.WHITE, Formatting.BOLD)));
                sp.networkHandler.sendPacket(new SubtitleS2CPacket(
                    Text.literal("BRACE YOURSELF!").formatted(Formatting.YELLOW)));
                sp.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 0));
            }
        }
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.HOSTILE, 2.0f, 0.5f);
        // Strike fires after the 30-tick warning
        radiantWarnTick = 30;
        broadcastAttack(world, "✦ Radiant Pulse incoming!", Formatting.WHITE);
    }

    private void doRadiantStrike(ServerWorld world) {
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(18.0), p -> true)
            .forEach(player -> {
                player.damage(world.getDamageSources().mobAttack(this), 16.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,  80, 2));
                player.setVelocity(
                    (player.getX() - this.getX()) * 0.5,
                    1.2,
                    (player.getZ() - this.getZ()) * 0.5);
                player.velocityDirty = true;
            });

        world.spawnParticles(ParticleTypes.END_ROD,
            this.getX(), this.getY() + 2, this.getZ(), 120, 6.0, 4.0, 6.0, 0.4);
        world.spawnParticles(ParticleTypes.FLASH,
            this.getX(), this.getY() + 1, this.getZ(), 5, 2.0, 2.0, 2.0, 0.0);
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.HOSTILE, 2.0f, 0.7f);
    }

    /** Fires 8 blue wither skulls directly at the target */
    private void doPlasmaVolley(ServerWorld world) {
        PlayerEntity target = world.getClosestPlayer(this, 48.0);
        if (target == null) return;

        double cx = this.getX(), cy = this.getEyeY(), cz = this.getZ();
        double tx = target.getX(), ty = target.getEyeY(), tz = target.getZ();
        double dx = tx - cx, dy = ty - cy, dz = tz - cz;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.1) return;
        dx /= len; dy /= len; dz /= len;

        for (int i = 0; i < 8; i++) {
            // Each shot has a small spread
            double spread = 0.15;
            double sx = dx + (Math.random() - 0.5) * spread;
            double sy = dy + (Math.random() - 0.5) * spread;
            double sz = dz + (Math.random() - 0.5) * spread;

            // Blue (charged) wither skull
            WitherSkullEntity skull = new WitherSkullEntity(world, this, new Vec3d(sx, sy, sz));
            skull.setPos(cx + dx, cy, cz + dz);
            skull.setCharged(true);
            world.spawnEntity(skull);
        }

        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0f, 1.3f);
        broadcastAttack(world, "⚡ Plasma Volley!", Formatting.BLUE);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void broadcastAttack(ServerWorld world, String msg, Formatting color) {
        for (PlayerEntity p : world.getPlayers()) {
            if (p instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal(msg).formatted(color, Formatting.BOLD), true);
            }
        }
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
