package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Taenia solium — the pork tapeworm. GI tract entry route.
 *
 * Biology note: Tapeworms consist of a scolex (head with hooks) and a chain of
 * proglottid segments, each capable of producing eggs. A single worm can grow to
 * 7 metres and live for decades. As it matures it adds segments from the neck
 * region — in-game this is the "growing" mechanic. When a tapeworm is disturbed
 * or dies, shed proglottids become independent infection sources, represented by
 * spawning segment sub-entities on death.
 *
 * Mechanics:
 *  - Starts with 1 segment (25 HP, scale 1.0)
 *  - Every 15 seconds: adds a segment — +12 HP, +0.15 scale (max 8 segments)
 *  - At full growth: ~121 HP, scale 2.2 — a colossal ribbon of flesh
 *  - On death (if not already a segment): sheds up to 3 smaller segment entities
 *  - Segments: lower HP, fixed scale, do not grow or shed further
 */
public class TaeniaEntity extends HostileEntity {

    private static final double BASE_HEALTH      = 25.0;
    private static final double BASE_SPEED       = 0.26;
    private static final double BASE_DAMAGE      = 4.0;
    private static final double FOLLOW_RANGE     = 20.0;

    private static final int    SEGMENT_INTERVAL  = 300;   // 15 seconds
    private static final int    MAX_SEGMENTS      = 8;
    private static final double HEALTH_PER_SEGMENT = 12.0;
    private static final double SCALE_PER_SEGMENT  = 0.15;

    private int     segmentCount = 1;
    private boolean isSegment    = false;

    public TaeniaEntity(EntityType<? extends TaeniaEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,     BASE_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, BASE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  BASE_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   FOLLOW_RANGE);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient() && !isSegment
                && this.age > 0 && this.age % SEGMENT_INTERVAL == 0
                && segmentCount < MAX_SEGMENTS) {
            addSegment();
        }
    }

    private void addSegment() {
        segmentCount++;

        EntityAttributeInstance health = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            double newMax = health.getValue() + HEALTH_PER_SEGMENT;
            health.setBaseValue(newMax);
            setHealth(Math.min(getHealth() + (float) HEALTH_PER_SEGMENT, (float) newMax));
        }

        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale != null) {
            scale.setBaseValue(scale.getValue() + SCALE_PER_SEGMENT);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient() && !isSegment && segmentCount >= 2) {
            shedSegments((ServerWorld) this.getWorld());
        }
    }

    private void shedSegments(ServerWorld world) {
        int toShed = Math.min(segmentCount / 2, 3);
        int inheritedSegments = segmentCount / 2;

        for (int i = 0; i < toShed; i++) {
            TaeniaEntity seg = new TaeniaEntity(ModEntities.TAENIA, world);
            seg.markAsSegment(inheritedSegments);

            double angle = (2 * Math.PI / toShed) * i;
            seg.refreshPositionAndAngles(
                this.getX() + Math.cos(angle) * 2.0,
                this.getY(),
                this.getZ() + Math.sin(angle) * 2.0,
                this.getYaw(), 0
            );
            world.spawnEntity(seg);
        }
    }

    /** Called immediately after spawning a shed segment to set its inherited stats. */
    public void markAsSegment(int inherited) {
        this.isSegment    = true;
        this.segmentCount = inherited;

        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale != null) {
            scale.setBaseValue(0.5 + inherited * SCALE_PER_SEGMENT);
        }

        EntityAttributeInstance health = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            double hp = 12.0 + inherited * HEALTH_PER_SEGMENT;
            health.setBaseValue(hp);
            setHealth((float) hp);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SegmentCount", this.segmentCount);
        nbt.putBoolean("IsSegment", this.isSegment);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.segmentCount = nbt.getInt("SegmentCount");
        this.isSegment    = nbt.getBoolean("IsSegment");
    }
}
