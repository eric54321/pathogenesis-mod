package com.pathogenesis.init;

import com.pathogenesis.PathogenesisMod;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

/**
 * A deliberately weak armor set — roughly leather-tier defense, no toughness,
 * no knockback resistance. Meant as basic starter gear, not end-game armor.
 */
public class ModArmorMaterials {

    public static final RegistryEntry<ArmorMaterial> IMMUNE_BARRIER = Registry.registerReference(
        Registries.ARMOR_MATERIAL,
        Identifier.of(PathogenesisMod.MOD_ID, "immune_barrier"),
        new ArmorMaterial(
            Map.of(
                ArmorItem.Type.HELMET, 2,
                ArmorItem.Type.CHESTPLATE, 5,
                ArmorItem.Type.LEGGINGS, 4,
                ArmorItem.Type.BOOTS, 2
            ),
            12, // enchantability — a bit above leather
            RegistryEntry.of(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value()),
            () -> Ingredient.ofItems(net.minecraft.item.Items.LEATHER),
            List.of(new ArmorMaterial.Layer(Identifier.of(PathogenesisMod.MOD_ID, "immune_barrier"))),
            0.0f, // toughness
            0.0f  // knockback resistance
        )
    );

    public static void register() {
        PathogenesisMod.LOGGER.info("Pathogenesis armor materials registered.");
    }
}
