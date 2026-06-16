package com.pathogenesis.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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

public class AnthraxBladeItem extends SwordItem {

    public static AttributeModifiersComponent buildModifiers() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(
                    Identifier.of("pathogenesis", "anthrax_blade_damage"),
                    // 20 base attack damage (netherite sword normally gives 8)
                    20.0,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(
                    Identifier.of("pathogenesis", "anthrax_blade_speed"),
                    -2.4,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    public AnthraxBladeItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings.attributeModifiers(buildModifiers()));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,   160, 2));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,   120, 1));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,  80, 2));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));

        if (!target.getWorld().isClient() && target.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.WITCH,
                target.getX(), target.getBodyY(0.5), target.getZ(),
                12, 0.4, 0.4, 0.4, 0.05);
        }

        // Bonus magic damage — bypasses armor
        target.damage(target.getDamageSources().magic(), 15.0f);

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
        tooltip.add(Text.literal("+15 bonus magic damage per hit (ignores armor)")
            .formatted(Formatting.AQUA));
        tooltip.add(Text.literal("\"The infection never stops.\"")
            .formatted(Formatting.DARK_GRAY));
    }
}
