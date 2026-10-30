package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.block.EngineTerminalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Interloper.MODID);

    public static final RegistryObject<Block> ENGINE_TERMINAL = BLOCKS.register("engine_terminal",
            () -> new EngineTerminalBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                    .strength(4.0F, 12.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
                    .lightLevel(state -> 4)));

    public static final RegistryObject<Item> ENGINE_TERMINAL_ITEM = ModItems.ITEMS.register("engine_terminal",
            () -> new BlockItem(ENGINE_TERMINAL.get(), new Item.Properties()));

    private ModBlocks() {
    }
}
