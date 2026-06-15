package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Viron — the first and simplest virus enemy.
 *
 * Biology note: Virions are individual viral particles — the form a virus takes
 * when it is outside a host cell, traveling through the body looking for a new
 * cell to infect. They are tiny, fast, and travel in groups because viruses
 * replicate in huge numbers. When they find a cell (player), they inject their
 * payload and weaken it — represented here as a Weakness debuff.
 *
 * Mechanics:
 *  - 6 HP — very fragile, must be killed quickly in a swarm
 *  - Fast movement (0.45 speed), flies through the air
 *  - Spawns in swarms of 5 (1 leader + 4 followers)
 *  - On hit: applies Weakness II for 5 seconds (the "infection" debuff)
 *  - Followers do NOT spawn their own swarms (prevents exponential explosions)
 *
 * To create a new virus, copy this file and extend VirusEntity.
 * Override applyInfectionEffect() with the new debuff.
 */
public class VironEntity extends VirusEntity {

    private static final double MAX_HEALTH    = 6.0;
    private static final double MOVE_SPEED    = 0.45;   // noticeably faster than player sprint
    private static final double ATTACK_DAMAGE = 2.0;    // 1 heart
    private static final double FOLLOW_RANGE  = 20.0;

    // How many extra Virons spawn alongside the "leader" Viron — subclasses can override
    private static final int SWARM_FOLLOWERS = 4;

    // Infection: Weakness II lasts 5 seconds (100 ticks)
    private static final int INFECTION_DURATION = 100;
    private static final int INFECTION_AMPLIFIER = 1;   // amplifier 1 = level II

    // Swarm followers set this to true so they don't spawn their own swarms
    private boolean isSwarmMember = false;

    public VironEntity(EntityType<? extends VironEntity> type, World world) {
        super(type, world);
    }

    /**
     * Base stats for all Virons.
     * Fast but fragile — players should feel urgency to kill them quickly.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,   MAX_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, ATTACK_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, FOLLOW_RANGE);
    }

    /**
     * AI goals — Virons are aggressive and prioritize reaching the player fast.
     * Higher speed means we use a slightly higher chase multiplier (1.5).
     */
    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.5, false));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /**
     * The infection mechanic — applies Weakness on every successful hit.
     * Real biology: viruses weaken cells by hijacking their protein machinery,
     * leaving the cell less able to defend itself or function normally.
     */
    @Override
    protected void applyInfectionEffect(LivingEntity target) {
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.WEAKNESS, INFECTION_DURATION, INFECTION_AMPLIFIER, false, true)
        );
    }

    /**
     * Called once when this entity first appears in the world.
     * If this is a swarm leader (not already a member), it spawns SWARM_FOLLOWERS
     * extra Virons nearby — simulating a viral burst entering the bloodstream.
     */
    @Override
    public @Nullable EntityData initialize(
        ServerWorldAccess world,
        LocalDifficulty difficulty,
        SpawnReason spawnReason,
        @Nullable EntityData entityData
    ) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);

        if (!isSwarmMember) {
            spawnSwarm(world);
        }

        return data;
    }

    /**
     * Returns how many followers spawn with this virus.
     * Subclasses override this to change swarm size.
     */
    protected int getSwarmSize() {
        return SWARM_FOLLOWERS;
    }

    /**
     * Spawns getSwarmSize() additional copies in a ring around this one.
     * Each follower is marked as a swarm member so it does not spawn its own swarm.
     */
    private void spawnSwarm(ServerWorldAccess world) {
        int count = getSwarmSize();
        for (int i = 0; i < count; i++) {
            VironEntity follower = new VironEntity(ModEntities.VIRON, world.toServerWorld());

            // Spread followers evenly in a circle
            double angle = (2 * Math.PI / count) * i;
            double offsetX = Math.cos(angle) * 2.0;
            double offsetZ = Math.sin(angle) * 2.0;
            double offsetY = (Math.random() - 0.5) * 1.5;  // slight vertical scatter

            follower.refreshPositionAndAngles(
                this.getX() + offsetX,
                this.getY() + offsetY,
                this.getZ() + offsetZ,
                this.getYaw(),
                this.getPitch()
            );

            // Mark as member so it doesn't trigger another swarm spawn
            follower.isSwarmMember = true;
            world.toServerWorld().spawnEntity(follower);
        }
    }

    // -------------------------------------------------------------------------
    // NBT — save/load isSwarmMember so behaviour survives server restarts
    // -------------------------------------------------------------------------

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsSwarmMember", this.isSwarmMember);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.isSwarmMember = nbt.getBoolean("IsSwarmMember");
    }
}
