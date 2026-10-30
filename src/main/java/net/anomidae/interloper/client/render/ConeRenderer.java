package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.ConeModel;
import net.anomidae.interloper.entity.ConeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ConeRenderer extends MobRenderer<ConeEntity, ConeModel> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/cone.png");

    public ConeRenderer(EntityRendererProvider.Context context) {
        super(context, new ConeModel(context.bakeLayer(ConeModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ConeEntity entity) {
        return TEXTURE;
    }
}
