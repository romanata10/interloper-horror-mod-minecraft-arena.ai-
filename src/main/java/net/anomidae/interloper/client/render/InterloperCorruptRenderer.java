package net.anomidae.interloper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.InterloperModel;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * The other Interloper. Same skeleton, taller, and wearing the missing texture.
 */
public class InterloperCorruptRenderer extends MobRenderer<InterloperCorruptEntity, InterloperModel<InterloperCorruptEntity>> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/interloper_corrupt.png");

    public InterloperCorruptRenderer(EntityRendererProvider.Context context) {
        super(context, new InterloperModel<>(context.bakeLayer(InterloperModel.LAYER)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(InterloperCorruptEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(InterloperCorruptEntity entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(1.15F, 1.3F, 1.15F);
    }

    @Override
    protected int getBlockLightLevel(InterloperCorruptEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
