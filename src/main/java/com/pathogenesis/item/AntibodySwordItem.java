package com.pathogenesis.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class AntibodySwordItem extends SwordItem {

    public static AttributeModifiersComponent buildModifiers() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(
                    Identifier.of("pathogenesis", "antibody_sword_damage"),
                    5.0,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(
                    Identifier.of("pathogenesis", "antibody_sword_speed"),
                    -2.4,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    public AntibodySwordItem(Settings settings) {
        super(ToolMaterials.STONE, settings.attributeModifiers(buildModifiers()));
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Standard-issue immune cell combat blade.")
            .formatted(Formatting.AQUA));
        tooltip.add(Text.literal("5 attack damage").formatted(Formatting.GRAY));
    }
}
