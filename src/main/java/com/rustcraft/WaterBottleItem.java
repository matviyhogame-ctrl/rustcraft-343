package com.rustcraft;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/** Empty bottle: right-click water to fill. Full bottle: drink to restore thirst. */
public class WaterBottleItem extends Item {
    private final boolean filled;

    public WaterBottleItem(Settings settings, boolean filled) {
        super(settings);
        this.filled = filled;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (filled) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        BlockHitResult hit = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && world.getFluidState(hit.getBlockPos()).isIn(FluidTags.WATER)) {
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            if (world.isClient || user.abilities.creativeMode) {
                return TypedActionResult.success(stack);
            }
            stack.decrement(1);
            ItemStack water = new ItemStack(ModItems.WATER_BOTTLE);
            if (stack.isEmpty()) {
                return TypedActionResult.success(water);
            }
            user.inventory.offerOrDrop(world, water);
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    @Override public int getMaxUseTime(ItemStack stack) { return filled ? 32 : 0; }
    @Override public UseAction getUseAction(ItemStack stack) { return filled ? UseAction.DRINK : UseAction.NONE; }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!filled || !(user instanceof PlayerEntity)) {
            return stack;
        }
        PlayerEntity player = (PlayerEntity) user;
        if (!world.isClient) {
            ThirstManager.add(player, 50.0F);
        }
        if (!player.abilities.creativeMode) {
            stack.decrement(1);
            ItemStack empty = new ItemStack(ModItems.EMPTY_BOTTLE);
            if (stack.isEmpty()) {
                return empty;
            }
            player.inventory.offerOrDrop(world, empty);
        }
        return stack;
    }
}
