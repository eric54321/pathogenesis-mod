package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Strongyloides stercoralis — the threadworm. GI tract entry route.
 *
 * Biology note: Strongyloides is unique among human parasites for its ability to
 * complete its entire life cycle inside one host — a process called autoinfection.
 * Larvae hatch in the gut, penetrate the intestinal wall, travel through the blood
 * to the lungs, are coughed up, and swallowed again to mature into adults.
 * If left untreated in an immunocompromised host, this cycle repeats until the
 * parasite population becomes overwhelming (hyperinfection). The in-game mechanic
 * directly mirrors this: the adult spawns larvae, and both the adult and its larvae
 * grow over time. Kill the adult first or the numbers become unmanageable.
 *
 * Mechanics:
 *  - Adults: 20 HP, scale 1.0, fast (0.42 speed)
 *  - Every 15 seconds adults spawn one larva (max 4 active larvae tracked by proximity)
 *  - Adults grow every 12 seconds: +0.3 scale, +10 HP (max scale 2.5)
 *  - Larvae: 8 HP, scale 0.45, faster (0.55 speed), do NOT spawn more larvae
 *  - Larvae also grow slowly: +0.15 scale every 20 seconds (max scale 0.9)
 */
public class StrongyloideEntity extends HostileEntity {

    // Adult stats
    private static final double ADULT_HEALTH  = 20.0;
    private static final double ADULT_SPEED   = 0.42;
    private static final double ADULT_DAMAGE  = 3.5;
    private static final double FOLLOW_RANGE  = 22.0;

    // Larva stats
    private static final double LARVA_HEALTH  = 8.0;
    private static final double LARVA_SCALE   = 0.45;
    private static final double LARVA_SPEED   = 0.55;
    private static final double LARVA_DAMAGE  = 2.0;

    // Adult growth: every 12 seconds
    private static final int    ADULT_GROWTH_INTERVAL  = 240;
    private static final double ADULT_SCALE_INCREMENT  = 0.3;
    private static final double ADULT_MAX_SCALE        = 2.5;
    private static final double ADULT_HEALTH_INCREMENT = 10.0;

    // Larva growth: every 20 seconds (slower)
    private static final int    LARVA_GROWTH_INTERVAL  = 400;
    private static final double LARVA_SCALE_INCREMENT  = 0.15;
    private static final double LARVA_MAX_SCALE        = 0.9;

    // Larvae spawn: every 15 seconds, up to 4 nearby
    private static final int    LARVA_SPAWN_INTERVAL   = 300;
    private static final int    MAX_LARVAE_NEARBY      = 4;
    private static final double LARVA_CHECK_RADIUS     = 20.0;

    private boolean isLarva = false;

    public StrongyloideEntity(EntityType<? extends StrongyloideEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,     ADULT_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, ADULT_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  ADULT_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   FOLLOW_RANGE);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /** Called immediately after spawning to configure larva stats. */
    public void markAsLarva() {
        this.isLarva = true;

        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale != null) scale.setBaseValue(LARVA_SCALE);

        EntityAttributeInstance health = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(LARVA_HEALTH);
            setHealth((float) LARVA_HEALTH);
        }

        EntityAttributeInstance speed = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(LARVA_SPEED);

        EntityAttributeInstance damage = getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(LARVA_DAMAGE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient() || this.age <= 0) return;

        if (isLarva) {
            if (this.age % LARVA_GROWTH_INTERVAL == 0) tryGrowLarva();
        } else {
            if (this.age % ADULT_GROWTH_INTERVAL == 0) tryGrowAdult();
            if (this.age % LARVA_SPAWN_INTERVAL  == 0) trySpawnLarva();
        }
    }

    private void tryGrowAdult() {
        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale == null || scale.getValue() >= ADULT_MAX_SCALE) return;

        scale.setBaseValue(Math.min(scale.getValue() + ADULT_SCALE_INCREMENT, ADULT_MAX_SCALE));

        EntityAttributeInstance health = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            double newMax = health.getValue() + ADULT_HEALTH_INCREMENT;
            health.setBaseValue(newMax);
            setHealth(Math.min(getHealth() + (float) ADULT_HEALTH_INCREMENT, (float) newMax));
        }
    }

    private void tryGrowLarva() {
        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale == null || scale.getValue() >= LARVA_MAX_SCALE) return;
        scale.setBaseValue(Math.min(scale.getValue() + LARVA_SCALE_INCREMENT, LARVA_MAX_SCALE));
    }

    private void trySpawnLarva() {
        ServerWorld world = (ServerWorld) this.getWorld();

        // Count larvae already nearby — cap autoinfection to prevent cascade
        long nearbyLarvae = world.getEntitiesByType(
            ModEntities.STRONGYLOIDE,
            this.getBoundingBox().expand(LARVA_CHECK_RADIUS),
            e -> e instanceof StrongyloideEntity s && s.isLarva
        ).size();

        if (nearbyLarvae >= MAX_LARVAE_NEARBY) return;

        double angle = this.getWorld().getRandom().nextFloat() * 2.0 * Math.PI;
        double dist  = 1.5 + this.getWorld().getRandom().nextFloat() * 2.0;

        StrongyloideEntity larva = new StrongyloideEntity(ModEntities.STRONGYLOIDE, world);
        larva.markAsLarva();
        larva.refreshPositionAndAngles(
            this.getX() + Math.cos(angle) * dist,
            this.getY(),
            this.getZ() + Math.sin(angle) * dist,
            this.getYaw(), 0
        );
        world.spawnEntity(larva);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsLarva", this.isLarva);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.isLarva = nbt.getBoolean("IsLarva");
    }
}
