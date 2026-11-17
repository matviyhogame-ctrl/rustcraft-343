package com.rustcraft;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.List;
import java.util.Random;

/** Supply drop: a loot chest appears near a random player every 20 minutes (or via /rust airdrop). */
public final class Airdrop {
    private static final int INTERVAL_TICKS = 20 * 60 * 20;

    private static final class Entry {
        final Item item; final int min, max; final float chance;
        Entry(Item item, int min, int max, float chance) { this.item = item; this.min = min; this.max = max; this.chance = chance; }
    }

    private static final Entry[] LOOT = {
        new Entry(ModItems.METAL_FRAGMENTS, 30, 80, 1.0F),
        new Entry(ModItems.SULFUR, 20, 60, 0.9F),
        new Entry(Items.GUNPOWDER, 10, 30, 0.8F),
        new Entry(ModItems.BANDAGE, 3, 6, 0.9F),
        new Entry(ModItems.MEDKIT, 1, 2, 0.6F),
        new Entry(ModItems.METAL_HATCHET, 1, 1, 0.3F),
        new Entry(ModItems.METAL_PICKAXE, 1, 1, 0.3F),
        new Entry(Items.BOW, 1, 1, 0.4F),
        new Entry(Items.ARROW, 20, 40, 0.7F),
        new Entry(Items.IRON_INGOT, 8, 24, 0.6F),
        new Entry(Items.GOLDEN_APPLE, 1, 3, 0.4F),
        new Entry(Items.DIAMOND, 1, 3, 0.2F),
        new Entry(ModItems.WATER_BOTTLE, 2, 5, 0.7F)
    };

    public static void tick(MinecraftServer server) {
        int t = server.getTicks();
        if (t <= 0 || t % INTERVAL_TICKS != 0) return;
        ServerWorld world = server.getOverworld();
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;
        ServerPlayerEntity target = players.get(world.random.nextInt(players.size()));
        drop(world, target.getBlockPos());
    }

    public static void drop(ServerWorld world, BlockPos origin) {
        Random r = world.random;
        BlockPos pos = null;
        for (int i = 0; i < 16 && pos == null; i++) {
            int x = origin.getX() + (r.nextBoolean() ? 1 : -1) * (60 + r.nextInt(120));
            int z = origin.getZ() + (r.nextBoolean() ? 1 : -1) * (60 + r.nextInt(120));
            world.getChunk(x >> 4, z >> 4);
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos c = new BlockPos(x, y, z);
            if (world.getFluidState(c.down()).isEmpty() && world.getBlockState(c.down()).isSolidBlock(world, c.down())) {
                pos = c;
            }
        }
        if (pos == null) return;

        world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity) be;
            for (Entry e : LOOT) {
                if (r.nextFloat() > e.chance) continue;
                int count = e.min + r.nextInt(e.max - e.min + 1);
                int slot = r.nextInt(chest.size());
                for (int tries = 0; tries < chest.size() && !chest.getStack(slot).isEmpty(); tries++) {
                    slot = (slot + 1) % chest.size();
                }
                chest.setStack(slot, new ItemStack(e.item, count));
            }
        }

        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
            bolt.setCosmetic(true);
            world.spawnEntity(bolt);
        }

        Text msg = new TranslatableText("message.rustcraft.airdrop", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.RED);
        world.getServer().getPlayerManager().broadcastChatMessage(msg, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private Airdrop() {}
}
