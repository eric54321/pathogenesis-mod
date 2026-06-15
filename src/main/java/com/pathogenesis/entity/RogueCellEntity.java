package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * RogueCell — a mid-game cancer cell enemy.
 *
 * Biology note: Real cancer cells can divide rapidly and resist being killed.
 * When you destroy one, two weaker copies spawn in its place — just like
 * cancer cells that survive treatment can keep dividing.
 *
 * Mechanics:
 *  - 20 HP, glowing red so players can spot it easily
 *  - On death, spawns 2 split copies (each with 10 HP) if it is not already a split copy
 *  - Split copies do NOT split again (prevents infinite loop)
 */
public class RogueCellEntity extends HostileEntity {

    // Full-size Rogue Cell has 20 HP
    private static final double FULL_HEALTH = 50.0;

    // Split copies get half the health — they are weaker but still dangerous
    private static final double SPLIT_HEALTH = 25.0;

    // Number of ticks between glowing effect refreshes (100 ticks = 5 seconds)
    private static final int GLOW_REFRESH_INTERVAL = 100;

    // Tracks whether this is a split copy so it does not split again
    private boolean isSplitCopy = false;

    /**
     * Constructor called by Minecraft when spawning this entity.
     * EntityType tells the game what kind of entity this is.
     * World is the dimension (overworld, nether, etc.) it lives in.
     */
    public RogueCellEntity(EntityType<? extends RogueCellEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    private static class LashGoal extends Goal {
        private final RogueCellEntity mob;
        private int timer = 60;

        LashGoal(RogueCellEntity mob) { this.mob = mob; }

        @Override
        public boolean canStart() { return true; }

        @Override
        public boolean shouldContinue() { return true; }

        @Override
        public void tick() {
            if (--timer <= 0) {
                timer = 60;
                if (!mob.getWorld().isClient()) {
                    // Grab all players within 5 blocks and pull them in + apply effects
                    mob.getWorld().getEntitiesByClass(PlayerEntity.class,
                            mob.getBoundingBox().expand(5.0), p -> true)
                        .forEach(player -> {
                            player.damage(mob.getWorld().getDamageSources().mobAttack(mob), 6.0f);
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
                            // Pull player toward the mob
                            double dx = mob.getX() - player.getX();
                            double dy = mob.getY() - player.getY();
                            double dz = mob.getZ() - player.getZ();
                            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                            if (dist > 0.1) {
                                player.setVelocity(player.getVelocity().add(
                                    dx / dist * 0.6, dy / dist * 0.3, dz / dist * 0.6));
                            }
                        });
                }
            }
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanEnterOpenDoors(false);
        nav.setCanSwim(false);
        return nav;
    }

    /**
     * Defines the base stats for every RogueCell.
     * Fabric calls this when the entity type is registered so the game
     * has default values before any individual entity is spawned.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, FULL_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_ARMOR, 10.0)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 4.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8);
    }

    /**
     * Registers the AI goals that decide what this mob does each tick.
     * Goals are added in priority order — lower number = higher priority.
     */
    @Override
    protected void initGoals() {
        // Priority 1: Lash tentacles every 3 seconds, grabbing players within 5 blocks
        this.goalSelector.add(1, new LashGoal(this));

        // Priority 2: Wander around if it has no target
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));

        // Priority 3: Stare at nearby players (creepy effect)
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

        // Priority 4: Randomly look around when idle
        this.goalSelector.add(4, new LookAroundGoal(this));

        // Target selector: lock onto the nearest player within follow range
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /**
     * Called every game tick (20 times per second).
     * We use this to keep the Glowing effect refreshed so the mob always glows red.
     */
    @Override
    public void tick() {
        super.tick();

        // Gentle hovering bob — sine wave on Y velocity
        double bobVelocity = Math.sin(this.age * 0.05) * 0.03;
        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x, bobVelocity, vel.z);

        // Only refresh the glow on the server (not in the client's rendering thread)
        if (!this.getWorld().isClient() && this.age % GLOW_REFRESH_INTERVAL == 0) {
            this.addStatusEffect(
                new StatusEffectInstance(StatusEffects.GLOWING, GLOW_REFRESH_INTERVAL + 20, 0, false, false)
            );
        }
    }

    /**
     * Called when this entity's health reaches zero.
     * This is where the splitting mechanic happens:
     * If this is NOT a split copy, spawn two weaker split copies before dying.
     */
    @Override
    public void onDeath(DamageSource source) {
        // Must call super first so the normal death logic runs (drops, XP, etc.)
        super.onDeath(source);

        // Splitting only happens on the server — client has no authority over spawning
        if (!this.getWorld().isClient() && !isSplitCopy) {
            spawnSplitCopies((ServerWorld) this.getWorld());
        }
    }

    /**
     * Spawns two weaker RogueCell copies offset left and right from the death location.
     * Each copy is marked as a split copy so it cannot split further.
     */
    private void spawnSplitCopies(ServerWorld world) {
        for (int i = 0; i < 2; i++) {
            // Offset: first copy goes left (-0.6), second goes right (+0.6)
            double offsetX = (i == 0) ? -0.6 : 0.6;

            RogueCellEntity splitCell = new RogueCellEntity(ModEntities.ROGUE_CELL, world);

            // Place the split copy near the parent's death position
            splitCell.refreshPositionAndAngles(
                this.getX() + offsetX,
                this.getY(),
                this.getZ(),
                this.getYaw(),
                this.getPitch()
            );

            // Mark it so it cannot split again — prevents infinite cancer explosion
            splitCell.markAsSplitCopy();

            world.spawnEntity(splitCell);
        }
    }

    /**
     * Configures this entity as a non-splitting split copy and reduces its max HP.
     * Called immediately after spawning a split copy.
     */
    public void markAsSplitCopy() {
        this.isSplitCopy = true;

        // Lower the max health and set current health to match
        if (this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(SPLIT_HEALTH);
        }
        this.setHealth((float) SPLIT_HEALTH);
    }

    // -------------------------------------------------------------------------
    // NBT serialization — saves isSplitCopy to disk so it survives server restarts
    // -------------------------------------------------------------------------

    /**
     * Writes custom data to the save file when this entity is saved to disk.
     * Without this, isSplitCopy resets to false on every server reload.
     */
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsSplitCopy", this.isSplitCopy);
    }

    /**
     * Reads custom data back from the save file when this entity is loaded.
     */
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.isSplitCopy = nbt.getBoolean("IsSplitCopy");
    }
}
