package net.anomidae.interloper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.InterloperModel;
import net.anomidae.interloper.entity.InterloperEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class InterloperRenderer extends MobRenderer<InterloperEntity, InterloperModel<InterloperEntity>> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/interloper.png");

    public InterloperRenderer(EntityRendererProvider.Context context) {
        super(context, new InterloperModel<>(context.bakeLayer(InterloperModel.LAYER)), 0.7F);
        this.addLayer(new InterloperEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(InterloperEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(InterloperEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // the renderer occasionally loses track of it
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null && (entity.tickCount / 6) % 23 == 0 && entity.distanceTo(player) > 24.0F) {
            return;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
}
