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
 * Coronavirus — a named virus subclass extending VironEntity.
 *
 * Biology note: Coronaviruses (including SARS-CoV-2) are large enveloped RNA
 * viruses named for their crown-like (corona) spike proteins. They primarily
 * attack the respiratory system, causing shortness of breath, fatigue, and
 * in severe cases, oxygen deprivation. Their large size (120-160nm, vs
 * influenza's 80-120nm) makes them slower-moving but harder to clear.
 *
 * Differences from InfluenzaEntity:
 *  - More HP (10 vs 8) — larger, harder to destroy
 *  - Slower movement (0.28 vs 0.35) — bigger viral envelope
 *  - Smaller swarms (3 vs 5) — replicates more slowly than flu
 *  - Infection: Blindness + Weakness (mimics respiratory/oxygen symptoms)
 *  - Bigger hitbox (0.7 vs 0.5) — reflects larger physical size
 *
 * TODO: Replace with a custom crown-shaped Blockbench model (influenza.bbmodel
 * as starting point — add more spikes arranged in a crown pattern).
 */
public class CoronavirusEntity extends VironEntity {

    private static final double MAX_HEALTH    = 10.0;
    private static final double MOVE_SPEED    = 0.28;
    private static final double ATTACK_DAMAGE = 3.0;

    // Respiratory symptoms: Blindness (O2 deprivation) + Weakness
    private static final int BLINDNESS_DURATION = 60;   // 3 seconds
    private static final int WEAKNESS_DURATION  = 160;  // 8 seconds
    private static final int WEAKNESS_AMPLIFIER = 0;    // level I

    public CoronavirusEntity(EntityType<? extends CoronavirusEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected VironEntity createFollower(World world) {
        return new CoronavirusEntity(ModEntities.CORONAVIRUS, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,    MAX_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  ATTACK_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   22.0);
    }

    /**
     * Corona infection: Blindness represents oxygen deprivation from
     * damaged lung tissue. Weakness represents systemic immune exhaustion.
     * Real biology: SARS-CoV-2 binds ACE2 receptors in the lungs,
     * causing inflammation that reduces oxygen exchange.
     */
    @Override
    protected void applyInfectionEffect(LivingEntity target) {
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.BLINDNESS, BLINDNESS_DURATION, 0, false, true)
        );
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.WEAKNESS, WEAKNESS_DURATION, WEAKNESS_AMPLIFIER, false, true)
        );
    }

    /**
     * Coronaviruses replicate more slowly — smaller swarm of 3 instead of 5.
     * Overrides the swarm size constant from VironEntity.
     */
    @Override
    protected int getSwarmSize() {
        return 2;  // 1 leader + 2 followers = 3 total
    }
}
