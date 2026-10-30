package net.anomidae.interloper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.ErrorCubeModel;
import net.anomidae.interloper.entity.VradEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * An error that emits its own light.
 */
public class VradRenderer extends MobRenderer<VradEntity, ErrorCubeModel> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/vrad.png");

    public VradRenderer(EntityRendererProvider.Context context) {
        super(context, new ErrorCubeModel(context.bakeLayer(ErrorCubeModel.LAYER)), 0.6F);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(VradEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(VradEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(VradEntity entity, PoseStack poseStack, float partialTicks) {
        float pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTicks) * 0.4F) * 0.06F;
        poseStack.scale(pulse, pulse, pulse);
    }
}
