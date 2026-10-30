package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.SkyghostEntity;
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
 * A head with glasses. It should not have glasses.
 */
public class SkyghostModel extends EntityModel<SkyghostEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("skyghost"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public SkyghostModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-3.2F, -5.5F, -4.4F, 2.7F, 1.6F, 1.0F)
                        .texOffs(42, 0).addBox(0.5F, -5.5F, -4.4F, 2.7F, 1.6F, 1.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        root.addOrReplaceChild("shoulders",
                CubeListBuilder.create().texOffs(0, 20).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SkyghostEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.head.zRot = Mth.sin(ageInTicks * 0.3F) * 0.06F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
