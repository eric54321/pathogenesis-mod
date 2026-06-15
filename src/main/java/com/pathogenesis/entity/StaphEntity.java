package com.pathogenesis.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/**
 * Staphylococcus aureus — the swarm bacterium of Stage 1.
 *
 * Biology note: Staph is a spherical (coccus) bacterium that colonizes skin surfaces.
 * It thrives in grape-like clusters and is the primary cause of wound infections and
 * skin abscesses. In large numbers it overwhelms local immune defenses before a
 * systemic response can be mounted.
 *
 * Mechanics:
 *  - 8 HP, moderate speed — fragile but dangerous in numbers
 *  - No special debuff: pure swarm pressure forces players to use AoE tactics
 *  - WaveSpawner spawns these in clusters of 3 (see WaveSpawner)
 */
public class StaphEntity extends BacteriaEntity {

    private static final double MAX_HEALTH    = 8.0;
    private static final double MOVE_SPEED    = 0.35;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final double FOLLOW_RANGE  = 16.0;

    public StaphEntity(EntityType<? extends StaphEntity> type, World world) {
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
    protected void onHitEffect(LivingEntity target) {
        // Staph relies on numbers alone — no debuff
    }
}
