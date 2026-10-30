package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
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

    public static final RegistryObject<EntityType<SkyghostEntity>> SKYGHOST = ENTITY_TYPES.register("skyghost",
            () -> EntityType.Builder.of(SkyghostEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.1F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build(Interloper.id("skyghost").toString()));

    private ModEntities() {
    }
}
