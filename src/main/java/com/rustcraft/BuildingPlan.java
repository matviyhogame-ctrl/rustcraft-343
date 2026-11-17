package com.rustcraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Rust-style building plan logic: piece selection, grid snapping, placement and ghost preview. */
public final class BuildingPlan {
    private static final String PLAN_TAG = "rustcraft_plan";

    public static final class Placement {
        public final List<BlockPos> preview = new ArrayList<>();
        public final List<BlockPos> blocks = new ArrayList<>();
        public final List<BlockState> states = new ArrayList<>();
        public String error = null;

        void add(BlockPos pos, BlockState state, boolean visible) {
            blocks.add(pos);
            states.add(state);
            if (visible) preview.add(pos);
        }
    }

    // ---------- selection stored in the item's NBT ----------

    public static BuildPiece getPiece(ItemStack stack) {
        NbtCompound t = stack.getTag();
        int i = t == null ? 0 : t.getInt("piece");
        return BuildPiece.values()[Math.floorMod(i, BuildPiece.values().length)];
    }

    public static BuildTier getTier(ItemStack stack) {
        NbtCompound t = stack.getTag();
        int i = t == null ? 0 : t.getInt("tier");
        return BuildTier.values()[Math.floorMod(i, BuildTier.values().length)];
    }

    public static void nextPiece(ItemStack stack) {
        stack.getOrCreateTag().putInt("piece", (getPiece(stack).ordinal() + 1) % BuildPiece.values().length);
    }

    public static void nextTier(ItemStack stack) {
        stack.getOrCreateTag().putInt("tier", (getTier(stack).ordinal() + 1) % BuildTier.values().length);
    }

    public static MutableText describe(ItemStack stack) {
        BuildPiece piece = getPiece(stack);
        BuildTier tier = getTier(stack);
        return new TranslatableText("building.rustcraft.selected",
            new TranslatableText("building.rustcraft.piece." + piece.key()),
            new TranslatableText("building.rustcraft.tier." + tier.key()),
            piece.cost);
    }

    // ---------- geometry ----------

    private static boolean inTriangle(int i, int j, int rot) {
        for (int k = 0; k < rot; k++) {
            int ni = j;
            int nj = 2 - i;
            i = ni;
            j = nj;
        }
        return i + j <= 2;
    }

    private static boolean canReplace(World world, BlockPos pos, boolean terrainOk) {
        if (pos.getY() < 1 || pos.getY() > 250) return false;
        BlockState s = world.getBlockState(pos);
        if (s.getBlock() instanceof BuildingPiece) return false;
        if (world.getBlockEntity(pos) != null) return false;
        if (s.isAir()) return true;
        Material m = s.getMaterial();
        if (m.isReplaceable() || m.isLiquid()) return true;
        if (terrainOk) {
            return m == Material.PLANT || m == Material.REPLACEABLE_PLANT || m == Material.SOIL
                || m == Material.SOLID_ORGANIC || m == Material.AGGREGATE || m == Material.STONE
                || m == Material.LEAVES || m == Material.SNOW_LAYER;
        }
        return false;
    }

    private static boolean fillable(BlockState s) {
        return s.isAir() || !s.getMaterial().isSolid();
    }

    public static Placement compute(World world, ServerPlayerEntity player, BlockPos hitPos, Direction face,
                                    Vec3d hit, BuildPiece piece, BuildTier tier) {
        Placement pl = new Placement();
        if (face == Direction.DOWN) {
            pl.error = "surface";
            return pl;
        }
        boolean onBuilding = world.getBlockState(hitPos).getBlock() instanceof BuildingPiece;
        int y, cx, cz;
        if (face == Direction.UP) {
            y = hitPos.getY() + 1;
            cx = Math.floorDiv(hitPos.getX(), 3);
            cz = Math.floorDiv(hitPos.getZ(), 3);
        } else if (onBuilding) {
            y = hitPos.getY();
            cx = Math.floorDiv(hitPos.getX(), 3) + face.getOffsetX();
            cz = Math.floorDiv(hitPos.getZ(), 3) + face.getOffsetZ();
        } else {
            BlockPos t = hitPos.offset(face);
            y = hitPos.getY();
            cx = Math.floorDiv(t.getX(), 3);
            cz = Math.floorDiv(t.getZ(), 3);
        }
        int ox = cx * 3;
        int oz = cz * 3;
        Direction facing = player.getHorizontalFacing();
        Block block = tier.block();
        BlockState bs = block.getDefaultState();
        boolean wallSupported = false;

        switch (piece) {
            case FOUNDATION:
            case TRI_FOUNDATION:
            case FLOOR:
            case TRI_FLOOR: {
                int rot = facing.getHorizontal();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (piece.isTriangle() && !inTriangle(i, j, rot)) continue;
                        pl.add(new BlockPos(ox + i, y, oz + j), bs, true);
                    }
                }
                break;
            }
            case WALL:
            case DOORWAY:
            case WINDOW: {
                double lx = Math.max(0, Math.min(3, hit.x - ox));
                double lz = Math.max(0, Math.min(3, hit.z - oz));
                double dn = lz, ds = 3 - lz, dw = lx, de = 3 - lx;
                double min = Math.min(Math.min(dn, ds), Math.min(dw, de));
                boolean alongX;
                int fixed;
                if (min == dn) { alongX = true; fixed = oz; }
                else if (min == ds) { alongX = true; fixed = oz + 2; }
                else if (min == dw) { alongX = false; fixed = ox; }
                else { alongX = false; fixed = ox + 2; }
                for (int i = 0; i < 3; i++) {
                    BlockPos base = alongX ? new BlockPos(ox + i, y, fixed) : new BlockPos(fixed, y, oz + i);
                    if (world.getBlockState(base.down()).getBlock() instanceof BuildingPiece) wallSupported = true;
                    for (int h = 0; h < 3; h++) {
                        if (piece == BuildPiece.DOORWAY && i == 1 && h < 2) continue;
                        if (piece == BuildPiece.WINDOW && i == 1 && h == 1) continue;
                        pl.add(base.up(h), bs, true);
                    }
                }
                break;
            }
            case STAIRS: {
                BlockState stair = tier.stairs().getDefaultState().with(StairsBlock.FACING, facing);
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        int x, z;
                        switch (facing) {
                            case NORTH: x = ox + c; z = oz + 2 - r; break;
                            case SOUTH: x = ox + c; z = oz + r; break;
                            case EAST: x = ox + r; z = oz + c; break;
                            default: x = ox + 2 - r; z = oz + c; break;
                        }
                        for (int k = 0; k < r; k++) {
                            pl.add(new BlockPos(x, y + k, z), bs, false);
                        }
                        pl.add(new BlockPos(x, y + r, z), stair, true);
                    }
                }
                break;
            }
        }

        // free space check
        for (int i = 0; i < pl.blocks.size(); i++) {
            if (!canReplace(world, pl.blocks.get(i), piece.isFoundation())) {
                pl.error = "obstructed";
                return pl;
            }
        }

        // support check
        Set<BlockPos> set = new HashSet<>(pl.blocks);
        if (piece.isFoundation()) {
            boolean grounded = false;
            List<BlockPos> extra = new ArrayList<>();
            for (BlockPos p : new ArrayList<>(pl.preview)) {
                for (int dy = 1; dy <= 8; dy++) {
                    BlockPos q = p.down(dy);
                    BlockState qs = world.getBlockState(q);
                    if (fillable(qs) && !(qs.getBlock() instanceof BuildingPiece)) {
                        extra.add(q);
                    } else {
                        grounded = true;
                        break;
                    }
                }
            }
            if (!grounded) {
                // maybe anchored by neighbouring building blocks at the same level
                for (BlockPos p : pl.blocks) {
                    for (Direction d : Direction.values()) {
                        BlockPos n = p.offset(d);
                        if (!set.contains(n) && world.getBlockState(n).getBlock() instanceof BuildingPiece) grounded = true;
                    }
                }
            }
            if (!grounded) {
                pl.error = "unsupported";
                return pl;
            }
            for (BlockPos q : extra) pl.add(q, bs, false);
        } else if (piece.isWall()) {
            if (!wallSupported) {
                pl.error = "unsupported";
                return pl;
            }
        } else {
            boolean ok = false;
            for (BlockPos p : pl.blocks) {
                for (Direction d : Direction.values()) {
                    BlockPos n = p.offset(d);
                    if (!set.contains(n) && world.getBlockState(n).getBlock() instanceof BuildingPiece) ok = true;
                }
            }
            if (!ok) {
                pl.error = "unsupported";
                return pl;
            }
        }

        if (!player.isCreative() && tier.count(player) < piece.cost) {
            pl.error = "resources";
        }
        return pl;
    }

    // ---------- placing ----------

    public static void place(ServerWorld world, ServerPlayerEntity player, ItemStack plan, BlockPos hitPos,
                             Direction face, Vec3d hit) {
        BuildPiece piece = getPiece(plan);
        BuildTier tier = getTier(plan);
        Placement pl = compute(world, player, hitPos, face, hit, piece, tier);
        if (pl.error != null) {
            player.sendMessage(new TranslatableText("building.rustcraft.error." + pl.error).formatted(Formatting.RED), true);
            return;
        }
        if (!player.isCreative()) tier.take(player, piece.cost);
        for (int i = 0; i < pl.blocks.size(); i++) {
            world.setBlockState(pl.blocks.get(i), pl.states.get(i), 3);
        }
        SoundEvent sound = tier == BuildTier.WOOD ? SoundEvents.BLOCK_WOOD_PLACE
            : tier == BuildTier.STONE ? SoundEvents.BLOCK_STONE_PLACE : SoundEvents.BLOCK_METAL_PLACE;
        world.playSound(null, pl.blocks.get(0), sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // ---------- ghost preview + starter plan ----------

    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 4 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!p.getScoreboardTags().contains(PLAN_TAG)) {
                p.addScoreboardTag(PLAN_TAG);
                p.giveItemStack(new ItemStack(ModItems.BUILDING_PLAN));
            }
            ItemStack held = p.getMainHandStack();
            if (!(held.getItem() instanceof BuildingPlanItem)) held = p.getOffHandStack();
            if (!(held.getItem() instanceof BuildingPlanItem)) continue;

            MutableText msg = describe(held);
            HitResult hr = p.raycast(6.0D, 0.0F, false);
            if (hr.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bh = (BlockHitResult) hr;
                Placement pl = compute(p.world, p, bh.getBlockPos(), bh.getSide(), bh.getPos(), getPiece(held), getTier(held));
                boolean ok = pl.error == null;
                DustParticleEffect fx = ok ? new DustParticleEffect(0.1F, 1.0F, 0.2F, 1.0F)
                                           : new DustParticleEffect(1.0F, 0.1F, 0.1F, 1.0F);
                ServerWorld sw = p.getServerWorld();
                int n = 0;
                for (BlockPos pos : pl.preview) {
                    if (n++ > 40) break;
                    sw.spawnParticles(p, fx, true, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
                }
                if (!ok) {
                    msg.append(new LiteralText("  "));
                    msg.append(new TranslatableText("building.rustcraft.error." + pl.error).formatted(Formatting.RED));
                }
            }
            p.sendMessage(msg, true);
        }
    }

    private BuildingPlan() {}
}
