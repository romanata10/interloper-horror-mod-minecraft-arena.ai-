package net.anomidae.interloper.client.render;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.TposeModel;
import net.anomidae.interloper.entity.BrokenCharacterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Each variant is a different exported character, all of them in the same wrong pose.
 */
public class BrokenCharacterRenderer extends MobRenderer<BrokenCharacterEntity, TposeModel<BrokenCharacterEntity>> {
    private static final ResourceLocation[] TEXTURES = {
            Interloper.id("textures/entity/broken_scientist.png"),
            Interloper.id("textures/entity/broken_citizen.png"),
            Interloper.id("textures/entity/broken_soldier.png"),
            Interloper.id("textures/entity/broken_robot.png")
    };

    public BrokenCharacterRenderer(EntityRendererProvider.Context context) {
        super(context, new TposeModel<>(context.bakeLayer(TposeModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BrokenCharacterEntity entity) {
        return TEXTURES[Math.floorMod(entity.variant(), TEXTURES.length)];
    }
}
