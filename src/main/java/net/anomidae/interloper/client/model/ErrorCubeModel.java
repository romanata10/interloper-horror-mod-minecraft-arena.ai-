package net.anomidae.interloper.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.VradEntity;
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
 * A red error cube with a couple of detached faces orbiting it.
 */
public class ErrorCubeModel extends EntityModel<VradEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Interloper.id("vrad"), "main");

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart shardA;
    private final ModelPart shardB;

    public ErrorCubeModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.shardA = root.getChild("shard_a");
        this.shardB = root.getChild("shard_b");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("shard_a",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.4F, 0.5F, 0.2F));
        root.addOrReplaceChild("shard_b",
                CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, -0.3F, -0.7F, -0.35F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(VradEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.core.yRot = ageInTicks * 0.35F;
        this.core.xRot = Mth.sin(ageInTicks * 0.1F) * 0.4F;
        this.shardA.yRot = -ageInTicks * 0.5F;
        this.shardB.yRot = ageInTicks * 0.7F;
        this.shardB.xRot = Mth.cos(ageInTicks * 0.2F) * 0.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
