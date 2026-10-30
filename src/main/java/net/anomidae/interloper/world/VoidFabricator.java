package net.anomidae.interloper.world;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds the maps of the void out of the only block that exists there:
 * arches, geometric shapes, houses, skyblocks and fragments of old maps.
 */
public final class VoidFabricator {
    private static final Map<ServerLevel, Set<Long>> DECORATED = new HashMap<>();

    public static void ensureSpawn(ServerLevel level) {
        // Clear anything that might have grown on the spawn pad.
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y < 8; y++) {
                    level.setBlock(new BlockPos(x, SourceVoid.FLOOR_Y + y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        decorate(level, 0, 0, level.getRandom());
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(SourceVoid.LEVEL)) {
            return;
        }
        Set<Long> done = DECORATED.computeIfAbsent(level, key -> new HashSet<>());
        level.getProfiler().push(Interloper.MODID + "_void_fabricator");
        try {
            for (var player : level.players()) {
                int centerX = player.chunkPosition().x;
                int centerZ = player.chunkPosition().z;
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
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
        } finally {
            level.getProfiler().pop();
        }
    }

    private static long mix(long key) {
        long value = key * 6364136223846793005L + 1442695040888963407L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        return value ^ (value >>> 29);
    }

    private static void place(ServerLevel level, int x, int y, int z, BlockState state) {
        if (y < level.getMinBuildHeight() + 1 || y > level.getMaxBuildHeight() - 1) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunkAt(pos)) {
            return;
        }
        level.setBlock(pos, state, 2);
    }

    private static void decorate(ServerLevel level, int chunkX, int chunkZ, RandomSource random) {
        BlockState floor = ModBlocks.SOURCE_FLOOR.get().defaultBlockState();
        int baseY = SourceVoid.FLOOR_Y;
        int originX = chunkX * 16 + 4 + random.nextInt(8);
        int originZ = chunkZ * 16 + 4 + random.nextInt(8);

        int roll = random.nextInt(100);
        if (roll < 22) {
            arch(level, originX, baseY, originZ, floor, random);
        } else if (roll < 42) {
            geometry(level, originX, baseY, originZ, floor, random);
        } else if (roll < 58) {
            house(level, originX, baseY, originZ, floor, random);
        } else if (roll < 70) {
            skyblock(level, originX, baseY, originZ, floor, random);
        } else if (roll < 82) {
            ruins(level, originX, baseY, originZ, floor, random);
        } else if (roll < 92) {
            floatingMap(level, originX, baseY, originZ, floor, random);
        }
        // The rest of the chunks stay empty. That is also a structure.
    }

    private static void arch(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int height = 5 + random.nextInt(6);
        int width = 4 + random.nextInt(5);
        for (int h = 0; h <= height; h++) {
            place(level, x, y + h, z, floor);
            place(level, x + width, y + h, z, floor);
        }
        for (int i = 0; i <= width; i++) {
            int lift = (int) Math.round(Math.sin((double) i / width * Math.PI) * 2.5D);
            place(level, x + i, y + height + lift, z, floor);
        }
        for (int d = 1; d <= 3; d++) {
            for (int h = 0; h <= height; h++) {
                place(level, x, y + h, z + d, floor);
                place(level, x + width, y + h, z + d, floor);
            }
        }
    }

    private static void geometry(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int kind = random.nextInt(4);
        if (kind == 0) {
            int size = 2 + random.nextInt(4);
            for (int i = 0; i <= size; i++) {
                for (int ix = -i; ix <= i; ix++) {
                    for (int iz = -i; iz <= i; iz++) {
                        place(level, x + ix, y + i, z + iz, floor);
                    }
                }
            }
        } else if (kind == 1) {
            for (int h = 0; h < 12 + random.nextInt(10); h++) {
                place(level, x, y + h, z, floor);
                if (h % 3 == 0) {
                    place(level, x + 1, y + h, z, floor);
                    place(level, x, y + h, z + 1, floor);
                }
            }
        } else if (kind == 2) {
            int radius = 3 + random.nextInt(4);
            for (int ix = -radius; ix <= radius; ix++) {
                for (int iz = -radius; iz <= radius; iz++) {
                    if (ix * ix + iz * iz > radius * radius) {
                        continue;
                    }
                    place(level, x + ix, y, z + iz, floor);
                }
            }
            for (int ix = -radius; ix <= radius; ix++) {
                place(level, x + ix, y + 4, z, floor);
            }
            for (int iz = -radius; iz <= radius; iz++) {
                place(level, x, y + 4, z + iz, floor);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                place(level, x + i, y, z, floor);
                place(level, x - i, y, z, floor);
                place(level, x, y, z + i, floor);
                place(level, x, y, z - i, floor);
                place(level, x + i, y + i, z, floor);
                place(level, x - i, y + i, z, floor);
            }
        }
    }

    private static void house(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int width = 5 + random.nextInt(4);
        int depth = 5 + random.nextInt(4);
        int height = 4 + random.nextInt(3);
        for (int ix = 0; ix < width; ix++) {
            for (int iz = 0; iz < depth; iz++) {
                place(level, x + ix, y, z + iz, floor);
            }
        }
        for (int h = 1; h <= height; h++) {
            for (int ix = 0; ix < width; ix++) {
                place(level, x + ix, y + h, z, floor);
                place(level, x + ix, y + h, z + depth - 1, floor);
            }
            for (int iz = 0; iz < depth; iz++) {
                place(level, x, y + h, z + iz, floor);
                place(level, x + width - 1, y + h, z + iz, floor);
            }
        }
        for (int ix = 0; ix < width; ix++) {
            for (int iz = 0; iz < depth; iz++) {
                place(level, x + ix, y + height + 1, z + iz, floor);
            }
        }
        // door
        place(level, x + width / 2, y + 1, z, Blocks.AIR.defaultBlockState());
        place(level, x + width / 2, y + 2, z, Blocks.AIR.defaultBlockState());
        // windows
        place(level, x + 1, y + 2, z, Blocks.AIR.defaultBlockState());
        place(level, x + width - 2, y + 2, z, Blocks.AIR.defaultBlockState());
        place(level, x + 1, y + 2, z + depth - 1, Blocks.AIR.defaultBlockState());
    }

    private static void skyblock(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int altitude = y + 18 + random.nextInt(20);
        for (int ix = -4; ix <= 4; ix++) {
            for (int iz = -4; iz <= 4; iz++) {
                if (Math.abs(ix) + Math.abs(iz) > 6) {
                    continue;
                }
                place(level, x + ix, altitude, z + iz, floor);
            }
        }
        for (int h = 1; h <= 3; h++) {
            place(level, x - 2, altitude + h, z - 2, floor);
            place(level, x + 2, altitude + h, z - 2, floor);
            place(level, x - 2, altitude + h, z + 2, floor);
            place(level, x + 2, altitude + h, z + 2, floor);
        }
        for (int ix = -2; ix <= 2; ix++) {
            for (int iz = -2; iz <= 2; iz++) {
                place(level, x + ix, altitude + 4, z + iz, floor);
            }
        }
        // tiny tree made of the same block
        for (int h = 1; h <= 4; h++) {
            place(level, x, altitude + 4 + h, z, floor);
        }
    }

    private static void ruins(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int count = 3 + random.nextInt(6);
        for (int i = 0; i < count; i++) {
            int px = x + random.nextInt(10) - 5;
            int pz = z + random.nextInt(10) - 5;
            int height = 1 + random.nextInt(7);
            for (int h = 0; h <= height; h++) {
                place(level, px, y + h, pz, floor);
                if (random.nextInt(3) == 0) {
                    place(level, px + 1, y + h, pz, floor);
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            int px = x + random.nextInt(12) - 6;
            int pz = z + random.nextInt(12) - 6;
            place(level, px, y + random.nextInt(3), pz, floor);
        }
    }

    private static void floatingMap(ServerLevel level, int x, int y, int z, BlockState floor, RandomSource random) {
        int altitude = y + 8 + random.nextInt(14);
        int size = 7 + random.nextInt(6);
        for (int ix = -size; ix <= size; ix++) {
            for (int iz = -size; iz <= size; iz++) {
                double noise = Math.sin(ix * 0.55D + random.nextInt(2)) + Math.cos(iz * 0.45D);
                if (noise < -0.4D) {
                    continue;
                }
                int h = (int) Math.max(1, Math.round(noise));
                for (int i = 0; i < h; i++) {
                    place(level, x + ix, altitude + i, z + iz, floor);
                }
            }
        }
    }

    private VoidFabricator() {
    }
}
