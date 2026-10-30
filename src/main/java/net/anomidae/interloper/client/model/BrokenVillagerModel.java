package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.CrasherEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * A villager whose skeleton was welded together by someone who had never seen
 * a skeleton. Nothing lines up, nothing moves.
 */
public class BrokenVillagerModel extends EntityModel<CrasherEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("crasher"), "main");

    private final ModelPart root;

    public BrokenVillagerModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(24, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.5F, 1.5F, 0.0F, 0.55F, 0.9F, 1.35F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 10.0F, 4.0F)
                        .texOffs(16, 40).addBox(-5.0F, 3.0F, -2.5F, 10.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.4F, 1.0F, 0.0F, 0.12F, 0.0F, 0.4F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offsetAndRotation(-4.0F, 1.5F, 0.0F, -1.9F, 0.2F, -1.4F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offsetAndRotation(4.0F, 3.0F, 0.0F, 0.9F, -0.6F, 2.1F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.9F, 0.0F, 0.6F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(2.0F, 13.0F, 0.0F, -0.5F, 0.0F, -0.9F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(CrasherEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // The rig is gone.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
