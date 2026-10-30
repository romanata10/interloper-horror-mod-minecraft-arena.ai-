package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.SkyghostModel;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SkyghostRenderer extends MobRenderer<SkyghostEntity, SkyghostModel> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/skyghost.png");

    public SkyghostRenderer(EntityRendererProvider.Context context) {
        super(context, new SkyghostModel(context.bakeLayer(SkyghostModel.LAYER)), 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SkyghostEntity entity) {
        return TEXTURE;
    }
}
