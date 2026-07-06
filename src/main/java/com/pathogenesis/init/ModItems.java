package com.pathogenesis.init;

import com.pathogenesis.PathogenesisMod;
import com.pathogenesis.item.AnthraxBladeItem;
import com.pathogenesis.item.AntibodyShotItem;
import com.pathogenesis.item.AntibodySwordItem;
import com.pathogenesis.item.CARTInjectorItem;
import com.pathogenesis.item.HealingShieldItem;
import com.pathogenesis.item.PathogenTrackerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers all custom items with Minecraft's item registry.
 * Also places items into creative-mode item groups so players
 * can find them easily during testing.
 */
public class ModItems {

    /**
     * The CAR-T Injector item instance.
     * Item.Settings() configures the item — maxCount(1) means it stacks to 1
     * because it is a one-use consumable that should not stack.
     */
    public static final CARTInjectorItem CART_INJECTOR = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "cart_injector"),
        new CARTInjectorItem(new Item.Settings().maxCount(1))
    );

    public static final AnthraxBladeItem ANTHRAX_BLADE = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "anthrax_blade"),
        new AnthraxBladeItem(new Item.Settings().maxCount(1))
    );

    public static final PathogenTrackerItem PATHOGEN_TRACKER = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "pathogen_tracker"),
        new PathogenTrackerItem(new Item.Settings().maxCount(1))
    );

    public static final AntibodySwordItem ANTIBODY_SWORD = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "antibody_sword"),
        new AntibodySwordItem(new Item.Settings().maxCount(1))
    );

    public static final HealingShieldItem HEALING_SHIELD = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "healing_shield"),
        new HealingShieldItem(new Item.Settings().maxCount(1))
    );

    public static final AntibodyShotItem ANTIBODY_SHOT = Registry.register(
        Registries.ITEM,
        Identifier.of(PathogenesisMod.MOD_ID, "antibody_shot"),
        new AntibodyShotItem(new Item.Settings().maxCount(32))
    );

    /**
     * Called from PathogenesisMod.onInitialize().
     * Adds our items to the Medicine & Potions creative tab so testers
     * can grab them without commands.
     */
    public static void register() {
        // Add CAR-T Injector to the medicine creative tab for easy testing
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(CART_INJECTOR);
            entries.add(PATHOGEN_TRACKER);
            entries.add(ANTHRAX_BLADE);
            entries.add(ANTIBODY_SWORD);
            entries.add(HEALING_SHIELD);
            entries.add(ANTIBODY_SHOT);
        });

        PathogenesisMod.LOGGER.info("Pathogenesis items registered.");
    }
}
