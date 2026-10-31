package net.anomidae.interloper.event;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.BrokenCharacterEntity;
import net.anomidae.interloper.entity.ConeEntity;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.anomidae.interloper.entity.CrasherEntity;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.entity.VradEntity;
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
        event.put(ModEntities.INTERLOPER_CORRUPT.get(), InterloperEntity.createAttributes().build());
        event.put(ModEntities.SKYGHOST.get(), SkyghostEntity.createAttributes().build());
        event.put(ModEntities.CORRUPTED_NPC.get(), CorruptedNpcEntity.createAttributes().build());
        event.put(ModEntities.VRAD.get(), VradEntity.createAttributes().build());
        event.put(ModEntities.CRASHER.get(), CrasherEntity.createAttributes().build());
        event.put(ModEntities.CONE.get(), ConeEntity.createAttributes().build());
        event.put(ModEntities.DM_NPC.get(), DmNpcEntity.createAttributes().build());
        event.put(ModEntities.BROKEN_CHARACTER.get(), BrokenCharacterEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.INTERLOPER.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                InterloperEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.SKYGHOST.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SkyghostEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        // The rest of the void walks the overworld too, just very rarely and in the dark.
        registerVoidMob(event, ModEntities.CORRUPTED_NPC.get());
        registerVoidMob(event, ModEntities.VRAD.get());
        registerVoidMob(event, ModEntities.CRASHER.get());
        registerVoidMob(event, ModEntities.CONE.get());
        registerVoidMob(event, ModEntities.DM_NPC.get());
        registerVoidMob(event, ModEntities.BROKEN_CHARACTER.get());
    }

    private static <T extends net.minecraft.world.entity.Entity> void registerVoidMob(
            SpawnPlacementRegisterEvent event,
            net.minecraft.world.entity.EntityType<T> type) {
        event.register(type,
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, reason, pos, random) -> reason != net.minecraft.world.entity.MobSpawnType.SPAWN_EGG
                        && net.minecraft.world.entity.monster.Monster.checkAnyLightMonsterSpawnRules(
                                entityType, level, reason, pos, random),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private ModBusEvents() {
    }
}
