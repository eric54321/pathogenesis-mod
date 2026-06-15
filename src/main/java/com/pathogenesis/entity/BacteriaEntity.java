package com.pathogenesis.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Abstract base class for all bacterial enemies — mirrors VirusEntity but ground-based.
 * Subclasses override onHitEffect() to define pathogen-specific debuffs.
 */
public abstract class BacteriaEntity extends HostileEntity {

    protected BacteriaEntity(EntityType<? extends BacteriaEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    protected abstract void onHitEffect(LivingEntity target);

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            onHitEffect(living);
        }
        return hit;
    }
}
