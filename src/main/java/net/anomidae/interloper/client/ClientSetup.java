package net.anomidae.interloper.client;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.model.InterloperModel;
import net.anomidae.interloper.client.model.SkyghostModel;
import net.anomidae.interloper.client.render.InterloperRenderer;
import net.anomidae.interloper.client.render.SkyghostRenderer;
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
        event.registerEntityRenderer(ModEntities.SKYGHOST.get(), SkyghostRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(InterloperModel.LAYER, InterloperModel::createBodyLayer);
        event.registerLayerDefinition(SkyghostModel.LAYER, SkyghostModel::createBodyLayer);
    }

    private ClientSetup() {
    }
}
