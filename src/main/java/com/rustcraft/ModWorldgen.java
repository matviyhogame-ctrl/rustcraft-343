package com.rustcraft;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public final class ModWorldgen {
    private static void ore(String name, Block block, int size, int maxY, int repeat) {
        ConfiguredFeature<?, ?> feature = Feature.ORE
            .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, block.getDefaultState(), size))
            .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(0, 0, maxY)))
            .spreadHorizontally()
            .repeat(repeat);
        Identifier id = new Identifier(RustCraft.MOD_ID, name);
        RegistryKey<ConfiguredFeature<?, ?>> key = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, id);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, key);
    }

    public static void init() {
        ore("sulfur_ore", ModBlocks.SULFUR_ORE, 8, 40, 6);
        ore("metal_ore", ModBlocks.METAL_ORE, 9, 64, 10);
    }

    private ModWorldgen() {}
}
