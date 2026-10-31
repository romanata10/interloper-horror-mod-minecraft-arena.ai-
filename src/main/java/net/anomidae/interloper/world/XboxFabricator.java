package net.anomidae.interloper.world;

import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.util.Loot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds xbox_360_dreams out of its own blocks: gm_construct, portal test chambers,
 * fragments of other Source maps, and one house that has a key in it.
 */
public final class XboxFabricator {
    private static final Map<ServerLevel, Set<Long>> DECORATED = new HashMap<>();
    private static final int FLOOR = XboxDreams.FLOOR_Y;

    public static void clearPad(ServerLevel level, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < 4; dy++) {
                    level.setBlock(new BlockPos(x + dx, y + dy, z + dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    public static void ensureSpawn(ServerLevel level) {
        // plaza with the dashboard glow, right where you land
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                for (int y = 0; y < 6; y++) {
                    level.setBlock(new BlockPos(x, FLOOR + y, z), Blocks.AIR.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(x, FLOOR - 1, z),
                        ModBlocks.XBOX_GRASS.get().defaultBlockState(), 2);
            }
        }
        for (int i = -5; i <= 5; i += 5) {
            for (int j = -5; j <= 5; j += 5) {
                for (int y = 0; y < 4; y++) {
                    level.setBlock(new BlockPos(i, FLOOR + y, j),
                            y == 3 ? ModBlocks.XBOX_GLOW.get().defaultBlockState()
                                    : ModBlocks.XBOX_PANEL.get().defaultBlockState(), 2);
                }
            }
        }

        // the house is always within sight of the spawn - it is the only way out
        gmodHouse(level, 14, FLOOR, 6, RandomSource.create(level.getGameTime() + 77L));
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(Dimensions.XBOX_DREAMS)) {
            return;
        }
        Set<Long> done = DECORATED.computeIfAbsent(level, key -> new HashSet<>());
        for (var player : level.players()) {
            int centerX = player.chunkPosition().x;
            int centerZ = player.chunkPosition().z;
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    int cx = centerX + dx;
                    int cz = centerZ + dz;
                    long key = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
                    if (!done.add(key)) {
                        continue;
                    }
                    RandomSource random = RandomSource.create(mix(key));
                    decorate(level, cx, cz, random);
                }
            }
        }
    }

    public static void reset() {
        DECORATED.clear();
    }

    private static long mix(long key) {
        long value = key * 6364136223846793005L + 1442695040888963407L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        return value ^ (value >>> 29);
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

    private static void decorate(ServerLevel level, int chunkX, int chunkZ, RandomSource random) {
        int originX = chunkX * 16 + 4 + random.nextInt(8);
        int originZ = chunkZ * 16 + 4 + random.nextInt(8);
        int roll = random.nextInt(100);

        if (roll < 18) {
            gmConstruct(level, originX, FLOOR, originZ, random);
        } else if (roll < 34) {
            portalMaze(level, originX, FLOOR, originZ, random);
        } else if (roll < 48) {
            sourceMapFragment(level, originX, FLOOR, originZ, random);
        } else if (roll < 58) {
            gmodHouse(level, originX, FLOOR, originZ, random);
        } else if (roll < 70) {
            brokenCharacters(level, originX, FLOOR, originZ, random);
        } else if (roll < 80) {
            dashboardBlade(level, originX, FLOOR, originZ, random);
        } else if (roll < 88) {
            Loot.chest(level, new BlockPos(originX, FLOOR, originZ), random, Loot.Kind.SALVAGE);
        }
        // everything else is grass and the green gradient of an old dashboard
    }

    // ------------------------------------------------------------------ structures

    private static void gmConstruct(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState floor = ModBlocks.PORTAL_FLOOR.get().defaultBlockState();
        BlockState panel = ModBlocks.XBOX_PANEL.get().defaultBlockState();
        BlockState cube = ModBlocks.MISSING_TEXTURE.get().defaultBlockState();
        int size = 7 + random.nextInt(4);

        for (int ix = -size; ix <= size; ix++) {
            for (int iz = -size; iz <= size; iz++) {
                place(level, x + ix, y - 1, z + iz, floor);
            }
        }
        // ramp
        for (int i = 0; i < 5; i++) {
            for (int w = -2; w <= 2; w++) {
                place(level, x + w, y + i, z + size - i, panel);
            }
        }
        // pillars and the spawn building
        for (int[] corner : new int[][]{{-size + 1, -size + 1}, {size - 1, -size + 1}, {-size + 1, size - 1}}) {
            for (int h = 0; h < 4 + random.nextInt(3); h++) {
                place(level, x + corner[0], y + h, z + corner[1], panel);
            }
        }
        for (int ix = -3; ix <= 3; ix++) {
            for (int iz = -3; iz <= 3; iz++) {
                place(level, x + size - 6 + ix, y, z - size + 6 + iz, panel);
            }
        }
        for (int h = 1; h <= 3; h++) {
            for (int ix = -3; ix <= 3; ix++) {
                place(level, x + size - 6 + ix, y + h, z - size + 3, panel);
            }
            for (int iz = -3; iz <= 3; iz++) {
                place(level, x + size - 9, y + h, z - size + 6 + iz, panel);
            }
        }
        // physical props lying around
        for (int i = 0; i < 6; i++) {
            int px = x + random.nextInt(size * 2) - size;
            int pz = z + random.nextInt(size * 2) - size;
            int h = 1 + random.nextInt(2);
            for (int j = 0; j < h; j++) {
                place(level, px, y + j, pz, cube);
            }
        }
        Loot.chest(level, new BlockPos(x + size - 6, y, z - size + 6), random, Loot.Kind.SALVAGE);
    }

    /**
     * Portal 1 test chamber: white panels, tight corridors, no sky.
     */
    private static void portalMaze(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState panel = ModBlocks.PORTAL_PANEL.get().defaultBlockState();
        BlockState floor = ModBlocks.PORTAL_FLOOR.get().defaultBlockState();
        int size = 9 + random.nextInt(5);
        int cell = 2;

        for (int ix = 0; ix < size; ix++) {
            for (int iz = 0; iz < size; iz++) {
                for (int dx = 0; dx < cell; dx++) {
                    for (int dz = 0; dz < cell; dz++) {
                        place(level, x + ix * cell + dx, y - 1, z + iz * cell + dz, floor);
                    }
                }
            }
        }

        for (int ix = 0; ix < size; ix++) {
            for (int iz = 0; iz < size; iz++) {
                if (ix % 2 == 0 && iz % 2 == 0) {
                    for (int h = 0; h < 4; h++) {
                        for (int dx = 0; dx < cell; dx++) {
                            for (int dz = 0; dz < cell; dz++) {
                                place(level, x + ix * cell + dx, y + h, z + iz * cell + dz, panel);
                            }
                        }
                    }
                    continue;
                }
                if (random.nextInt(3) == 0) {
                    boolean horizontal = ix % 2 == 0;
                    for (int h = 0; h < 4; h++) {
                        if (horizontal) {
                            place(level, x + ix * cell, y + h, z + iz * cell, panel);
                            place(level, x + ix * cell + 1, y + h, z + iz * cell, panel);
                        } else {
                            place(level, x + ix * cell, y + h, z + iz * cell, panel);
                            place(level, x + ix * cell, y + h, z + iz * cell + 1, panel);
                        }
                    }
                }
            }
        }
        place(level, x + cell, y + 3, z + cell, Blocks.LIGHT.defaultBlockState());
        place(level, x + size, y + 3, z + size, Blocks.LIGHT.defaultBlockState());
    }

    /**
     * Fragments of maps that were famous twenty years ago: sand, offices, crates.
     */
    private static void sourceMapFragment(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState panel = ModBlocks.XBOX_PANEL.get().defaultBlockState();
        BlockState dirt = ModBlocks.XBOX_DIRT.get().defaultBlockState();
        BlockState floor = ModBlocks.PORTAL_FLOOR.get().defaultBlockState();
        int kind = random.nextInt(3);

        if (kind == 0) {
            // dust-style courtyard
            for (int ix = -8; ix <= 8; ix++) {
                for (int iz = -8; iz <= 8; iz++) {
                    place(level, x + ix, y - 1, z + iz, dirt);
                }
            }
            for (int ix = -8; ix <= 8; ix++) {
                place(level, x + ix, y, z - 8, panel);
                place(level, x + ix, y + 1, z - 8, panel);
            }
            for (int iz = -8; iz <= 8; iz++) {
                place(level, x - 8, y, z + iz, panel);
                place(level, x - 8, y + 1, z + iz, panel);
            }
            for (int i = 0; i < 4; i++) {
                int px = x + random.nextInt(14) - 7;
                int pz = z + random.nextInt(14) - 7;
                place(level, px, y, pz, panel);
                place(level, px + 1, y, pz, panel);
            }
        } else if (kind == 1) {
            // office building
            int w = 6 + random.nextInt(5);
            int d = 6 + random.nextInt(5);
            for (int ix = 0; ix < w; ix++) {
                for (int iz = 0; iz < d; iz++) {
                    place(level, x + ix, y - 1, z + iz, floor);
                }
            }
            for (int h = 0; h < 3; h++) {
                for (int ix = 0; ix < w; ix++) {
                    place(level, x + ix, y + h, z, panel);
                    place(level, x + ix, y + h, z + d - 1, panel);
                }
                for (int iz = 0; iz < d; iz++) {
                    place(level, x, y + h, z + iz, panel);
                    place(level, x + w - 1, y + h, z + iz, panel);
                }
            }
            for (int ix = 0; ix < w; ix++) {
                for (int iz = 0; iz < d; iz++) {
                    place(level, x + ix, y + 3, z + iz, panel);
                }
            }
            place(level, x + w / 2, y, z, Blocks.AIR.defaultBlockState());
            place(level, x + w / 2, y + 1, z, Blocks.AIR.defaultBlockState());
            Loot.chest(level, new BlockPos(x + 1, y, z + 1), random, Loot.Kind.SALVAGE);
        } else {
            // crates and catwalks
            for (int i = 0; i < 5; i++) {
                int px = x + random.nextInt(10) - 5;
                int pz = z + random.nextInt(10) - 5;
                int h = 1 + random.nextInt(3);
                for (int j = 0; j < h; j++) {
                    place(level, px, y + j, pz, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
                }
            }
            for (int ix = -6; ix <= 6; ix++) {
                place(level, x + ix, y + 4, z, panel);
            }
            place(level, x, y, z, ModBlocks.XBOX_GLOW.get().defaultBlockState());
        }
    }

    /**
     * The house from Garry's Mod. Chest with the key, and a hole in the floor.
     */
    public static void gmodHouse(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState panel = ModBlocks.XBOX_PANEL.get().defaultBlockState();
        BlockState floor = ModBlocks.PORTAL_FLOOR.get().defaultBlockState();
        int w = 7;
        int d = 8;
        int h = 4;

        for (int ix = 0; ix < w; ix++) {
            for (int iz = 0; iz < d; iz++) {
                place(level, x + ix, y - 1, z + iz, floor);
            }
        }
        for (int levelY = 0; levelY < h; levelY++) {
            for (int ix = 0; ix < w; ix++) {
                place(level, x + ix, y + levelY, z, panel);
                place(level, x + ix, y + levelY, z + d - 1, panel);
            }
            for (int iz = 0; iz < d; iz++) {
                place(level, x, y + levelY, z + iz, panel);
                place(level, x + w - 1, y + levelY, z + iz, panel);
            }
        }
        for (int ix = 0; ix < w; ix++) {
            for (int iz = 0; iz < d; iz++) {
                place(level, x + ix, y + h, z + iz, panel);
            }
        }
        // doorway
        place(level, x + w / 2, y, z, Blocks.AIR.defaultBlockState());
        place(level, x + w / 2, y + 1, z, Blocks.AIR.defaultBlockState());
        // window
        place(level, x, y + 2, z + 3, Blocks.AIR.defaultBlockState());
        place(level, x + w - 1, y + 2, z + 4, Blocks.AIR.defaultBlockState());

        // the hole in the floor: a 2x2 fall hole that drops you into white_door_bsp
        for (int ix = 0; ix < 2; ix++) {
            for (int iz = 0; iz < 2; iz++) {
                place(level, x + w - 3 + ix, y - 1, z + d - 3 + iz, Blocks.AIR.defaultBlockState());
                place(level, x + w - 3 + ix, y - 2, z + d - 3 + iz,
                        ModBlocks.FALL_HOLE.get().defaultBlockState());
            }
        }

        // the chest with the key, and the message that explains nothing
        Loot.chest(level, new BlockPos(x + 1, y, z + 1), random, Loot.Kind.WHITE_DOOR_KEY);

        // a broken character in the corner, because someone was playing here
        if (random.nextInt(3) == 0) {
            spawnBrokenCharacter(level, x + w - 2 + 0.5D, y, z + 1 + 0.5D, random);
        }
        place(level, x + w / 2, y + h - 1, z + d / 2, ModBlocks.XBOX_GLOW.get().defaultBlockState());
    }

    private static void brokenCharacters(ServerLevel level, int x, int y, int z, RandomSource random) {
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            spawnBrokenCharacter(level, x + random.nextInt(8) - 4 + 0.5D, y,
                    z + random.nextInt(8) - 4 + 0.5D, random);
        }
        for (int i = 0; i < 4; i++) {
            place(level, x + random.nextInt(10) - 5, y, z + random.nextInt(10) - 5,
                    ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
        }
    }

    private static void spawnBrokenCharacter(ServerLevel level, double x, double y, double z, RandomSource random) {
        var entity = ModEntities.BROKEN_CHARACTER.get().create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        entity.setVariant(random.nextInt(4));
        level.addFreshEntity(entity);
    }

    private static void dashboardBlade(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState panel = ModBlocks.XBOX_PANEL.get().defaultBlockState();
        BlockState glow = ModBlocks.XBOX_GLOW.get().defaultBlockState();
        int width = 6 + random.nextInt(6);
        for (int i = 0; i < width; i++) {
            // a curved blade, like the old dashboard
            int lift = (int) Math.round(Math.sin((double) i / width * Math.PI) * 3.0D);
            for (int h = 0; h < 2 + lift; h++) {
                place(level, x + i, y + h, z, panel);
            }
        }
        place(level, x + width / 2, y + 6, z, glow);
        Loot.chest(level, new BlockPos(x, y, z + 2), random, Loot.Kind.SALVAGE);
    }

    private XboxFabricator() {
    }
}
