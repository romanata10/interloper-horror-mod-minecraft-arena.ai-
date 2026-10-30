package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.InterloperEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Three blocks of tall, thin nothing under an umbrella.
 */
public class InterloperModel<T extends InterloperEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("interloper"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public InterloperModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-3.0F, -5.0F, -4.4F, 2.0F, 1.0F, 1.0F)
                        .texOffs(38, 0).addBox(1.0F, -5.0F, -4.4F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(40, 8).addBox(-3.0F, -1.0F, -4.2F, 6.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 18.0F, 4.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 26.0F, 3.0F),
                PartPose.offset(5.5F, -14.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 26.0F, 3.0F),
                PartPose.offset(-5.5F, -14.0F, 0.0F));

        rightArm.addOrReplaceChild("umbrella",
                CubeListBuilder.create()
                        .texOffs(26, 16).addBox(-12.0F, 0.0F, -12.0F, 24.0F, 1.0F, 24.0F)
                        .texOffs(0, 50).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 22.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(48, 26).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F),
                PartPose.offset(2.2F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(48, 26).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F),
                PartPose.offset(-2.2F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float sway = Mth.cos(ageInTicks * 0.05F) * 0.05F;
        this.leftArm.zRot = 0.06F + sway;
        this.rightArm.zRot = -0.06F - sway;

        if (entity.stareTicks() > 0) {
            this.head.zRot = Mth.sin(ageInTicks * 1.7F) * 0.12F;
            this.head.yRot += Mth.sin(ageInTicks * 0.9F) * 0.25F;
        } else {
            this.head.zRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
