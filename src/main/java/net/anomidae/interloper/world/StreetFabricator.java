package net.anomidae.interloper.world;

import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.util.Loot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A small street: two rows of houses, a road, and one wall in the middle that
 * sometimes has a man with an umbrella painted on it.
 */
public final class StreetFabricator {
    public static void build(ServerLevel level, BlockPos center, RandomSource random) {
        BlockState road = ModBlocks.XBOX_PANEL.get().defaultBlockState();
        BlockState wall = ModBlocks.WHITE_WALL.get().defaultBlockState();
        int y = center.getY();

        // clear the ground and lay the road
        for (int x = -10; x <= 10; x++) {
            for (int z = -12; z <= 12; z++) {
                for (int dy = 0; dy < 8; dy++) {
                    place(level, center.getX() + x, y + dy, center.getZ() + z, Blocks.AIR.defaultBlockState());
                }
                place(level, center.getX() + x, y - 1, center.getZ() + z, road);
                for (int dy = 1; dy >= -3; dy--) {
                    if (level.getBlockState(new BlockPos(center.getX() + x, y + dy - 1, center.getZ() + z)).isAir()) {
                        place(level, center.getX() + x, y + dy, center.getZ() + z,
                                ModBlocks.XBOX_DIRT.get().defaultBlockState());
                    }
                }
            }
        }

        // houses on both sides of the road
        house(level, center.offset(-9, 0, -8), 7, 8, wall, random);
        house(level, center.offset(-9, 0, 2), 7, 7, wall, random);
        house(level, center.offset(3, 0, -8), 7, 8, wall, random);
        house(level, center.offset(3, 0, 2), 7, 7, wall, random);

        // the wall between the houses - this is where the graffiti goes
        for (int x = -1; x <= 1; x++) {
            for (int dy = 0; dy < 3; dy++) {
                place(level, center.getX() + x, y + dy, center.getZ() - 7, wall);
            }
        }

        // a chest in the last house, with a note in it
        Loot.chest(level, center.offset(6, 0, 6), random, Loot.Kind.BREAD_NOTE);

        // street lamps out of dashboard glow
        for (int z = -6; z <= 6; z += 6) {
            place(level, center.getX() - 2, y, center.getZ() + z, road);
            place(level, center.getX() - 2, y + 1, center.getZ() + z, road);
            place(level, center.getX() - 2, y + 2, center.getZ() + z,
                    ModBlocks.XBOX_GLOW.get().defaultBlockState());
        }
    }

    private static void house(ServerLevel level, BlockPos origin, int width, int depth, BlockState wall,
                              RandomSource random) {
        int y = origin.getY();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                place(level, origin.getX() + x, y - 1, origin.getZ() + z,
                        ModBlocks.PORTAL_FLOOR.get().defaultBlockState());
            }
        }
        for (int h = 0; h < 4; h++) {
            for (int x = 0; x < width; x++) {
                place(level, origin.getX() + x, y + h, origin.getZ(), wall);
                place(level, origin.getX() + x, y + h, origin.getZ() + depth - 1, wall);
            }
            for (int z = 0; z < depth; z++) {
                place(level, origin.getX(), y + h, origin.getZ() + z, wall);
                place(level, origin.getX() + width - 1, y + h, origin.getZ() + z, wall);
            }
        }
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                place(level, origin.getX() + x, y + 4, origin.getZ() + z, wall);
            }
        }
        // doorway facing the road
        int door = width / 2;
        int doorZ = origin.getZ() < 0 ? origin.getZ() + depth - 1 : origin.getZ();
        place(level, origin.getX() + door, y, doorZ, Blocks.AIR.defaultBlockState());
        place(level, origin.getX() + door, y + 1, doorZ, Blocks.AIR.defaultBlockState());
        // window
        place(level, origin.getX() + 1, y + 2, doorZ, Blocks.AIR.defaultBlockState());
        place(level, origin.getX() + 1, y + 2, origin.getZ() + depth / 2, Blocks.GLASS.defaultBlockState());
    }

    private static void place(ServerLevel level, int x, int y, int z, BlockState state) {
        if (y < level.getMinBuildHeight() + 1 || y > level.getMaxBuildHeight() - 2) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunkAt(pos)) {
            return;
        }
        level.setBlock(pos, state, 2);
    }

    private StreetFabricator() {
    }
}
