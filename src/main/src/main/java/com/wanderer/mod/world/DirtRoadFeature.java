package com.wanderer.mod.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/** Редкие грязные дороги. Иногда у дороги появляется лагерь. */
public class DirtRoadFeature extends Feature<NoneFeatureConfiguration> {
    /** Чем больше число, тем реже лагеря (шанс 1 к N на чанк с дорогой). */
    public static final int CAMP_RARITY = 20;
    /** Порог "маски": чем больше, тем реже дороги в мире. */
    private static final double MASK_MIN = 0.30;

    public DirtRoadFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        int baseX = origin.getX() & ~15;
        int baseZ = origin.getZ() & ~15;
        RoadNoise noise = RoadNoise.get(level.getSeed());

        if (noise.mask(baseX + 8, baseZ + 8) < MASK_MIN - 0.15) return false;

        List<BlockPos> cells = new ArrayList<>();
        boolean any = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx;
                int z = baseZ + dz;

                double m = noise.mask(x, z);
                if (m < MASK_MIN) continue;
                double taper = Math.min(1.0, (m - MASK_MIN) / 0.10);
                if (taper < 0.08) continue;

                double v = noise.v(x, z);
                double gx = noise.v(x + 1, z) - v;
                double gz = noise.v(x, z + 1) - v;
                double g = Math.sqrt(gx * gx + gz * gz);
                if (g < 1.0e-5) continue;

                double dist = Math.abs(v) / g; // примерное расстояние до центра дороги в блоках
                double w = Mth.clamp(noise.width(x, z) * 0.5 + 0.5, 0.0, 1.0);
                double half = (1.1 + 0.9 * w) * taper;
                if (dist > half + 1.6) continue;

                int gy = TerrainUtil.groundY(level, x, z);
                if (gy == TerrainUtil.NONE) continue;
                BlockPos gp = new BlockPos(x, gy, z);
                if (!TerrainUtil.isRoadBiome(level.getBiome(gp))) continue;

                if (dist <= half) {
                    level.setBlock(gp, pickRoad(random), 2);
                    TerrainUtil.clearPlantsAbove(level, x, gy, z);
                    cells.add(gp);
                    any = true;
                } else if (random.nextFloat() < 0.45F) {
                    level.setBlock(gp, random.nextBoolean()
                            ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState(), 2);
                    TerrainUtil.clearPlantsAbove(level, x, gy, z);
                    any = true;
                }
            }
        }

        if (!cells.isEmpty() && random.nextInt(CAMP_RARITY) == 0) {
            BlockPos c = cells.get(random.nextInt(cells.size()));
            CampBuilder.tryBuild(level, c, random);
        }
        return any;
    }

    private static BlockState pickRoad(RandomSource r) {
        float f = r.nextFloat();
        if (f < 0.38F) return Blocks.COARSE_DIRT.defaultBlockState();
        if (f < 0.68F) return Blocks.DIRT.defaultBlockState();
        if (f < 0.88F) return Blocks.DIRT_PATH.defaultBlockState();
        return Blocks.MUD.defaultBlockState();
    }
}
