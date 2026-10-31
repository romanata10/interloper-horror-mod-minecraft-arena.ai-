package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.block.EngineTerminalBlock;
import net.anomidae.interloper.block.WhiteDoorBlock;
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

    /**
     * The only thing the Source Void is made of. No detail, no variation, no relief.
     */
    public static final RegistryObject<Block> SOURCE_FLOOR = BLOCKS.register("source_floor",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE)));

    // ---------------------------------------------------------------- xbox 360 dreams

    public static final RegistryObject<Block> XBOX_GRASS = BLOCKS.register("xbox_grass",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .strength(0.6F).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> XBOX_DIRT = BLOCKS.register("xbox_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
                    .strength(0.5F).sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> XBOX_PANEL = BLOCKS.register("xbox_panel",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_STONE)
                    .strength(3.0F, 9.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> XBOX_GLOW = BLOCKS.register("xbox_glow",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLOWSTONE)
                    .strength(0.5F).lightLevel(state -> 15).sound(SoundType.GLASS)));

    public static final RegistryObject<Block> PORTAL_PANEL = BLOCKS.register("portal_panel",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ)
                    .strength(2.0F, 6.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> PORTAL_FLOOR = BLOCKS.register("portal_floor",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)
                    .strength(2.5F, 6.0F).sound(SoundType.STONE)));

    /** A block that never got a texture. Purple and black, exactly like the engine's warning. */
    public static final RegistryObject<Block> MISSING_TEXTURE = BLOCKS.register("missing_texture",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(0.4F).lightLevel(state -> 7).sound(SoundType.GLASS)));

    // ---------------------------------------------------------------- white door bsp

    public static final RegistryObject<Block> WHITE_WALL = BLOCKS.register("white_wall",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.QUARTZ_BLOCK)
                    .strength(2.0F, 6.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> WHITE_DOOR = BLOCKS.register("white_door",
            () -> new WhiteDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR)
                    .strength(3.0F, 1200.0F).sound(SoundType.METAL), false));

    public static final RegistryObject<Block> WHITE_LOCK = BLOCKS.register("white_lock",
            () -> new WhiteDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0F, 1200.0F).sound(SoundType.METAL), true));

    public static final RegistryObject<Item> ENGINE_TERMINAL_ITEM = ModItems.ITEMS.register("engine_terminal",
            () -> new BlockItem(ENGINE_TERMINAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> SOURCE_FLOOR_ITEM = ModItems.ITEMS.register("source_floor",
            () -> new BlockItem(SOURCE_FLOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> XBOX_GRASS_ITEM = ModItems.ITEMS.register("xbox_grass",
            () -> new BlockItem(XBOX_GRASS.get(), new Item.Properties()));

    public static final RegistryObject<Item> XBOX_DIRT_ITEM = ModItems.ITEMS.register("xbox_dirt",
            () -> new BlockItem(XBOX_DIRT.get(), new Item.Properties()));

    public static final RegistryObject<Item> XBOX_PANEL_ITEM = ModItems.ITEMS.register("xbox_panel",
            () -> new BlockItem(XBOX_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<Item> XBOX_GLOW_ITEM = ModItems.ITEMS.register("xbox_glow",
            () -> new BlockItem(XBOX_GLOW.get(), new Item.Properties()));

    public static final RegistryObject<Item> PORTAL_PANEL_ITEM = ModItems.ITEMS.register("portal_panel",
            () -> new BlockItem(PORTAL_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<Item> PORTAL_FLOOR_ITEM = ModItems.ITEMS.register("portal_floor",
            () -> new BlockItem(PORTAL_FLOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> MISSING_TEXTURE_ITEM = ModItems.ITEMS.register("missing_texture",
            () -> new BlockItem(MISSING_TEXTURE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WHITE_WALL_ITEM = ModItems.ITEMS.register("white_wall",
            () -> new BlockItem(WHITE_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> WHITE_DOOR_ITEM = ModItems.ITEMS.register("white_door",
            () -> new BlockItem(WHITE_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> WHITE_LOCK_ITEM = ModItems.ITEMS.register("white_lock",
            () -> new BlockItem(WHITE_LOCK.get(), new Item.Properties()));

    private ModBlocks() {
    }
}
