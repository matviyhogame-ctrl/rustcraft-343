package com.rustcraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;

import java.util.Locale;

public enum BuildTier {
    WOOD, STONE, METAL;

    public String key() { return name().toLowerCase(Locale.ROOT); }

    public Block block() {
        switch (this) {
            case WOOD: return ModBlocks.BUILDING_WOOD;
            case STONE: return ModBlocks.BUILDING_STONE;
            default: return ModBlocks.BUILDING_METAL;
        }
    }

    public Block stairs() {
        switch (this) {
            case WOOD: return ModBlocks.BUILDING_WOOD_STAIRS;
            case STONE: return ModBlocks.BUILDING_STONE_STAIRS;
            default: return ModBlocks.BUILDING_METAL_STAIRS;
        }
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        switch (this) {
            case WOOD: return item.isIn(ItemTags.PLANKS);
            case STONE: return item == Items.COBBLESTONE;
            default: return item == ModItems.METAL_FRAGMENTS;
        }
    }

    public int count(PlayerEntity player) {
        int n = 0;
        for (int i = 0; i < player.inventory.size(); i++) {
            ItemStack s = player.inventory.getStack(i);
            if (matches(s)) n += s.getCount();
        }
        return n;
    }

    public void take(PlayerEntity player, int amount) {
        for (int i = 0; i < player.inventory.size() && amount > 0; i++) {
            ItemStack s = player.inventory.getStack(i);
            if (matches(s)) {
                int t = Math.min(amount, s.getCount());
                s.decrement(t);
                amount -= t;
            }
        }
    }
}
