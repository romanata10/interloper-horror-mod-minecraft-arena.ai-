package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.DmNpcEntity;
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
 * dm_npc. Long limbs, correct walk cycle. The only thing in the void that moves
 * like something alive.
 */
public class DmNpcModel extends EntityModel<DmNpcEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("dmnpc"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DmNpcModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-3.0F, 0.0F, -1.5F, 6.0F, 14.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 18.0F, 2.0F),
                PartPose.offset(-4.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 18.0F, 2.0F),
                PartPose.offset(4.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.2F, 0.0F, -1.2F, 2.4F, 16.0F, 2.4F),
                PartPose.offset(-1.6F, 14.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).addBox(-1.2F, 0.0F, -1.2F, 2.4F, 16.0F, 2.4F),
                PartPose.offset(1.6F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(DmNpcEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float swing = limbSwing * 0.6662F;
        this.rightLeg.xRot = Mth.cos(swing) * 1.25F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(swing + (float) Math.PI) * 1.25F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(swing + (float) Math.PI) * 0.9F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(swing) * 0.9F * limbSwingAmount;

        // a small, very human bounce in the shoulders
        this.body.y = Mth.cos(swing * 2.0F) * 0.4F * limbSwingAmount;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.head.zRot = Mth.cos(swing) * 0.03F * limbSwingAmount;
        this.rightArm.zRot = 0.05F;
        this.leftArm.zRot = -0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
