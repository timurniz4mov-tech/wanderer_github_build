package com.wanderer.mod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WandererRenderer extends HumanoidMobRenderer<WandererEntity, PlayerModel<WandererEntity>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation(WandererMod.MODID, "textures/entity/wanderer_0.png"),
            new ResourceLocation(WandererMod.MODID, "textures/entity/wanderer_1.png")
    };

    public WandererRenderer(EntityRendererProvider.Context context) {
        // Скины с тонкими руками (Alex) -> slim модель
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WandererEntity entity) {
        int skin = entity.getSkin();
        return TEXTURES[(skin == 1) ? 1 : 0];
    }
}
