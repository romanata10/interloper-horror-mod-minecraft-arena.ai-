package net.anomidae.interloper.world;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.util.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * white_door_bsp: one small white room, one door, one lock, and the way out.
 */
public final class WhiteDoor {
    public static final int FLOOR_Y = Dimensions.FLOOR_Y;
    private static final BlockPos ROOM = new BlockPos(0, FLOOR_Y, 0);

    public static boolean enter(ServerPlayer player) {
        ServerLevel level = Dimensions.level(player, Dimensions.WHITE_DOOR);
        if (level == null) {
            player.sendSystemMessage(Component.literal("error: could not load interloper:white_door_bsp")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        build(level);
        Dimensions.moveTo(player, Dimensions.WHITE_DOOR, 0.5D, FLOOR_Y, 4.5D, 180.0F, true);

        ModNetwork.toPlayer(player, new ConsoleLinePacket("Loading white_door_bsp", 0xFFCFCFCF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("the exit is white door and key", 0xFFFFFFFF, true));
        level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 0.6F, 1.6F);
        return true;
    }

    private static void build(ServerLevel level) {
        BlockState wall = ModBlocks.WHITE_WALL.get().defaultBlockState();
        int x0 = ROOM.getX() - 5;
        int x1 = ROOM.getX() + 5;
        int z0 = ROOM.getZ() - 5;
        int z1 = ROOM.getZ() + 7;
        int floor = ROOM.getY();
        int ceiling = floor + 6;

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = floor - 1; y <= ceiling; y++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1 || y == floor - 1 || y == ceiling;
                    level.setBlock(new BlockPos(x, y, z), edge ? wall : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // the door, and the lock that wants the key
        BlockPos doorPos = new BlockPos(ROOM.getX(), floor, z0);
        level.setBlock(new BlockPos(doorPos.getX() - 1, floor, z0), ModBlocks.WHITE_DOOR.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX() + 1, floor, z0), ModBlocks.WHITE_LOCK.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX(), floor, z0), wall, 3);
        level.setBlock(new BlockPos(doorPos.getX(), floor + 1, z0), ModBlocks.WHITE_DOOR.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX(), floor + 2, z0), ModBlocks.WHITE_DOOR.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX(), floor, z0 + 1), Blocks.LIGHT.defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX() + 2, floor + 3, z0 + 3), Blocks.LIGHT.defaultBlockState(), 3);
        level.setBlock(new BlockPos(doorPos.getX() - 2, floor + 3, z0 + 3), Blocks.LIGHT.defaultBlockState(), 3);
    }

    /**
     * Called when the key turns in the lock.
     */
    public static void open(ServerPlayer player) {
        ModNetwork.toPlayer(player, new ConsoleLinePacket("door unlocked", 0xFFFFFFFF, true));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("sv_cheats has been returned to 0", 0xFFCFCFCF, false));
        player.sendSystemMessage(Component.literal("the white door opens. minecraft is on the other side.")
                .withStyle(ChatFormatting.WHITE));
        ServerScheduler.schedule(50, () -> {
            if (!player.isAlive()) {
                return;
            }
            Dimensions.toOverworld(player, true);
            player.sendSystemMessage(Component.literal("you can survive here now")
                    .withStyle(ChatFormatting.GRAY));
        });
    }

    public static boolean isInRoom(ServerPlayer player) {
        return Dimensions.isIn(player, Dimensions.WHITE_DOOR);
    }

    private WhiteDoor() {
    }
}
