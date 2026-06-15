package com.pathogenesis.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Dermatophyte — the zone-control fungus of Stage 1.
 *
 * Biology note: Dermatophytes (Trichophyton, Microsporum, Epidermophyton) infect the
 * outer skin, hair, and nails. They secrete keratinase enzymes that dissolve keratin,
 * and their spores persist in the environment long after the fungus is eliminated —
 * the classic case being athlete's foot returning from a contaminated shower floor.
 * That environmental persistence is the in-game spore cloud mechanic.
 *
 * Mechanics:
 *  - 16 HP, slow movement — deliberate zone-control threat
 *  - On death: releases a 2.5-block radius spore cloud (Slowness II + Nausea, 5 sec)
 *  - Players who rush the kill zone get debuffed — position carefully
 */
public class DermatophyteEntity extends HostileEntity {

    private static final double MAX_HEALTH    = 16.0;
    private static final double MOVE_SPEED    = 0.22;
    private static final double ATTACK_DAMAGE = 2.5;
    private static final double FOLLOW_RANGE  = 14.0;

    private static final float CLOUD_RADIUS          = 2.5f;
    private static final int   CLOUD_DURATION_TICKS  = 200;  // 10 seconds
    private static final int   DEBUFF_DURATION_TICKS = 100;  // 5 seconds

    public DermatophyteEntity(EntityType<? extends DermatophyteEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,     MAX_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  ATTACK_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   FOLLOW_RANGE);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient()) {
            spawnSporeCloud((ServerWorld) this.getWorld());
        }
    }

    private void spawnSporeCloud(ServerWorld world) {
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(
            world, this.getX(), this.getY(), this.getZ()
        );
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setDuration(CLOUD_DURATION_TICKS);
        cloud.addEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, DEBUFF_DURATION_TICKS, 1));
        cloud.addEffect(new StatusEffectInstance(StatusEffects.NAUSEA,   DEBUFF_DURATION_TICKS, 0));
        world.spawnEntity(cloud);
    }
}
