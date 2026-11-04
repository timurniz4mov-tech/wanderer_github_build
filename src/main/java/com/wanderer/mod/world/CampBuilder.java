package com.wanderer.mod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Лагерь: костёр по центру, 2-3 палатки с кроватью внутри. */
public final class CampBuilder {
    private static final Block[] WOOLS = {Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.GRAY_WOOL,
            Blocks.RED_WOOL, Blocks.LIGHT_GRAY_WOOL};
    private static final Block[] BEDS = {Blocks.RED_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.BLUE_BED};

    // направление "вдоль палатки" (w) и "поперёк" (u) для поворотов 0..3
    private static final int[] WX = {0, -1, 0, 1};
    private static final int[] WZ = {1, 0, -1, 0};
    private static final int[] UX = {1, 0, -1, 0};
    private static final int[] UZ = {0, 1, 0, -1};
    // куда смотрит кровать (от ног к голове) = против направления w
    private static final Direction[] BED_FACING = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private CampBuilder() {}

    /** Строит лагерь, только если местность достаточно ровная. */
    public static boolean tryBuild(WorldGenLevel level, BlockPos c, RandomSource random) {
        int min = c.getY(), max = c.getY();
        int[] offs = {-6, 0, 6};
        for (int ox : offs) {
            for (int oz : offs) {
                int gy = TerrainUtil.groundY(level, c.getX() + ox, c.getZ() + oz);
                if (gy == TerrainUtil.NONE) return false;
                min = Math.min(min, gy);
                max = Math.max(max, gy);
            }
        }
        if (max - min > 3) return false;
        build(level, c.getX(), c.getY(), c.getZ(), random);
        return true;
    }

    public static void build(WorldGenLevel level, int cx, int y0, int cz, RandomSource random) {
        // 1. выравниваем площадку и очищаем от деревьев/травы
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 49) continue;
                int x = cx + dx, z = cz + dz;
                int gy = TerrainUtil.groundY(level, x, z);
                if (gy == TerrainUtil.NONE) continue;

                for (int y = y0 + 1; y <= y0 + 10; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                if (gy < y0) {
                    for (int y = gy + 1; y < y0; y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
                if (d2 <= 42) {
                    level.setBlock(new BlockPos(x, y0, z), floor(random), 2);
                } else if (gy != y0) {
                    level.setBlock(new BlockPos(x, y0, z), Blocks.COARSE_DIRT.defaultBlockState(), 2);
                }
            }
        }

        int b = y0 + 1; // уровень, на котором стоят предметы

        // 2. костёр с каменным кольцом
        level.setBlock(new BlockPos(cx, b, cz),
                Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        BlockState slab = Blocks.COBBLESTONE_SLAB.defaultBlockState();
        level.setBlock(new BlockPos(cx + 1, b, cz), slab, 2);
        level.setBlock(new BlockPos(cx - 1, b, cz), slab, 2);
        level.setBlock(new BlockPos(cx, b, cz + 1), slab, 2);
        level.setBlock(new BlockPos(cx, b, cz - 1), slab, 2);

        // 3. палатки: центр (dx,dz) и поворот, чтобы вход смотрел на костёр
        List<int[]> sides = new ArrayList<>();
        sides.add(new int[]{-4, 0, 3}); // запад, вход на восток
        sides.add(new int[]{4, 0, 1});  // восток, вход на запад
        sides.add(new int[]{0, -4, 0}); // север, вход на юг
        sides.add(new int[]{0, 4, 2});  // юг, вход на север
        Collections.shuffle(sides, new java.util.Random(random.nextLong()));
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            int[] s = sides.get(i);
            tent(level, cx + s[0], b, cz + s[1], s[2],
                    WOOLS[random.nextInt(WOOLS.length)], BEDS[random.nextInt(BEDS.length)]);
        }
    }

    private static BlockState floor(RandomSource r) {
        float f = r.nextFloat();
        if (f < 0.35F) return Blocks.COARSE_DIRT.defaultBlockState();
        if (f < 0.65F) return Blocks.DIRT.defaultBlockState();
        return Blocks.DIRT_PATH.defaultBlockState();
    }

    /** Палатка 5x5: стены по бокам, ступенчатая крыша, закрытая задняя стенка, вход спереди, кровать внутри. */
    private static void tent(WorldGenLevel level, int tx, int b, int tz, int r, Block wool, Block bed) {
        BlockState main = wool.defaultBlockState();
        BlockState ridge = Blocks.WHITE_WOOL.defaultBlockState();
        for (int w = -2; w <= 2; w++) {
            for (int u = -2; u <= 2; u++) {
                int au = Math.abs(u);
                int x = tx + u * UX[r] + w * WX[r];
                int z = tz + u * UZ[r] + w * WZ[r];
                if (au == 2) { // боковые стены
                    put(level, x, b, z, main);
                    put(level, x, b + 1, z, main);
                }
                put(level, x, b + 2, z, main);                 // крыша
                if (au <= 1) put(level, x, b + 3, z, ridge);   // конёк
                if (w == -2 && au <= 1) {                      // задняя стенка
                    put(level, x, b, z, main);
                    put(level, x, b + 1, z, main);
                }
            }
        }
        // кровать: ноги в w=0, голова в w=-1 (в глубине палатки)
        BlockPos foot = new BlockPos(tx, b, tz);
        Direction facing = BED_FACING[r];
        BlockState bs = bed.defaultBlockState().setValue(BedBlock.FACING, facing);
        level.setBlock(foot, bs.setValue(BedBlock.PART, BedPart.FOOT), 2);
        level.setBlock(foot.relative(facing), bs.setValue(BedBlock.PART, BedPart.HEAD), 2);
    }

    private static void put(WorldGenLevel level, int x, int y, int z, BlockState s) {
        level.setBlock(new BlockPos(x, y, z), s, 2);
    }
}
