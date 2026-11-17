package com.rustcraft;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModItems {
    public static final ItemGroup GROUP = FabricItemGroupBuilder.build(
        new Identifier(RustCraft.MOD_ID, "main"), () -> new ItemStack(ModItems.METAL_HATCHET));

    private static Item.Settings s() { return new Item.Settings().group(GROUP); }

    public static final Item SULFUR = reg("sulfur", new Item(s()));
    public static final Item METAL_FRAGMENTS = reg("metal_fragments", new Item(s()));

    public static final Item STONE_HATCHET = reg("stone_hatchet", new AxeItem(RustToolMaterial.STONE, 5.0F, -3.0F, s()) {});
    public static final Item METAL_HATCHET = reg("metal_hatchet", new AxeItem(RustToolMaterial.METAL, 5.0F, -3.0F, s()) {});
    public static final Item METAL_PICKAXE = reg("metal_pickaxe", new PickaxeItem(RustToolMaterial.METAL, 1, -2.8F, s()) {});

    public static final Item BANDAGE = reg("bandage", new HealItem(s().maxCount(16), 4.0F, 30, false));
    public static final Item MEDKIT = reg("medkit", new HealItem(s().maxCount(4), 10.0F, 80, true));

    public static final Item EMPTY_BOTTLE = reg("empty_bottle", new WaterBottleItem(s().maxCount(16), false));
    public static final Item WATER_BOTTLE = reg("water_bottle", new WaterBottleItem(s().maxCount(16), true));

    public static final Item BUILDING_PLAN = reg("building_plan", new BuildingPlanItem(s().maxCount(1)));

    private static Item reg(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(RustCraft.MOD_ID, name), item);
    }

    public static void init() {}
}
