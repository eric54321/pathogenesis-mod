package com.pathogenesis.item;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * CAR-T Injector — a one-use therapy weapon.
 *
 * Biology note: CAR-T (Chimeric Antigen Receptor T-cell) therapy takes a patient's
 * own T-cells, genetically reprograms them in a lab to recognize cancer cell
 * surface proteins, then injects them back. The reprogrammed cells permanently
 * "remember" the target and attack it for the rest of the patient's life.
 *
 * In-game: using this item permanently increases the player's attack damage.
 * The boost is stored in the player's attribute system so it persists across
 * sessions and cannot be stacked by using multiple injectors.
 */
public class CARTInjectorItem extends Item {

    // How much extra attack damage the CAR-T boost gives (in half-hearts)
    private static final double ATTACK_BOOST_AMOUNT = 4.0;

    // Unique ID for the attribute modifier so we can check if it was already applied.
    // Using the mod's namespace prevents conflicts with other mods.
    private static final Identifier MODIFIER_ID =
        Identifier.of("pathogenesis", "cart_attack_boost");

    /**
     * Constructor — Item.Settings come from ModItems where maxCount(1) is set.
     */
    public CARTInjectorItem(Settings settings) {
        super(settings);
    }

    /**
     * Called when the player right-clicks while holding this item.
     * Applies a permanent attack damage modifier if the player has not already used one.
     *
     * TypedActionResult tells the game what happened:
     *  - SUCCESS: consume item, play animation
     *  - FAIL: do nothing, keep item
     *  - PASS: do nothing (default item behavior)
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Item logic only runs on the server — the client just plays the animation
        if (world.isClient()) {
            return TypedActionResult.pass(stack);
        }

        EntityAttributeInstance attackAttribute =
            user.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        if (attackAttribute == null) {
            return TypedActionResult.fail(stack);
        }

        // Check if this player already used a CAR-T Injector — prevent stacking
        if (attackAttribute.hasModifier(MODIFIER_ID)) {
            user.sendMessage(
                Text.literal("Your T-cells are already reprogrammed — one injection is enough.")
                    .formatted(Formatting.YELLOW),
                true  // 'true' shows the message above the hotbar (action bar), not in chat
            );
            return TypedActionResult.fail(stack);
        }

        // Build the permanent attribute modifier
        // ADD_VALUE adds a flat number to the base attack damage stat
        EntityAttributeModifier modifier = new EntityAttributeModifier(
            MODIFIER_ID,
            ATTACK_BOOST_AMOUNT,
            EntityAttributeModifier.Operation.ADD_VALUE
        );

        // addPersistentModifier saves to the player's NBT data, so it survives
        // logging out and back in — the reprogramming is permanent
        attackAttribute.addPersistentModifier(modifier);

        // Confirm to the player what happened
        user.sendMessage(
            Text.literal("CAR-T reprogramming complete! Your immune cells now deal more damage permanently.")
                .formatted(Formatting.GREEN),
            true
        );

        // Consume one item from the stack (removes it from the player's hand)
        stack.decrement(1);
        return TypedActionResult.success(stack);
    }

    /**
     * Adds explanatory tooltip lines when the player hovers over this item in their inventory.
     * This is where the educational biology text lives without interrupting gameplay.
     */
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(
            Text.literal("Reprograms your T-cells to permanently target cancer antigens.")
                .formatted(Formatting.AQUA)
        );
        tooltip.add(
            Text.literal("Permanently +" + (int) ATTACK_BOOST_AMOUNT + " attack damage. One use only.")
                .formatted(Formatting.GRAY)
        );
        tooltip.add(
            Text.literal("Based on real CAR-T cell therapy used in cancer treatment.")
                .formatted(Formatting.DARK_GRAY)
        );
    }
}
