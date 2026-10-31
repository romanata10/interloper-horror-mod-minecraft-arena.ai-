package net.anomidae.interloper.world;

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
 * xbox_360_dreams. A console dashboard that someone left running for twenty years,
 * with grass on it and a gm_construct map in it.
 */
public final class XboxDreams {
    public static final int FLOOR_Y = Dimensions.FLOOR_Y;

    public static boolean enter(ServerPlayer player) {
        ServerLevel level = Dimensions.level(player, Dimensions.XBOX_DREAMS);
        if (level == null) {
            player.sendSystemMessage(Component.literal("error: could not load interloper:xbox_360_dreams")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        level.getChunk(0, 0);
        XboxFabricator.ensureSpawn(level);

        int offsetX = (int) (Math.abs(player.getUUID().getLeastSignificantBits()) % 7L) - 3;
        int offsetZ = (int) (Math.abs(player.getUUID().getMostSignificantBits()) % 7L) - 3;
        XboxFabricator.clearPad(level, offsetX, FLOOR_Y, offsetZ);

        Hunters.discardBoundTo(player);
        Dimensions.moveTo(player, Dimensions.XBOX_DREAMS, offsetX + 0.5D, FLOOR_Y, offsetZ + 0.5D,
                player.getYRot(), true);

        SkyboxWatcher.markVoid(player);
        ModNetwork.toPlayer(player, new ConsoleLinePacket("Loading xbox_360_dreams.bsp", 0xFFCFCFCF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("XBOX 360 DREAMS", 0xFF8CFF00, true));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("achievement unlocked: you are still here", 0xFFB0FF60, false));
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.8F, 0.6F);
        level.playSound(null, player.blockPosition(), ModSounds.STATIC.get(), SoundSource.AMBIENT, 0.6F, 0.9F);
        return true;
    }

    public static boolean isInDreams(ServerPlayer player) {
        return Dimensions.isIn(player, Dimensions.XBOX_DREAMS);
    }

    private XboxDreams() {
    }
}
