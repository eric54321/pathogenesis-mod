package com.pathogenesis.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/**
 * Influenza — a specific virus enemy extending VironEntity.
 *
 * Biology note: Influenza (the flu) is an enveloped RNA virus with
 * hemagglutinin (HA) and neuraminidase (NA) spike proteins on its surface.
 * It targets the respiratory system, causing fever, nausea, and weakness.
 * The flu spreads in swarms (like all Virons) because infected cells
 * release thousands of new viral particles at once.
 *
 * Compared to the base VironEntity:
 *  - Slightly more HP (8 vs 6) — flu is harder to clear than a generic virus
 *  - Slower (0.35 vs 0.45) — flu virions are larger due to their envelope
 *  - Infection effect: Nausea + Slowness (mimics flu symptoms) instead of Weakness
 *  - Swarm size inherited from VironEntity (5 per spawn)
 *
 * To add another named virus, extend VironEntity and override
 * createAttributes() and applyInfectionEffect().
 */
public class InfluenzaEntity extends VironEntity {

    // Flu-specific stats
    private static final double MAX_HEALTH    = 8.0;
    private static final double MOVE_SPEED    = 0.35;
    private static final double ATTACK_DAMAGE = 2.0;

    // Flu symptoms: Nausea for 4 seconds + Slowness II for 6 seconds
    private static final int NAUSEA_DURATION    = 80;
    private static final int SLOWNESS_DURATION  = 120;
    private static final int SLOWNESS_AMPLIFIER = 1;  // level II

    public InfluenzaEntity(EntityType<? extends InfluenzaEntity> type, World world) {
        super(type, world);
    }

    /**
     * Overrides Viron base stats with flu-specific values.
     * Larger and slower than a generic virion due to the viral envelope.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,    MAX_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,  ATTACK_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,   20.0);
    }

    /**
     * Flu infection: causes Nausea and Slowness instead of Weakness.
     * Real biology: influenza triggers an immune response that causes
     * fever, fatigue, and nausea — the body fighting the infection
     * makes you feel worse than the virus itself.
     */
    @Override
    protected void applyInfectionEffect(LivingEntity target) {
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.NAUSEA, NAUSEA_DURATION, 0, false, true)
        );
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.SLOWNESS, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, true)
        );
    }
}
