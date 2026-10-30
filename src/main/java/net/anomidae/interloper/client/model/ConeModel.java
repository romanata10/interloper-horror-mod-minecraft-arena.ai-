package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.ConeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * A grey cone. Nothing else.
 */
public class ConeModel extends EntityModel<ConeEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("cone"), "main");

    private final ModelPart root;

    public ConeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.offset(0.0F, 25.0F, 0.0F));
        root.addOrReplaceChild("layer1", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("layer2", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("layer3", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("tip", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(ConeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
