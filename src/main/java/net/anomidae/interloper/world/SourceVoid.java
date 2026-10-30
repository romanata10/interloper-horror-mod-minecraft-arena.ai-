package net.anomidae.interloper.world;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * The level behind the level. One block, one colour, no sky.
 */
public final class SourceVoid {
    public static final ResourceKey<Level> LEVEL =
            ResourceKey.create(Registries.DIMENSION, Interloper.id("source_void"));

    /** y of the block layer you stand on. */
    public static final int FLOOR_Y = -59;

    public static boolean enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel level = server.getLevel(LEVEL);
        if (level == null) {
            player.sendSystemMessage(Component.literal("error: could not load interloper:source_void")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        level.getChunk(0, 0);
        VoidFabricator.ensureSpawn(level);

        player.teleportTo(level, 0.5D, FLOOR_Y, 0.5D, player.getYRot(), player.getXRot());
        player.getPersistentData().putBoolean(InterloperEntity.VOID_FLAG, true);

        ModNetwork.toPlayer(player, new ConsoleLinePacket("Loading source_void.bsp", 0xFFCFCFCF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("source void: minecraft", 0xFFFFFFFF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("ERROR: map has no entities", 0xFFFF7A00, false));
        level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 0.4F);
        level.playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(), SoundSource.AMBIENT, 0.8F, 0.5F);
        return true;
    }

    public static boolean exit(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel overworld = server.overworld();
        int x = (int) player.getX();
        int z = (int) player.getZ();
        overworld.getChunk(x >> 4, z >> 4);
        int y = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        player.teleportTo(overworld, x + 0.5D, y + 1.0D, z + 0.5D, player.getYRot(), player.getXRot());
        ModNetwork.toPlayer(player, new ConsoleLinePacket("disconnect", 0xFFB0B0B0, false));
        return true;
    }

    public static boolean isInVoid(ServerPlayer player) {
        return player.level().dimension().equals(LEVEL);
    }

    private SourceVoid() {
    }
}
