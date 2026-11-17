package com.rustcraft;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModBlocks {
    public static final Block SULFUR_ORE = reg("sulfur_ore", new Block(
        FabricBlockSettings.of(Material.STONE).strength(3.0F, 3.0F).requiresTool()
            .breakByTool(FabricToolTags.PICKAXES, 1).sounds(BlockSoundGroup.STONE)));

    public static final Block METAL_ORE = reg("metal_ore", new Block(
        FabricBlockSettings.of(Material.STONE).strength(3.0F, 3.0F).requiresTool()
            .breakByTool(FabricToolTags.PICKAXES, 1).sounds(BlockSoundGroup.STONE)));

    public static final Block SLEEPING_BAG = reg("sleeping_bag", new SleepingBagBlock(
        FabricBlockSettings.of(Material.DECORATION).strength(0.5F).noCollision().nonOpaque().sounds(BlockSoundGroup.WOOL)));

    public static final Block BUILDING_WOOD = regPlain("building_wood", new BuildingBlock(
        FabricBlockSettings.of(Material.WOOD).strength(4.0F, 6.0F).requiresTool()
            .breakByTool(FabricToolTags.AXES, 0).sounds(BlockSoundGroup.WOOD)));
    public static final Block BUILDING_STONE = regPlain("building_stone", new BuildingBlock(
        FabricBlockSettings.of(Material.STONE).strength(10.0F, 30.0F).requiresTool()
            .breakByTool(FabricToolTags.PICKAXES, 1).sounds(BlockSoundGroup.STONE)));
    public static final Block BUILDING_METAL = regPlain("building_metal", new BuildingBlock(
        FabricBlockSettings.of(Material.METAL).strength(25.0F, 600.0F).requiresTool()
            .breakByTool(FabricToolTags.PICKAXES, 2).sounds(BlockSoundGroup.METAL)));

    public static final Block BUILDING_WOOD_STAIRS = regPlain("building_wood_stairs",
        new BuildingStairsBlock(BUILDING_WOOD.getDefaultState(), FabricBlockSettings.copyOf(BUILDING_WOOD)));
    public static final Block BUILDING_STONE_STAIRS = regPlain("building_stone_stairs",
        new BuildingStairsBlock(BUILDING_STONE.getDefaultState(), FabricBlockSettings.copyOf(BUILDING_STONE)));
    public static final Block BUILDING_METAL_STAIRS = regPlain("building_metal_stairs",
        new BuildingStairsBlock(BUILDING_METAL.getDefaultState(), FabricBlockSettings.copyOf(BUILDING_METAL)));

    private static Block regPlain(String name, Block block) {
        return Registry.register(Registry.BLOCK, new Identifier(RustCraft.MOD_ID, name), block);
    }

    private static Block reg(String name, Block block) {
        Identifier id = new Identifier(RustCraft.MOD_ID, name);
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new Item.Settings().group(ModItems.GROUP)));
        return block;
    }

    public static void init() {}
}
