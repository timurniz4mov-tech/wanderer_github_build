package com.wanderer.mod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WandererRenderer extends HumanoidMobRenderer<WandererEntity, PlayerModel<WandererEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(WandererMod.MODID, "textures/entity/wanderer.png");

    public WandererRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WandererEntity entity) {
        return TEXTURE;
    }
}
