package com.wanderer.mod.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Ставит лагерь в указанной точке. Для проверки: /place feature wanderer:camp */
public class CampFeature extends Feature<NoneFeatureConfiguration> {
    public CampFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        int gy = TerrainUtil.groundY(level, o.getX(), o.getZ());
        if (gy == TerrainUtil.NONE) gy = o.getY() - 1;
        CampBuilder.build(level, o.getX(), gy, o.getZ(), ctx.random());
        return true;
    }
}
