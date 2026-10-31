package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.BoxModel;
import net.anomidae.interloper.entity.VoidProbeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VoidProbeRenderer extends MobRenderer<VoidProbeEntity, BoxModel<VoidProbeEntity>> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/void_probe.png");

    public VoidProbeRenderer(EntityRendererProvider.Context context) {
        super(context, new BoxModel<>(context.bakeLayer(BoxModel.LAYER)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidProbeEntity entity) {
        return TEXTURE;
    }
}
