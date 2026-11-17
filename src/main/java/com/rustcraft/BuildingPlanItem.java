package com.rustcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Right-click a block: place the selected piece.
 * Sneak + right-click: next piece. Right-click in the air: next material (wood / stone / metal).
 */
public class BuildingPlanItem extends Item {
    public BuildingPlanItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;
        ItemStack stack = ctx.getStack();
        if (player.isSneaking()) {
            BuildingPlan.nextPiece(stack);
            player.sendMessage(BuildingPlan.describe(stack), true);
            return ActionResult.SUCCESS;
        }
        if (player instanceof ServerPlayerEntity && world instanceof ServerWorld) {
            BuildingPlan.place((ServerWorld) world, (ServerPlayerEntity) player, stack,
                ctx.getBlockPos(), ctx.getSide(), ctx.getHitPos());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            if (user.isSneaking()) {
                BuildingPlan.nextPiece(stack);
            } else {
                BuildingPlan.nextTier(stack);
            }
            user.sendMessage(BuildingPlan.describe(stack), true);
        }
        return TypedActionResult.success(stack);
    }
}
