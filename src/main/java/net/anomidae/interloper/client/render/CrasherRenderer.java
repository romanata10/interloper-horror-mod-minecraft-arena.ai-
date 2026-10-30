package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.BrokenVillagerModel;
import net.anomidae.interloper.entity.CrasherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CrasherRenderer extends MobRenderer<CrasherEntity, BrokenVillagerModel> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/crasher.png");

    public CrasherRenderer(EntityRendererProvider.Context context) {
        super(context, new BrokenVillagerModel(context.bakeLayer(BrokenVillagerModel.LAYER)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(CrasherEntity entity) {
        return TEXTURE;
    }
}
