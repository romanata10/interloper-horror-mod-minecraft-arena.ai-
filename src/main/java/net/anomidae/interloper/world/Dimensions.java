package net.anomidae.interloper.world;

import net.anomidae.interloper.Interloper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;

/**
 * Every level this mod knows about, and the only place that moves players between them.
 *
 * Moving a player also moves their respawn point, so dying inside the void does not
 * dump you back into the overworld.
 */
public final class Dimensions {
    public static final ResourceKey<Level> SOURCE_VOID = key("source_void");
    public static final ResourceKey<Level> XBOX_DREAMS = key("xbox_360_dreams");
    public static final ResourceKey<Level> WHITE_DOOR = key("white_door_bsp");
    public static final ResourceKey<Level> SKYBOX = key("skybox");

    /** Height of the walkable block layer in all void levels. */
    public static final int FLOOR_Y = -59;

    private static ResourceKey<Level> key(String path) {
        return ResourceKey.create(Registries.DIMENSION, Interloper.id(path));
    }

    @Nullable
    public static ServerLevel level(ServerPlayer player, ResourceKey<Level> key) {
        MinecraftServer server = player.getServer();
        return server == null ? null : server.getLevel(key);
    }

    /**
     * Teleports the player and makes that place their respawn point.
     */
    public static boolean moveTo(ServerPlayer player, ResourceKey<Level> key, double x, double y, double z,
                                 float yaw, boolean setRespawn) {
        ServerLevel level = level(player, key);
        if (level == null) {
            return false;
        }
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        level.getChunk(bx >> 4, bz >> 4);
        if (level.getBlockState(new BlockPos(bx, (int) y, bz)).isAir()
                && level.getBlockState(new BlockPos(bx, (int) y - 1, bz)).isAir()) {
            level.setBlockAndUpdate(new BlockPos(bx, (int) y - 1, bz),
                    net.anomidae.interloper.registry.ModBlocks.SOURCE_FLOOR.get().defaultBlockState());
        }
        player.teleportTo(level, x, y, z, yaw, player.getXRot());
        if (setRespawn) {
            player.setRespawnPosition(key, BlockPos.containing(x, y, z), yaw, true, false);
        }
        return true;
    }

    /**
     * Back to the overworld, at the surface, with a normal respawn point.
     */
    public static boolean toOverworld(ServerPlayer player, boolean resetRespawn) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel overworld = server.overworld();
        int x = (int) player.getX();
        int z = (int) player.getZ();
        overworld.getChunk(x >> 4, z >> 4);
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        player.teleportTo(overworld, x + 0.5D, y + 1.0D, z + 0.5D, player.getYRot(), player.getXRot());
        if (resetRespawn) {
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.setRespawnPosition(Level.OVERWORLD, spawn, 0.0F, false, false);
        }
        return true;
    }

    public static boolean isIn(ServerPlayer player, ResourceKey<Level> key) {
        return player.level().dimension().equals(key);
    }

    public static boolean isVoidLevel(ServerPlayer player) {
        return isIn(player, SOURCE_VOID) || isIn(player, XBOX_DREAMS) || isIn(player, WHITE_DOOR)
                || isIn(player, SKYBOX);
    }

    private Dimensions() {
    }
}
