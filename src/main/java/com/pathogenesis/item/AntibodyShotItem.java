package com.pathogenesis.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class AntibodyShotItem extends Item {

    public AntibodyShotItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, false, true)); // Regen I 5s
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,    60, 0, false, true)); // Resistance I 3s

        world.playSound(null, user.getBlockPos(),
            SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5f, 1.8f);

        if (!user.isCreative()) stack.decrement(1);
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("A concentrated antibody injection.")
            .formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Grants Regeneration I + Resistance I briefly")
            .formatted(Formatting.GRAY));
    }
}
