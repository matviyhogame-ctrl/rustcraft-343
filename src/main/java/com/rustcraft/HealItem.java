package com.rustcraft;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Bandage / Medkit: hold right-click to heal. */
public class HealItem extends Item {
    private final float heal;
    private final int useTicks;
    private final boolean regen;

    public HealItem(Settings settings, float heal, int useTicks, boolean regen) {
        super(settings);
        this.heal = heal;
        this.useTicks = useTicks;
        this.regen = regen;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getHealth() >= user.getMaxHealth()) {
            return TypedActionResult.fail(stack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override public int getMaxUseTime(ItemStack stack) { return useTicks; }
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.heal(heal);
            if (regen) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
            }
        }
        if (!(user instanceof PlayerEntity) || !((PlayerEntity) user).abilities.creativeMode) {
            stack.decrement(1);
        }
        return stack;
    }
}
