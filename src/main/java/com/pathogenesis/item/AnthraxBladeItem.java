package com.pathogenesis.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Anthrax Blade — unique weapon dropped by Bacillus Anthracis.
 *
 * Netherite-tier base + extra flat damage via attribute modifier.
 * On hit: Poison III + Wither II + Slowness III. Deadly toxin on every strike.
 */
public class AnthraxBladeItem extends SwordItem {

    private static final Identifier DAMAGE_BONUS_ID =
        Identifier.of("pathogenesis", "anthrax_blade_damage");

    public AnthraxBladeItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Anthrax toxin injection on every hit
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,    160, 2)); // Poison III, 8s
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,    120, 1)); // Wither II, 6s
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,   80, 2)); // Slowness III, 4s
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,  100, 1)); // Weakness II, 5s

        // Green spore particle burst on hit
        if (!target.getWorld().isClient() && target.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.WITCH,
                target.getX(), target.getBodyY(0.5), target.getZ(),
                8, 0.3, 0.3, 0.3, 0.05);
        }

        // Extra flat damage on top of sword damage (additional 6 hearts = 12 HP)
        target.damage(target.getDamageSources().magic(), 6.0f);

        target.getWorld().playSound(null, target.getBlockPos(),
            SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.4f, 1.8f);

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
        tooltip.add(Text.literal("On hit: Poison III + Wither II + Slowness III + Weakness II")
            .formatted(Formatting.GREEN));
        tooltip.add(Text.literal("+6 bonus magic damage per hit")
            .formatted(Formatting.AQUA));
        tooltip.add(Text.literal("\"The infection never stops.\"")
            .formatted(Formatting.DARK_GRAY));
    }
}
