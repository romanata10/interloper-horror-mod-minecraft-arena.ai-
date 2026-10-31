package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.TposeModel;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CorruptedNpcRenderer extends MobRenderer<CorruptedNpcEntity, TposeModel<CorruptedNpcEntity>> {
    private static final ResourceLocation TEXTURE = Interloper.id("textures/entity/corrupted_npc.png");

    public CorruptedNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new TposeModel<>(context.bakeLayer(TposeModel.LAYER)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(CorruptedNpcEntity entity) {
        return TEXTURE;
    }
}
