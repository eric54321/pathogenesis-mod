package com.pathogenesis.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Anthrax Blade — unique weapon dropped by the Bacillus Anthracis boss.
 *
 * Biology note: Bacillus anthracis secretes anthrax toxins (Lethal Factor + Edema Factor)
 * that destroy immune cells and cause massive tissue damage. This blade channels those same
 * toxins, rotting enemies from within on every hit.
 *
 * Stats: Netherite-tier damage + Poison II + Wither II on hit. Always glows.
 */
public class AnthraxBladeItem extends SwordItem {

    public AnthraxBladeItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Inject anthrax toxins into the target on every hit
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,  60, 1));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Forged from the toxins of Bacillus Anthracis.")
            .formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.literal("Inflicts Poison II + Wither II + Weakness on every hit.")
            .formatted(Formatting.GREEN));
        tooltip.add(Text.literal("\"The infection never stops.\"")
            .formatted(Formatting.DARK_GRAY));
    }
}
