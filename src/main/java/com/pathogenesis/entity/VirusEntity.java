package com.pathogenesis.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/**
 * Abstract base class for all virus enemies in Pathogenesis.
 *
 * Biology note: All viruses share the same basic life cycle —
 * they find a host cell, attach, inject their genetic material,
 * and hijack the cell's machinery. Each subclass overrides
 * applyInfectionEffect() to represent a different virus's specific
 * method of attacking the host.
 *
 * Shared behavior all viruses inherit:
 *  - Flying movement (viruses travel through fluids and air in the body)
 *  - On hit, they apply a debuff (infection) as well as dealing damage
 *
 * To add a new virus:
 *   1. Create a class that extends VirusEntity
 *   2. Override applyInfectionEffect() with the virus's unique debuff
 *   3. Register it in ModEntities
 */
public abstract class VirusEntity extends HostileEntity {

    protected VirusEntity(EntityType<? extends VirusEntity> type, World world) {
        super(type, world);
        // Viruses float — no gravity
        this.setNoGravity(true);
    }

    /**
     * Replaces the default ground-based pathfinder with a flying navigator.
     * This lets the virus move freely in 3D space instead of walking on floors.
     */
    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanEnterOpenDoors(false);
        nav.setCanSwim(false);
        return nav;
    }

    /**
     * Each virus subclass defines what debuff it applies when it hits a player.
     * Called automatically by tryAttack() whenever an attack lands.
     *
     * @param target The living entity that was hit
     */
    protected abstract void applyInfectionEffect(LivingEntity target);

    /**
     * Overrides the default attack so every successful hit also triggers infection.
     * The base damage still applies — infection is an additional effect on top.
     */
    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            applyInfectionEffect(living);
        }
        return hit;
    }
}
