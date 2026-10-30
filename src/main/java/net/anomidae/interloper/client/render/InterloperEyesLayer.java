package net.anomidae.interloper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.InterloperModel;
import net.anomidae.interloper.entity.InterloperEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Emissive eyes. They are the only warm thing about it.
 */
public class InterloperEyesLayer extends RenderLayer<InterloperEntity, InterloperModel<InterloperEntity>> {
    private static final ResourceLocation EYES = Interloper.id("textures/entity/interloper_eyes.png");

    public InterloperEyesLayer(RenderLayerParent<InterloperEntity, InterloperModel<InterloperEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       InterloperEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(EYES));
        this.getParentModel().renderToBuffer(poseStack, consumer, 15728640, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}
