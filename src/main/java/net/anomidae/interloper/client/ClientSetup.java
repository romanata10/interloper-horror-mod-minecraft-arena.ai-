package net.anomidae.interloper.client;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.BrokenVillagerModel;
import net.anomidae.interloper.client.model.ConeModel;
import net.anomidae.interloper.client.model.DmNpcModel;
import net.anomidae.interloper.client.model.ErrorCubeModel;
import net.anomidae.interloper.client.model.InterloperModel;
import net.anomidae.interloper.client.model.SkyghostModel;
import net.anomidae.interloper.client.model.TposeModel;
import net.anomidae.interloper.client.render.ConeRenderer;
import net.anomidae.interloper.client.render.CorruptedNpcRenderer;
import net.anomidae.interloper.client.render.CrasherRenderer;
import net.anomidae.interloper.client.render.DmNpcRenderer;
import net.anomidae.interloper.client.render.InterloperCorruptRenderer;
import net.anomidae.interloper.client.render.InterloperRenderer;
import net.anomidae.interloper.client.render.SkyghostRenderer;
import net.anomidae.interloper.client.render.VradRenderer;
import net.anomidae.interloper.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INTERLOPER.get(), InterloperRenderer::new);
        event.registerEntityRenderer(ModEntities.INTERLOPER_CORRUPT.get(), InterloperCorruptRenderer::new);
        event.registerEntityRenderer(ModEntities.SKYGHOST.get(), SkyghostRenderer::new);
        event.registerEntityRenderer(ModEntities.CORRUPTED_NPC.get(), CorruptedNpcRenderer::new);
        event.registerEntityRenderer(ModEntities.VRAD.get(), VradRenderer::new);
        event.registerEntityRenderer(ModEntities.CRASHER.get(), CrasherRenderer::new);
        event.registerEntityRenderer(ModEntities.CONE.get(), ConeRenderer::new);
        event.registerEntityRenderer(ModEntities.DM_NPC.get(), DmNpcRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(InterloperModel.LAYER, InterloperModel::createBodyLayer);
        event.registerLayerDefinition(SkyghostModel.LAYER, SkyghostModel::createBodyLayer);
        event.registerLayerDefinition(TposeModel.LAYER, TposeModel::createBodyLayer);
        event.registerLayerDefinition(BrokenVillagerModel.LAYER, BrokenVillagerModel::createBodyLayer);
        event.registerLayerDefinition(ConeModel.LAYER, ConeModel::createBodyLayer);
        event.registerLayerDefinition(DmNpcModel.LAYER, DmNpcModel::createBodyLayer);
        event.registerLayerDefinition(ErrorCubeModel.LAYER, ErrorCubeModel::createBodyLayer);
    }

    private ClientSetup() {
    }
}
