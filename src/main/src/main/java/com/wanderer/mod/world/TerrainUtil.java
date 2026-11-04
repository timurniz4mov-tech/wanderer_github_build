package com.wanderer.mod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TerrainUtil {
    public static final int NONE = Integer.MIN_VALUE;

    private TerrainUtil() {}

    /** Высота земли (блок почвы) в колонке или NONE, если там вода/постройка/камень. */
    public static int groundY(WorldGenLevel level, int x, int z) {
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int bottom = Math.max(level.getMinBuildHeight(), top - 32);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; y--) {
            p.set(x, y, z);
            BlockState s = level.getBlockState(p);
            if (!s.getFluidState().isEmpty()) return NONE;
            if (s.is(BlockTags.DIRT)) return y;
            if (s.isAir() || s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS)
                    || s.getCollisionShape(level, p).isEmpty()) {
                continue;
            }
            return NONE;
        }
        return NONE;
    }

    public static boolean isRoadBiome(Holder<Biome> b) {
        return b.is(BiomeTags.IS_FOREST) || b.is(BiomeTags.IS_TAIGA)
                || b.is(Biomes.PLAINS) || b.is(Biomes.SUNFLOWER_PLAINS) || b.is(Biomes.MEADOW);
    }

    /** Убирает траву/цветы/папоротники над дорогой. */
    public static void clearPlantsAbove(WorldGenLevel level, int x, int groundY, int z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int y = groundY + 1; y <= groundY + 3; y++) {
            p.set(x, y, z);
            BlockState s = level.getBlockState(p);
            if (s.isAir() || !s.getFluidState().isEmpty()) break;
            if (s.getCollisionShape(level, p).isEmpty()) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            } else {
                break;
            }
        }
    }
}
