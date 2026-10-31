package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.item.AnomalyScannerItem;
import net.anomidae.interloper.item.ConsoleItem;
import net.anomidae.interloper.item.DemoTapeItem;
import net.anomidae.interloper.item.InterloperUmbrellaItem;
import net.anomidae.interloper.item.WhiteDoorKeyItem;
import net.anomidae.interloper.item.SkyboxFragmentItem;
import net.anomidae.interloper.item.UmbrellaItem;
import net.anomidae.interloper.util.DemoType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Interloper.MODID);

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Interloper.MODID);

    public static final RegistryObject<Item> CONSOLE = ITEMS.register("console",
            () -> new ConsoleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DEMO_TAPE = ITEMS.register("demo_tape",
            () -> new DemoTapeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> WHITE_DOOR_KEY = ITEMS.register("white_door_key",
            () -> new WhiteDoorKeyItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> INTERLOPER_UMBRELLA = ITEMS.register("interloper_umbrella",
            () -> new InterloperUmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SKYBOX_FRAGMENT = ITEMS.register("skybox_fragment",
            () -> new SkyboxFragmentItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ANOMALY_SCANNER = ITEMS.register("anomaly_scanner",
            () -> new AnomalyScannerItem(new Item.Properties().stacksTo(1).durability(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> UMBRELLA = ITEMS.register("umbrella",
            () -> new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> INTERLOPER_SPAWN_EGG = ITEMS.register("interloper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INTERLOPER, 0x0B0B0F, 0xE8E8E8,
                    new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> INTERLOPER_CORRUPT_SPAWN_EGG = ITEMS.register("interloper_corrupt_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INTERLOPER_CORRUPT, 0xFF00FF, 0x0B0B0F,
                    new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SKYGHOST_SPAWN_EGG = ITEMS.register("skyghost_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SKYGHOST, 0x131318, 0xF2F2F2,
                    new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CORRUPTED_NPC_SPAWN_EGG = ITEMS.register("corrupted_npc_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CORRUPTED_NPC, 0x3A4A32, 0x7A8A6A,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> VRAD_SPAWN_EGG = ITEMS.register("vrad_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.VRAD, 0xFF0000, 0x000000,
                    new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CRASHER_SPAWN_EGG = ITEMS.register("crasher_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CRASHER, 0x6B5A3A, 0x2A2A2A,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CONE_SPAWN_EGG = ITEMS.register("cone_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CONE, 0x808080, 0x4A4A4A,
                    new Item.Properties()));

    public static final RegistryObject<Item> BROKEN_CHARACTER_SPAWN_EGG = ITEMS.register("broken_character_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BROKEN_CHARACTER, 0x9A9A9A, 0x2A2A2A,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> DMNPC_SPAWN_EGG = ITEMS.register("dmnpc_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DM_NPC, 0x0A0A12, 0x9A4AFF,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.interloper"))
                    .icon(() -> new ItemStack(CONSOLE.get()))
                    .displayItems((params, output) -> {
                        output.accept(CONSOLE.get());
                        output.accept(ModBlocks.ENGINE_TERMINAL.get());
                        output.accept(ModBlocks.SOURCE_FLOOR.get());
                        for (DemoType type : DemoType.values()) {
                            output.accept(demoTape(type));
                        }
                        output.accept(SKYBOX_FRAGMENT.get());
                        output.accept(ANOMALY_SCANNER.get());
                        output.accept(UMBRELLA.get());
                        output.accept(INTERLOPER_SPAWN_EGG.get());
                        output.accept(INTERLOPER_CORRUPT_SPAWN_EGG.get());
                        output.accept(SKYGHOST_SPAWN_EGG.get());
                        output.accept(CORRUPTED_NPC_SPAWN_EGG.get());
                        output.accept(VRAD_SPAWN_EGG.get());
                        output.accept(CRASHER_SPAWN_EGG.get());
                        output.accept(CONE_SPAWN_EGG.get());
                        output.accept(DMNPC_SPAWN_EGG.get());
                        output.accept(BROKEN_CHARACTER_SPAWN_EGG.get());
                        output.accept(ModBlocks.XBOX_GRASS.get());
                        output.accept(ModBlocks.XBOX_DIRT.get());
                        output.accept(ModBlocks.XBOX_PANEL.get());
                        output.accept(ModBlocks.XBOX_GLOW.get());
                        output.accept(ModBlocks.PORTAL_PANEL.get());
                        output.accept(ModBlocks.PORTAL_FLOOR.get());
                        output.accept(ModBlocks.MISSING_TEXTURE.get());
                        output.accept(ModBlocks.WHITE_WALL.get());
                        output.accept(ModBlocks.FALL_HOLE.get());
                        output.accept(ModBlocks.WHITE_DOOR.get());
                        output.accept(ModBlocks.WHITE_LOCK.get());
                        output.accept(WHITE_DOOR_KEY.get());
                        output.accept(INTERLOPER_UMBRELLA.get());
                    })
                    .build());

    public static ItemStack demoTape(DemoType type) {
        ItemStack stack = new ItemStack(DEMO_TAPE.get());
        type.write(stack);
        return stack;
    }

    private ModItems() {
    }
}
