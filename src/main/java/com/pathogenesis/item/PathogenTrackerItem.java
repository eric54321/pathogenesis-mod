package com.pathogenesis.item;

import com.pathogenesis.system.BossArena;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PathogenTrackerItem extends Item {

    public PathogenTrackerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            BlockPos center = BossArena.getArenaCenter();
            if (center != null) {
                int dist = (int) Math.sqrt(
                    Math.pow(user.getX() - center.getX(), 2) +
                    Math.pow(user.getZ() - center.getZ(), 2)
                );
                user.sendMessage(Text.literal("☣ Pathogen signal detected!")
                    .formatted(Formatting.DARK_GREEN, Formatting.BOLD), false);
                user.sendMessage(Text.literal("  → Coordinates: X=" + center.getX() + ", Z=" + center.getZ())
                    .formatted(Formatting.GREEN), false);
                user.sendMessage(Text.literal("  → Distance: " + dist + " blocks away")
                    .formatted(Formatting.YELLOW), false);
                user.sendMessage(Text.literal("  ⚠ Approach with extreme caution.")
                    .formatted(Formatting.RED), false);
            } else {
                user.sendMessage(Text.literal("No signal detected yet...")
                    .formatted(Formatting.GRAY), false);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
