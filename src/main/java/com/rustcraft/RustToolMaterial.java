package com.rustcraft;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;

public enum RustToolMaterial implements ToolMaterial {
    STONE(1, 180, 4.0F, 1.0F, 5, Items.COBBLESTONE),
    METAL(2, 520, 7.0F, 2.0F, 12, Items.IRON_INGOT);

    private final int level, durability, enchant;
    private final float speed, damage;
    private final Item repair;

    RustToolMaterial(int level, int durability, float speed, float damage, int enchant, Item repair) {
        this.level = level;
        this.durability = durability;
        this.speed = speed;
        this.damage = damage;
        this.enchant = enchant;
        this.repair = repair;
    }

    @Override public int getDurability() { return durability; }
    @Override public float getMiningSpeedMultiplier() { return speed; }
    @Override public float getAttackDamage() { return damage; }
    @Override public int getMiningLevel() { return level; }
    @Override public int getEnchantability() { return enchant; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(repair); }
}
