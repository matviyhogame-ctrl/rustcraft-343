package com.rustcraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

public class BuildingStairsBlock extends StairsBlock implements BuildingPiece {
    public BuildingStairsBlock(BlockState base, Settings settings) {
        super(base, settings);
    }
}
