package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SKYBOX: the pocket dimension that SKYBOX.exe opens. A castle on a mountain,
 * water underneath, and one Skyghost that lives there now and nowhere else.
 */
public final class SkyboxDimension {
    public static final String UNLOCKED = "interloper.skybox_unlocked";

    private static final int WATER_TOP = -51;
    private static final int PEAK = -20;
    private static boolean built;

    public static boolean isUnlocked(ServerPlayer player) {
        return player.getPersistentData().getBoolean(UNLOCKED);
    }

    public static void unlock(ServerPlayer player) {
        player.getPersistentData().putBoolean(UNLOCKED, true);
        SkyboxState.get(player.serverLevel()).unlockSkybox();
    }

    public static boolean enter(ServerPlayer player) {
        ServerLevel level = Dimensions.level(player, Dimensions.SKYBOX);
        if (level == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component
                    .literal("error: interloper:skybox is not loaded"));
            return false;
        }
        if (!built || !level.hasChunkAt(new BlockPos(0, PEAK, 0))) {
            build(level);
            built = true;
        }

        Dimensions.moveTo(player, Dimensions.SKYBOX, 0.5D, PEAK + 1.0D, 1.5D, 0.0F, true);
        level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 1.2F);
        ModNetwork.toPlayer(player, new ConsoleLinePacket("skybox.exe: mounted", 0xFFFFFFFF, true));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("map: skybox_castle.bsp", 0xFFCFCFCF, false));

        ensureExitPad(level);
        spawnResident(level, player);
        return true;
    }

    /**
     * The hole in the castle floor that leads home. Re-stamped on every entry, so
     * old worlds get it too.
     */
    private static void ensureExitPad(ServerLevel level) {
        for (int x = -1; x <= 1; x++) {
            for (int z = 5; z <= 7; z++) {
                set(level, x, PEAK, z, ModBlocks.FALL_HOLE.get().defaultBlockState());
            }
        }
        for (int x = -2; x <= 2; x++) {
            set(level, x, PEAK, 4, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
            set(level, x, PEAK, 8, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
        }
        for (int z = 4; z <= 8; z++) {
            set(level, -2, PEAK, z, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
            set(level, 2, PEAK, z, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
        }
    }

    /**
     * The way back. SKYBOX.exe, the exit pad in the castle floor and falling out
     * of the map all end here.
     */
    public static boolean exit(ServerPlayer player) {
        if (!Dimensions.isIn(player, Dimensions.SKYBOX)) {
            return false;
        }
        if (!Dimensions.toOverworld(player, true)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component
                    .literal("error: could not load minecraft:overworld"));
            return false;
        }
        player.resetFallDistance();
        player.sendSystemMessage(net.minecraft.network.chat.Component
                .literal("you are outside again").withStyle(net.minecraft.ChatFormatting.WHITE));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("skybox.exe: disconnect", 0xFFCFCFCF, true));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("map: minecraft", 0xFFFFFFFF, false));
        player.level().playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL,
                SoundSource.PLAYERS, 0.8F, 0.6F);
        return true;
    }

    /**
     * Falling out of the pocket dimension is also an exit, the same way it is for
     * every other map in this mod.
     */
    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(Dimensions.SKYBOX)) {
            return;
        }
        int bottom = level.getMinBuildHeight() - 4;
        for (ServerPlayer player : level.players()) {
            if (player.getY() < bottom) {
                exit(player);
            }
        }
    }

    /**
     * The castle keeps exactly one ghost. It cannot leave.
     */
    private static void spawnResident(ServerLevel level, ServerPlayer player) {
        boolean exists = !level.getEntitiesOfClass(SkyghostEntity.class,
                new net.minecraft.world.phys.AABB(-64, -64, -64, 64, 256, 64)).isEmpty();
        if (exists) {
            return;
        }
        SkyghostEntity ghost = ModEntities.SKYGHOST.get().create(level);
        if (ghost == null) {
            return;
        }
        ghost.moveTo(0.5D, PEAK + 1.0D, -6.5D, 180.0F, 0.0F);
        ghost.bindPlayer(player.getUUID());
        level.addFreshEntity(ghost);
    }

    private static void build(ServerLevel level) {
        BlockState stone = ModBlocks.SOURCE_FLOOR.get().defaultBlockState();
        BlockState wall = ModBlocks.WHITE_WALL.get().defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();

        // the sea is part of the flat generator (see dimension/skybox.json);
        // the island is not
        // mountain
        for (int y = WATER_TOP; y <= PEAK; y++) {
            double t = (double) (y - WATER_TOP) / Math.max(1, PEAK - WATER_TOP);
            int radius = (int) Math.round(22 - 19 * t);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z > radius * radius) {
                        continue;
                    }
                    set(level, x, y, z, stone);
                }
            }
        }

        // plateau + castle
        for (int x = -10; x <= 10; x++) {
            for (int z = -12; z <= 12; z++) {
                set(level, x, PEAK, z, wall);
            }
        }
        for (int x = -9; x <= 9; x++) {
            for (int z = -11; z <= 11; z++) {
                boolean edge = x == -9 || x == 9 || z == -11 || z == 11;
                for (int y = PEAK + 1; y <= PEAK + 7; y++) {
                    if (edge) {
                        set(level, x, y, z, wall);
                    } else {
                        set(level, x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
                set(level, x, PEAK + 8, z, wall);
            }
        }
        // gate
        for (int y = PEAK + 1; y <= PEAK + 3; y++) {
            set(level, 0, y, 11, Blocks.AIR.defaultBlockState());
            set(level, -1, y, 11, Blocks.AIR.defaultBlockState());
        }
        // windows
        for (int x = -6; x <= 6; x += 4) {
            set(level, x, PEAK + 4, 11, glass);
            set(level, x, PEAK + 4, -11, glass);
        }
        // the way out: a hole in the castle floor that does not go anywhere here
        for (int x = -1; x <= 1; x++) {
            for (int z = 5; z <= 7; z++) {
                set(level, x, PEAK, z, ModBlocks.FALL_HOLE.get().defaultBlockState());
            }
        }
        for (int x = -2; x <= 2; x++) {
            set(level, x, PEAK, 4, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
            set(level, x, PEAK, 8, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
        }
        for (int z = 4; z <= 8; z++) {
            set(level, -2, PEAK, z, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
            set(level, 2, PEAK, z, ModBlocks.MISSING_TEXTURE.get().defaultBlockState());
        }

        // tower on top
        for (int y = PEAK + 9; y <= PEAK + 16; y++) {
            int radius = 3;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
                    set(level, x, y, z, edge ? wall : Blocks.AIR.defaultBlockState());
                }
            }
        }
        set(level, 0, PEAK + 17, 0, ModBlocks.XBOX_GLOW.get().defaultBlockState());
        set(level, 0, PEAK + 6, 0, ModBlocks.XBOX_GLOW.get().defaultBlockState());
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        if (y < level.getMinBuildHeight() + 1 || y > level.getMaxBuildHeight() - 2) {
            return;
        }
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    public static void reset() {
        built = false;
    }

    private SkyboxDimension() {
    }
}
