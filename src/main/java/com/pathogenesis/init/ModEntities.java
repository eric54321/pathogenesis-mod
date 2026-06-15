package com.pathogenesis.init;

import com.pathogenesis.PathogenesisMod;
import com.pathogenesis.entity.RogueCellEntity;
import com.pathogenesis.entity.VironEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers all custom mob types with Minecraft's entity registry.
 * Every new mob must be added here AND have its attributes registered
 * with FabricDefaultAttributeRegistry so the game knows its stats.
 */
public class ModEntities {

    /**
     * The EntityType object for RogueCell.
     * SpawnGroup.MONSTER means it is treated as a hostile mob (counts toward mob cap, etc.).
     * dimensions() sets the hitbox: 0.6 wide, 1.8 tall (similar to a zombie).
     */
    public static final EntityType<RogueCellEntity> ROGUE_CELL = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(PathogenesisMod.MOD_ID, "rogue_cell"),
        EntityType.Builder.create(RogueCellEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.5f, 1.5f)
            .build()
    );

    /**
     * Viron — fast, fragile viral particle that travels in swarms of 5.
     * Small hitbox (0.4 wide, 0.4 tall) to match its tiny real-world scale.
     */
    public static final EntityType<VironEntity> VIRON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(PathogenesisMod.MOD_ID, "viron"),
        EntityType.Builder.create(VironEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.4f, 0.4f)
            .build()
    );

    /**
     * Called from PathogenesisMod.onInitialize().
     * Registers entity attribute sets (health, speed, damage) for each mob.
     * Without this call the game will crash when the entity tries to spawn.
     */
    public static void register() {
        FabricDefaultAttributeRegistry.register(ROGUE_CELL, RogueCellEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VIRON, VironEntity.createAttributes());

        PathogenesisMod.LOGGER.info("Pathogenesis entities registered.");
    }
}
