package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.item.AnomalyScannerItem;
import net.anomidae.interloper.item.DemoTapeItem;
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

    public static final RegistryObject<Item> DEMO_TAPE = ITEMS.register("demo_tape",
            () -> new DemoTapeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SKYBOX_FRAGMENT = ITEMS.register("skybox_fragment",
            () -> new SkyboxFragmentItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ANOMALY_SCANNER = ITEMS.register("anomaly_scanner",
            () -> new AnomalyScannerItem(new Item.Properties().stacksTo(1).durability(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> UMBRELLA = ITEMS.register("umbrella",
            () -> new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> INTERLOPER_SPAWN_EGG = ITEMS.register("interloper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INTERLOPER, 0x0B0B0F, 0xE8E8E8,
                    new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SKYGHOST_SPAWN_EGG = ITEMS.register("skyghost_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SKYGHOST, 0x131318, 0xF2F2F2,
                    new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.interloper"))
                    .icon(() -> new ItemStack(DEMO_TAPE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.ENGINE_TERMINAL.get());
                        for (DemoType type : DemoType.values()) {
                            output.accept(demoTape(type));
                        }
                        output.accept(SKYBOX_FRAGMENT.get());
                        output.accept(ANOMALY_SCANNER.get());
                        output.accept(UMBRELLA.get());
                        output.accept(INTERLOPER_SPAWN_EGG.get());
                        output.accept(SKYGHOST_SPAWN_EGG.get());
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
