package net.anomidae.interloper.event;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::init);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.INTERLOPER.get(), InterloperEntity.createAttributes().build());
        event.put(ModEntities.SKYGHOST.get(), SkyghostEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.INTERLOPER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                InterloperEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.SKYGHOST.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SkyghostEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private ModBusEvents() {
    }
}
