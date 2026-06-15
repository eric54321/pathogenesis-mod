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
 * Streptococcus pyogenes — the punisher bacterium of Stage 1.
 *
 * Biology note: Strep forms chains (strepto = twisted chain in Greek) and secretes
 * toxins that directly damage tissue and suppress the local immune response.
 * Streptococcus pyogenes causes strep throat, impetigo, and if untreated,
 * rheumatic fever. The Weakness debuff represents immune suppression from streptolysins.
 *
 * Mechanics:
 *  - 14 HP, faster than Staph — rewards focused fire over AoE
 *  - On hit: Weakness I for 4 seconds (tissue toxins reduce player output)
 */
public class StreptococcusEntity extends BacteriaEntity {

    private static final double MAX_HEALTH    = 14.0;
    private static final double MOVE_SPEED    = 0.38;
    private static final double ATTACK_DAMAGE = 3.0;
    private static final double FOLLOW_RANGE  = 20.0;

    private static final int WEAKNESS_DURATION  = 80;  // 4 seconds
    private static final int WEAKNESS_AMPLIFIER = 0;   // Weakness I

    public StreptococcusEntity(EntityType<? extends StreptococcusEntity> type, World world) {
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
        target.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WEAKNESS, WEAKNESS_DURATION, WEAKNESS_AMPLIFIER, false, true
        ));
    }
}
