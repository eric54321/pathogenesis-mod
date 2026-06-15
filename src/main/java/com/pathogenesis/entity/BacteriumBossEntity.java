package com.pathogenesis.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
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
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Bacterium Boss — Bacillus anthracis, the final boss of the wave system.
 *
 * Attacks (rotating cycle):
 *  1. Toxin Cloud  — AoE stomp + lingering poison cloud under each player
 *  2. Spore Burst  — 12 wither skulls fired in a ring at head height
 *  3. Slam         — leaps up, crashes down with massive knockback + screen shake
 *  4. Charge       — rockets toward nearest player at high speed
 */
public class BacteriumBossEntity extends HostileEntity {

    private final ServerBossBar bossBar = new ServerBossBar(
        Text.literal("Bacillus Anthracis").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
        BossBar.Color.GREEN,
        BossBar.Style.NOTCHED_10
    );

    // Attack cycle: 0=toxin, 1=spore, 2=slam, 3=charge
    private int attackPhase = 0;
    private int attackTimer = 80;

    // Slam state
    private boolean isSlamming = false;
    private int slamTick = 0;
    private double slamTargetY = 0;

    // Charge state
    private boolean isCharging = false;
    private int chargeTick = 0;
    private Vec3d chargeDir = Vec3d.ZERO;

    public BacteriumBossEntity(EntityType<? extends BacteriumBossEntity> type, World world) {
        super(type, world);
    }

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

        bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Handle slam mid-air
        if (isSlamming) {
            tickSlam();
            return;
        }

        // Handle charge dash
        if (isCharging) {
            tickCharge();
            return;
        }

        if (--attackTimer <= 0) {
            triggerNextAttack();
        }
    }

    private void triggerNextAttack() {
        switch (attackPhase) {
            case 0 -> { doToxinCloud();  attackTimer = 100; }
            case 1 -> { doSporeBurst();  attackTimer = 120; }
            case 2 -> { beginSlam();     attackTimer = 180; }
            case 3 -> { beginCharge();   attackTimer = 140; }
        }
        attackPhase = (attackPhase + 1) % 4;
    }

    // -------------------------------------------------------------------------
    // Attack 1: Toxin Cloud — AoE damage + lingering cloud under each player
    // -------------------------------------------------------------------------
    private void doToxinCloud() {
        ServerWorld world = (ServerWorld) this.getWorld();

        // Hurt all nearby players and leave a cloud under them
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(12.0), p -> true)
            .forEach(player -> {
                player.damage(world.getDamageSources().mobAttack(this), 6.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,  120, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1));

                // Spawn lingering cloud under the player
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

    // -------------------------------------------------------------------------
    // Attack 2: Spore Burst — 12 wither skulls fired outward in a ring
    // -------------------------------------------------------------------------
    private void doSporeBurst() {
        ServerWorld world = (ServerWorld) this.getWorld();

        double cx = this.getX();
        double cy = this.getEyeY();
        double cz = this.getZ();

        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI / 12) * i;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            WitherSkullEntity skull = new WitherSkullEntity(world, this, new Vec3d(dx, 0.05, dz));
            skull.setPos(cx + dx * 2, cy, cz + dz * 2);
            world.spawnEntity(skull);
        }

        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0f, 0.7f);

        broadcastAttack(world, "☣ Spore Burst!", Formatting.DARK_GREEN);
    }

    // -------------------------------------------------------------------------
    // Attack 3: Slam — leaps up then crashes down
    // -------------------------------------------------------------------------
    private void beginSlam() {
        isSlamming = true;
        slamTick = 0;
        slamTargetY = this.getY();
        // Launch upward
        this.setVelocity(0, 1.8, 0);
        this.velocityDirty = true;

        ServerWorld world = (ServerWorld) this.getWorld();
        broadcastAttack(world, "☣ Ground Slam!", Formatting.YELLOW);
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 2.0f, 0.6f);
    }

    private void tickSlam() {
        slamTick++;
        ServerWorld world = (ServerWorld) this.getWorld();

        if (slamTick < 20) {
            // Still rising — let gravity do its thing
            return;
        }

        // Crash down — override gravity with fast downward velocity
        this.setVelocity(this.getVelocity().x, -3.5, this.getVelocity().z);
        this.velocityDirty = true;

        // Check if we've landed (close to target Y)
        if (this.getY() <= slamTargetY + 1.0 && slamTick > 20) {
            isSlamming = false;
            // Impact
            world.getEntitiesByClass(PlayerEntity.class,
                    this.getBoundingBox().expand(10.0), p -> true)
                .forEach(player -> {
                    player.damage(world.getDamageSources().mobAttack(this), 12.0f);
                    // Massive upward knockback
                    player.setVelocity(
                        player.getX() - this.getX(),
                        2.5,
                        player.getZ() - this.getZ()
                    );
                    player.velocityDirty = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 3));

                    // Screen shake via title flash
                    if (player instanceof ServerPlayerEntity sp) {
                        sp.networkHandler.sendPacket(new TitleFadeS2CPacket(2, 8, 2));
                        sp.networkHandler.sendPacket(new TitleS2CPacket(
                            Text.literal("").formatted(Formatting.RED)));
                        sp.networkHandler.sendPacket(new SubtitleS2CPacket(
                            Text.literal("SLAM!").formatted(Formatting.DARK_RED, Formatting.BOLD)));
                    }
                });

            // Spawn explosion particles
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY(), this.getZ(), 4, 2.0, 0.5, 2.0, 0.1);

            world.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 0.8f);
        }
    }

    // -------------------------------------------------------------------------
    // Attack 4: Charge — rockets toward nearest player
    // -------------------------------------------------------------------------
    private void beginCharge() {
        PlayerEntity target = this.getWorld().getClosestPlayer(this, 48.0);
        if (target == null) return;

        isCharging = true;
        chargeTick = 0;

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.1) return;
        chargeDir = new Vec3d(dx / len * 2.2, 0, dz / len * 2.2);

        ServerWorld world = (ServerWorld) this.getWorld();
        broadcastAttack(world, "☣ Charge!", Formatting.RED);
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENTITY_RAVAGER_ATTACK, SoundCategory.HOSTILE, 2.0f, 0.8f);
    }

    private void tickCharge() {
        chargeTick++;
        this.setVelocity(chargeDir.x, this.getVelocity().y, chargeDir.z);
        this.velocityDirty = true;

        // Hurt players we run into
        ServerWorld world = (ServerWorld) this.getWorld();
        world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(1.5), p -> true)
            .forEach(player -> {
                player.damage(world.getDamageSources().mobAttack(this), 10.0f);
                // Knock them sideways
                player.setVelocity(chargeDir.x * 1.5, 0.8, chargeDir.z * 1.5);
                player.velocityDirty = true;
            });

        if (chargeTick >= 25) {
            isCharging = false;
            this.setVelocity(0, this.getVelocity().y, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
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
