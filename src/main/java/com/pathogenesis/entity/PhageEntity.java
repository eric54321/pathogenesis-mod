package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/**
 * Bacteriophage — the alien spaceship virus.
 *
 * Biology note: Bacteriophages are viruses that infect bacteria, not human cells.
 * They have one of the most distinctive shapes in biology: an icosahedral protein
 * head filled with DNA, a hollow tail shaft, a base plate, and 6 tail fibers that
 * act like legs — anchoring the phage to a bacterial cell wall so it can inject
 * its DNA payload. In the game, the Phage represents a mutated phage that has
 * learned to target immune cells (players) instead of bacteria.
 *
 * Mechanics:
 *  - 8 HP — moderately tough
 *  - Slow and deliberate (0.25 speed) — stalks players like a real phage
 *  - High attack damage (4) — DNA injection is catastrophic
 *  - On hit: Poison II for 3 seconds (DNA corruption corrupts cell machinery)
 *  - Tiny swarm of 2 — phages are precision weapons, not swarm attackers
 *  - Appears in waves 4+ as a mid-to-late threat
 */
public class PhageEntity extends VironEntity {

    private static final double MAX_HEALTH    = 8.0;
    private static final double MOVE_SPEED    = 0.25;
    private static final double ATTACK_DAMAGE = 4.0;

    // Poison II for 3 seconds — DNA injection corrupts cell machinery
    private static final int POISON_DURATION  = 60;
    private static final int POISON_AMPLIFIER = 1;  // level II

    public PhageEntity(EntityType<? extends PhageEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,     MAX_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  ATTACK_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   24.0);
    }

    /**
     * DNA injection: Poison represents the phage hijacking the cell's machinery.
     * Real biology: a phage latches onto a receptor, punches a hole in the membrane,
     * and injects its DNA — the cell then produces viral copies instead of itself.
     */
    @Override
    protected void applyInfectionEffect(LivingEntity target) {
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.POISON, POISON_DURATION, POISON_AMPLIFIER, false, true)
        );
    }

    /** Phages are precision weapons — tiny swarm of just 1 follower. */
    @Override
    protected int getSwarmSize() {
        return 1;
    }

    @Override
    protected VironEntity createFollower(World world) {
        return new PhageEntity(ModEntities.PHAGE, world);
    }
}
