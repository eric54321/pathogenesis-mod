package com.pathogenesis.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class HealingShieldItem extends Item {

    private static final int COOLDOWN_TICKS = 400; // 20 seconds

    public HealingShieldItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1, false, true)); // Regen II 10s
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,   100, 0, false, true)); // Absorption I 5s
        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        world.playSound(null, user.getBlockPos(),
            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.5f);

        if (!world.isClient() && world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.HEART,
                user.getX(), user.getBodyY(0.8), user.getZ(),
                8, 0.4, 0.4, 0.4, 0.05);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click to activate healing protocols.")
            .formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Grants Regeneration II + Absorption for 10s")
            .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("20 second cooldown").formatted(Formatting.DARK_GRAY));
    }
}
