package com.wanderer.mod.world;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/** Шум, по нулевой линии которого идёт дорога (одинаковый во всех чанках, поэтому дорога непрерывна). */
final class RoadNoise {
    private static volatile RoadNoise cache;

    final long seed;
    private final ImprovedNoise a, b, mask, width;

    static RoadNoise get(long seed) {
        RoadNoise c = cache;
        if (c != null && c.seed == seed) return c;
        synchronized (RoadNoise.class) {
            c = cache;
            if (c == null || c.seed != seed) {
                c = new RoadNoise(seed);
                cache = c;
            }
            return c;
        }
    }

    private RoadNoise(long seed) {
        this.seed = seed;
        this.a = new ImprovedNoise(RandomSource.create(seed ^ 0x5DEECE66DL));
        this.b = new ImprovedNoise(RandomSource.create(seed ^ 0x1B873593L));
        this.mask = new ImprovedNoise(RandomSource.create(seed ^ 0x2545F491L));
        this.width = new ImprovedNoise(RandomSource.create(seed ^ 0x7A3C9E15L));
    }

    double v(double x, double z) {
        return a.noise(x * 0.0065, 0.37, z * 0.0065) + 0.3 * b.noise(x * 0.02, 1.71, z * 0.02);
    }

    double mask(double x, double z) {
        return mask.noise(x * 0.0028, 2.2, z * 0.0028);
    }

    double width(double x, double z) {
        return width.noise(x * 0.03, 3.3, z * 0.03);
    }
}
