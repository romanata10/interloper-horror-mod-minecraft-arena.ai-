package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.DmNpcModel;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DmNpcRenderer extends MobRenderer<DmNpcEntity, DmNpcModel> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/dmnpc.png");

    public DmNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new DmNpcModel(context.bakeLayer(DmNpcModel.LAYER)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(DmNpcEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(DmNpcEntity entity, net.minecraft.core.BlockPos pos) {
        return 8;
    }
}
