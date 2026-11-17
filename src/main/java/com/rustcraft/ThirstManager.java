package com.rustcraft;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Rust-style thirst (0..100). Resets to 100 on death / server restart. */
public final class ThirstManager {
    private static final Map<UUID, Float> THIRST = new HashMap<>();
    private static final String KIT_TAG = "rustcraft_kit";

    public static float get(PlayerEntity p) {
        return THIRST.getOrDefault(p.getUuid(), 100.0F);
    }

    public static void add(PlayerEntity p, float amount) {
        THIRST.put(p.getUuid(), Math.max(0.0F, Math.min(100.0F, get(p) + amount)));
    }

    public static void tick(MinecraftServer server) {
        int ticks = server.getTicks();
        if (ticks % 20 != 0) return;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!p.getScoreboardTags().contains(KIT_TAG)) {
                p.addScoreboardTag(KIT_TAG);
                p.giveItemStack(new ItemStack(ModItems.STONE_HATCHET));
                p.giveItemStack(new ItemStack(ModItems.BANDAGE, 3));
                p.giveItemStack(new ItemStack(ModItems.EMPTY_BOTTLE, 1));
                p.giveItemStack(new ItemStack(Items.TORCH, 1));
            }
            if (!p.isAlive()) {
                THIRST.put(p.getUuid(), 100.0F);
                continue;
            }
            if (p.isCreative() || p.isSpectator()) continue;

            float drain = 0.12F;
            if (p.isSprinting()) drain *= 2.0F;
            if (p.world.getBiome(p.getBlockPos()).getCategory() == Biome.Category.DESERT) drain *= 1.5F;
            float value = Math.max(0.0F, get(p) - drain);
            THIRST.put(p.getUuid(), value);

            if (value < 15.0F) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, false));
            }
            if (value <= 0.0F && (ticks / 20) % 2 == 0) {
                p.damage(DamageSource.STARVE, 1.0F);
            }
            p.sendMessage(new TranslatableText("hud.rustcraft.thirst", (int) value), true);
        }
    }

    private ThirstManager() {}
}
