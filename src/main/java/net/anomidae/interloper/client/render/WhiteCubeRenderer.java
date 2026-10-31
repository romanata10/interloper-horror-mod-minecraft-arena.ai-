package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.BoxModel;
import net.anomidae.interloper.entity.WhiteCubeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WhiteCubeRenderer extends MobRenderer<WhiteCubeEntity, BoxModel<WhiteCubeEntity>> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/white_cube.png");

    public WhiteCubeRenderer(EntityRendererProvider.Context context) {
        super(context, new BoxModel<>(context.bakeLayer(BoxModel.LAYER)), 0.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(WhiteCubeEntity entity) {
        return TEXTURE;
    }
}
