package net.anomidae.interloper.util;

import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Chests, books and the things that are inside them.
 */
public final class Loot {
    public static final String BOOK_TITLE = "the exit is white door and key";
    public static final String SKYBOX_BOOK_TITLE = "the skybox is jail for skybox just bread for skyghost";

    /**
     * A written book that tells you the only useful thing anyone ever wrote down here.
     */
    public static ItemStack exitBook() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("title", BOOK_TITLE);
        tag.putString("author", "?");
        tag.putByte("resolved", (byte) 1);
        ListTag pages = new ListTag();
        pages.add(page("the exit is white door and key"));
        pages.add(page("do not read the other pages"));
        pages.add(page("sv_cheats is not yours"));
        tag.put("pages", pages);
        return stack;
    }

    /**
     * The other book. Whoever wrote it knew what the skybox is for.
     */
    public static ItemStack skyboxBook() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("title", SKYBOX_BOOK_TITLE);
        tag.putString("author", "?");
        tag.putByte("resolved", (byte) 1);
        ListTag pages = new ListTag();
        pages.add(page("the skybox is jail for skybox"));
        pages.add(page("just bread for skyghost"));
        pages.add(page("feed it and the wall lets one shard out"));
        tag.put("pages", pages);
        return stack;
    }

    private static StringTag page(String text) {
        return StringTag.valueOf(Component.Serializer.toJson(Component.literal(text)));
    }

    /**
     * Places a chest and fills it. Used by the structure builders.
     */
    public static void chest(ServerLevel level, BlockPos pos, RandomSource random, Kind kind) {
        if (!level.hasChunkAt(pos)) {
            return;
        }
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) {
            return;
        }

        switch (kind) {
            case VOID_CACHE -> {
                container.setItem(4, exitBook());
                int blocks = 3 + random.nextInt(5);
                container.setItem(0, new ItemStack(ModBlocks.MISSING_TEXTURE.get(), blocks));
                if (random.nextBoolean()) {
                    container.setItem(1, new ItemStack(ModBlocks.SOURCE_FLOOR.get(), 4 + random.nextInt(9)));
                }
                if (random.nextInt(3) == 0) {
                    container.setItem(2, new ItemStack(Items.ROTTEN_FLESH, 2 + random.nextInt(4)));
                }
            }
            case WHITE_DOOR_KEY -> {
                container.setItem(13, new ItemStack(ModItems.WHITE_DOOR_KEY.get()));
                container.setItem(0, new ItemStack(ModBlocks.MISSING_TEXTURE.get(), 2));
                if (random.nextBoolean()) {
                    container.setItem(1, exitBook());
                }
            }
            case BREAD_NOTE -> {
                container.setItem(4, skyboxBook());
                container.setItem(0, new ItemStack(Items.BREAD, 6 + random.nextInt(6)));
                if (random.nextBoolean()) {
                    container.setItem(1, new ItemStack(ModItems.SKYBOX_SHARD.get(), 1 + random.nextInt(2)));
                }
            }
            case FLASHLIGHT_NUKE -> {
                // the nuke glitch, in a chest that a broken block fell on
                container.setItem(13, new ItemStack(ModItems.FLASHLIGHT_NUKE.get()));
                container.setItem(4, new ItemStack(ModItems.FLASHLIGHT_NUKE.get()));
                int debris = 2 + random.nextInt(4);
                container.setItem(0, new ItemStack(ModItems.BROKEN_NPC_DEBRIS.get(), debris));
                if (random.nextInt(3) == 0) {
                    container.setItem(1, new ItemStack(ModItems.DM_CODE_FRAGMENT.get(), 1 + random.nextInt(3)));
                }
                container.setItem(2, new ItemStack(ModBlocks.MISSING_TEXTURE.get(), 3 + random.nextInt(4)));
            }
            case SALVAGE -> {
                ItemStack[] pool = {
                        new ItemStack(Items.BREAD, 1 + random.nextInt(3)),
                        new ItemStack(Items.TORCH, 4 + random.nextInt(8)),
                        new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(3)),
                        new ItemStack(Items.STRING, 2 + random.nextInt(4)),
                        new ItemStack(ModBlocks.PORTAL_PANEL.get(), 4 + random.nextInt(6)),
                        new ItemStack(ModBlocks.XBOX_GRASS.get(), 2 + random.nextInt(4))
                };
                for (int i = 0; i < 3; i++) {
                    container.setItem(random.nextInt(27), pool[random.nextInt(pool.length)].copy());
                }
            }
        }
    }

    public enum Kind {
        VOID_CACHE,
        WHITE_DOOR_KEY,
        SALVAGE,
        BREAD_NOTE,
        FLASHLIGHT_NUKE
    }

    private Loot() {
    }
}
