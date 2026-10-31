package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * The level behind the level. One block, one colour, no sky.
 */
public final class SourceVoid {
    public static final int FLOOR_Y = Dimensions.FLOOR_Y;

    public static boolean enter(ServerPlayer player) {
        ServerLevel level = Dimensions.level(player, Dimensions.SOURCE_VOID);
        if (level == null) {
            player.sendSystemMessage(Component.literal("error: could not load interloper:source_void")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        level.getChunk(0, 0);
        VoidFabricator.ensureSpawn(level);

        int offsetX = (int) (Math.abs(player.getUUID().getLeastSignificantBits()) % 9L) - 4;
        int offsetZ = (int) (Math.abs(player.getUUID().getMostSignificantBits()) % 9L) - 4;
        VoidFabricator.clearPad(level, offsetX, FLOOR_Y, offsetZ);

        // the chase in the old world is over the moment you are gone
        Hunters.discardBoundTo(player);

        // the respawn point moves with the player: dying in the void keeps you in the void
        Dimensions.moveTo(player, Dimensions.SOURCE_VOID, offsetX + 0.5D, FLOOR_Y, offsetZ + 0.5D,
                player.getYRot(), true);
        player.getPersistentData().putBoolean(InterloperEntity.VOID_FLAG, true);
        SkyboxWatcher.markVoid(player);

        ModNetwork.toPlayer(player, new ConsoleLinePacket("Loading source_void.bsp", 0xFFCFCFCF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("source void: minecraft", 0xFFFFFFFF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("ERROR: map has no entities", 0xFFFF7A00, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("there is a hole in the floor", 0xFF808080, true));
        level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 0.4F);
        level.playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(), SoundSource.AMBIENT, 0.8F, 0.5F);
        return true;
    }

    public static boolean exit(ServerPlayer player) {
        if (!Dimensions.toOverworld(player, true)) {
            return false;
        }
        ModNetwork.toPlayer(player, new ConsoleLinePacket("disconnect", 0xFFB0B0B0, false));
        return true;
    }

    public static boolean isInVoid(ServerPlayer player) {
        return Dimensions.isIn(player, Dimensions.SOURCE_VOID);
    }

    private SourceVoid() {
    }
}
