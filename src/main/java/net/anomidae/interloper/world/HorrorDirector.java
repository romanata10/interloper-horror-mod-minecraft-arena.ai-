package net.anomidae.interloper.world;

import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.SourceText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.List;

/**
 * Picks a player and does something unkind to them now and then.
 */
public final class HorrorDirector {
    private static final String[] WHISPERS = {
            "message.interloper.whisper.1",
            "message.interloper.whisper.2",
            "message.interloper.whisper.3",
            "message.interloper.whisper.4",
            "message.interloper.whisper.5",
            "message.interloper.whisper.6"
    };

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!InterloperConfig.AMBIENCE_ENABLED.get()) {
            return;
        }
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        RandomSource random = level.getRandom();
        int intervalSeconds = Math.max(5, InterloperConfig.AMBIENCE_INTERVAL.get() / 20);
        if (random.nextInt(intervalSeconds) != 0) {
            return;
        }

        ServerPlayer player = players.get(random.nextInt(players.size()));
        if (player.isSpectator() || !player.isAlive()) {
            return;
        }
        rollEvent(level, player, random);
    }

    private static void rollEvent(ServerLevel level, ServerPlayer player, RandomSource random) {
        int roll = random.nextInt(100);

        // the console keeps talking even when nobody is using it
        if (roll < 26 && InterloperConfig.AMBIENT_CHAT.get()) {
            ModNetwork.toPlayer(player, new ConsoleLinePacket(SourceText.randomCommand(random),
                    0xFFB8B8B8, false));
            if (random.nextInt(3) == 0) {
                ModNetwork.toPlayer(player, new ConsoleLinePacket(SourceText.randomLine(random),
                        0xFF8A8A8A, false));
            }
            return;
        }

        if (roll < 45) {
            playNearby(level, player, ModSounds.WHISPER.get(), 0.9F, 0.85F + random.nextFloat() * 0.3F);
        } else if (roll < 58) {
            playNearby(level, player, ModSounds.STATIC.get(), 0.7F, 0.7F + random.nextFloat() * 0.5F);
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 30, 0.35F));
        } else if (roll < 70) {
            if (InterloperConfig.AMBIENT_CHAT.get()) {
                String key = WHISPERS[random.nextInt(WHISPERS.length)];
                ModNetwork.toPlayer(player, new ChatGhostPacket(Component.translatable(key)
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            }
        } else if (roll < 80) {
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.WHISPER, 60, 0.6F));
            playNearby(level, player, ModSounds.HEARTBEAT.get(), 1.0F, 1.0F);
        } else if (roll < 88) {
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 50, 0.5F));
            playNearby(level, player, ModSounds.STINGER.get(), 0.8F, 0.9F);
        } else if (roll < 97) {
            if (InterloperConfig.NATURAL_SKYGHOST.get()) {
                spawnSkyghost(level, player, random);
            }
        }
        // The Interloper never spawns on his own in the overworld any more.
        // He only comes when somebody asks for him: the console, or a chat command.
    }

    private static void playNearby(ServerLevel level, Player player, SoundEvent sound, float volume, float pitch) {
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 6.0D + level.getRandom().nextDouble() * 14.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        level.playSound(null, x, player.getY(), z, sound, SoundSource.AMBIENT, volume, pitch);
    }

    public static void spawnSkyghost(ServerLevel level, Player player, RandomSource random) {
        // There is exactly one place a Skyghost can be, and it is not here.
        if (!level.dimension().equals(Dimensions.SKYBOX)) {
            return;
        }
        SkyghostEntity ghost = ModEntities.SKYGHOST.get().create(level);
        if (ghost == null) {
            return;
        }
        Vec3 look = player.getViewVector(1.0F);
        double distance = 18.0D + random.nextDouble() * 12.0D;
        double x = player.getX() - look.x * distance;
        double z = player.getZ() - look.z * distance;
        double y = player.getY() + 1.0D + random.nextDouble() * 2.0D;
        ghost.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        ghost.bindPlayer(player.getUUID());
        level.addFreshEntity(ghost);
        level.playSound(null, new BlockPos((int) x, (int) y, (int) z), ModSounds.WHISPER.get(),
                SoundSource.AMBIENT, 0.7F, 0.6F);
    }

    public static void startHunt(ServerLevel level, ServerPlayer player, RandomSource random) {
        InterloperEntity interloper = ModEntities.INTERLOPER.get().create(level);
        if (interloper == null) {
            return;
        }
        Vec3 look = player.getViewVector(1.0F);
        double distance = 26.0D + random.nextDouble() * 14.0D;
        double x = player.getX() - look.x * distance;
        double z = player.getZ() - look.z * distance;
        interloper.moveTo(x, player.getY(), z, random.nextFloat() * 360.0F, 0.0F);
        interloper.bindPlayer(player.getUUID());
        interloper.setTarget(player);
        interloper.setHunting(true);
        level.addFreshEntity(interloper);

        level.playSound(null, player.blockPosition(), ModSounds.STINGER.get(), SoundSource.AMBIENT, 1.0F, 0.55F);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 90, 0.8F));
        if (InterloperConfig.AMBIENT_CHAT.get()) {
            ModNetwork.toPlayer(player, new ChatGhostPacket(Component.translatable("message.interloper.hunt_start")
                    .withStyle(ChatFormatting.DARK_RED)));
        }
    }

    private HorrorDirector() {
    }
}
