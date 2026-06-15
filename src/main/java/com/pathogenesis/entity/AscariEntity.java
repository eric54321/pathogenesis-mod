package com.pathogenesis.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Ascaris lumbricoides — the giant intestinal roundworm. GI tract entry route.
 *
 * Biology note: Ascaris is the largest intestinal parasite infecting humans —
 * adults can reach 35 cm. Eggs are swallowed in contaminated food or water,
 * hatch in the small intestine, and the larvae migrate through the blood to the
 * lungs before being coughed up and swallowed again. Heavy infestations cause
 * intestinal blockage and malnutrition. The in-game worm literally grows during
 * the fight, simulating the parasite feeding and maturing.
 *
 * Mechanics:
 *  - Starts at scale 1.0 (30 HP, 5 damage) — dangerous but manageable
 *  - Every 10 seconds: grows 0.5 scale, gains 15 HP, hits harder
 *  - Gets slower as it grows — massive but deliberate at full size
 *  - Max scale 3.5: 80+ HP, 7.5 damage — a genuine raid boss
 *  - Kill it fast or it becomes nearly unkillable
 */
public class AscariEntity extends HostileEntity {

    private static final double BASE_HEALTH    = 30.0;
    private static final double BASE_SPEED     = 0.30;
    private static final double BASE_DAMAGE    = 5.0;
    private static final double FOLLOW_RANGE   = 24.0;

    private static final int    GROWTH_INTERVAL   = 200;   // ticks between growth pulses (10 sec)
    private static final double SCALE_INCREMENT   = 0.5;
    private static final double MAX_SCALE         = 3.5;
    private static final double HEALTH_PER_GROWTH = 15.0;
    private static final double DAMAGE_PER_GROWTH = 0.5;
    private static final double SPEED_LOSS        = 0.02;  // slows as it grows — massive but lumbering

    public AscariEntity(EntityType<? extends AscariEntity> type, World world) {
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
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient() && this.age > 0 && this.age % GROWTH_INTERVAL == 0) {
            tryGrow();
        }
    }

    private void tryGrow() {
        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale == null || scale.getValue() >= MAX_SCALE) return;

        double newScale = Math.min(scale.getValue() + SCALE_INCREMENT, MAX_SCALE);
        scale.setBaseValue(newScale);

        EntityAttributeInstance health = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            double newMax = health.getValue() + HEALTH_PER_GROWTH;
            health.setBaseValue(newMax);
            setHealth(Math.min(getHealth() + (float) HEALTH_PER_GROWTH, (float) newMax));
        }

        EntityAttributeInstance damage = getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getValue() + DAMAGE_PER_GROWTH);
        }

        EntityAttributeInstance speed = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(Math.max(0.14, speed.getValue() - SPEED_LOSS));
        }
    }
}
