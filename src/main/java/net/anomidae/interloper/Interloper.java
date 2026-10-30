package net.anomidae.interloper;

import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * INTERLOPER - a horror mod for Minecraft Forge 1.20.1 based on the INTERLOPER ARG by Anomidae.
 *
 * The engine remembers. The skybox is watching.
 */
@Mod(Interloper.MODID)
public class Interloper {
    public static final String MODID = "interloper";
    public static final Logger LOGGER = LoggerFactory.getLogger("Interloper");

    public Interloper() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.TABS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModSounds.SOUNDS.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, InterloperConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("INTERLOPER loaded. If you can read this, the demo has already started.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
