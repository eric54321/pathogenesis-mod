package com.pathogenesis.entity;

import com.pathogenesis.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TentacleProjectile extends ProjectileEntity {

    public TentacleProjectile(EntityType<? extends TentacleProjectile> type, World world) {
        super(type, world);
    }

    public TentacleProjectile(World world, LivingEntity owner) {
        super(ModEntities.TENTACLE, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        // Raycast for hits each tick
        HitResult hit = ProjectileUtil.getCollision(this, this::canHit);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onCollision(hit);
        }
        // Apply gravity and move
        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x, vel.y - 0.02, vel.z);
        this.setPosition(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);

        // Despawn after 3 seconds
        if (this.age > 60) this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (this.getWorld().isClient()) return;
        if (hit.getEntity() instanceof LivingEntity target && target != this.getOwner()) {
            target.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), 5.0f);
            // Apply poison and slowness — tentacle latches on
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
            this.discard();
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {}
}
