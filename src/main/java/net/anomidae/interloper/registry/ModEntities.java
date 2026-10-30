package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.ConeEntity;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.anomidae.interloper.entity.CrasherEntity;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.entity.VradEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Interloper.MODID);

    public static final RegistryObject<EntityType<InterloperEntity>> INTERLOPER = ENTITY_TYPES.register("interloper",
            () -> EntityType.Builder.of(InterloperEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 3.0F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build(Interloper.id("interloper").toString()));

    public static final RegistryObject<EntityType<InterloperCorruptEntity>> INTERLOPER_CORRUPT =
            ENTITY_TYPES.register("interloper_corrupt",
                    () -> EntityType.Builder.of(InterloperCorruptEntity::new, MobCategory.MONSTER)
                            .sized(0.95F, 3.8F)
                            .clientTrackingRange(16)
                            .fireImmune()
                            .build(Interloper.id("interloper_corrupt").toString()));

    public static final RegistryObject<EntityType<SkyghostEntity>> SKYGHOST = ENTITY_TYPES.register("skyghost",
            () -> EntityType.Builder.of(SkyghostEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.9F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build(Interloper.id("skyghost").toString()));

    public static final RegistryObject<EntityType<CorruptedNpcEntity>> CORRUPTED_NPC = ENTITY_TYPES.register("corrupted_npc",
            () -> EntityType.Builder.of(CorruptedNpcEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(Interloper.id("corrupted_npc").toString()));

    public static final RegistryObject<EntityType<VradEntity>> VRAD = ENTITY_TYPES.register("vrad",
            () -> EntityType.Builder.of(VradEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 1.1F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build(Interloper.id("vrad").toString()));

    public static final RegistryObject<EntityType<CrasherEntity>> CRASHER = ENTITY_TYPES.register("crasher",
            () -> EntityType.Builder.of(CrasherEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.9F)
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(Interloper.id("crasher").toString()));

    public static final RegistryObject<EntityType<ConeEntity>> CONE = ENTITY_TYPES.register("cone",
            () -> EntityType.Builder.of(ConeEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.7F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Interloper.id("cone").toString()));

    public static final RegistryObject<EntityType<DmNpcEntity>> DM_NPC = ENTITY_TYPES.register("dmnpc",
            () -> EntityType.Builder.of(DmNpcEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.9F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build(Interloper.id("dmnpc").toString()));

    private ModEntities() {
    }
}
