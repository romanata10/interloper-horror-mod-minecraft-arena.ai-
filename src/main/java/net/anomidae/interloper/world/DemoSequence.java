package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.util.SourceText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Demo 1. The last thing that was ever recorded.
 */
public final class DemoSequence {
    public static final String DEMO_FLAG = "interloper.demo_played";

    public static void play(ServerPlayer player, int demoId) {
        ServerLevel level = player.serverLevel();
        player.getPersistentData().putBoolean(DEMO_FLAG, true);

        line(player, 0, "--- demo playback ---", 0xFFFFFFFF);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CAPTURE, 200, 1.0F));
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.DARKNESS, 120, 0.55F));
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 200, 0.8F));
        level.playSound(null, player.blockPosition(), ModSounds.STATIC.get(), SoundSource.AMBIENT, 1.0F, 0.8F);
        level.playSound(null, player.blockPosition(), ModSounds.CUBEMAP.get(), SoundSource.AMBIENT, 1.0F, 0.7F);

        String demoName = demoId <= 1 ? "interloper_1" : "interloper_" + demoId;
        line(player, 10, "Loading demo " + demoName + ".dem", 0xFF9A9A9A);
        line(player, 30, "Demo file recorded on: unknown", 0xFF9A9A9A);
        line(player, 45, "Playback started.", 0xFFCFCFCF);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 140, 0.9F));

        int seed = 60;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            schedule(player, seed + i * 22, () -> {
                line(player, 0, "  " + SourceText.randomCommand(player.getRandom()), 0xFFCFCFCF);
                if (index % 2 == 0) {
                    ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 40, 0.6F));
                }
            });
        }

        schedule(player, 190, () -> {
            line(player, 0, "Player: who is recording this", 0xFFFFFFFF);
            line(player, 0, "Demo playback finished. 00:47", 0xFFFFA500);
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.FLASH, 20, 1.0F));
        });

        schedule(player, 215, () -> {
            line(player, 0, "Disconnected from server.", 0xFFFFA500);
            line(player, 0, "Reconnecting to minecraft ...", 0xFFCFCFCF);
            line(player, 0, "map: minecraft", 0xFFFFFFFF);
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.DARKNESS, 60, 0.9F));
        });

        schedule(player, 260, () -> {
            line(player, 0, "You are in the world again.", 0xFFCFCFCF);
            line(player, 0, "sv_cheats has been forced to 0.", 0xFFCFCFCF);
            ModNetwork.toPlayer(player, new ConsoleLinePacket(
                    "The console stays open.", 0xFF808080, true));
            player.sendSystemMessage(Component.literal("> the tape kept recording")
                    .withStyle(ChatFormatting.DARK_GRAY));
        });

        // bells, then him
        schedule(player, 320, () -> {
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 0.7F);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.AMBIENT, 1.0F, 0.9F);
            line(player, 0, "Warning: an entity has no valid origin.", 0xFFFF7A00);
        });

        schedule(player, 380, () -> spawnHunter(player, true));
    }

    /**
     * Bells, then the umbrella man flies at you from behind the render distance.
     */
    public static void spawnHunter(ServerPlayer player, boolean bells) {
        ServerLevel level = player.serverLevel();
        if (bells) {
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.AMBIENT, 1.0F, 0.75F);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 0.55F);
            level.playSound(null, player.blockPosition(), ModSounds.STINGER.get(), SoundSource.AMBIENT, 0.9F, 0.5F);
        }

        InterloperEntity interloper = ModEntities.INTERLOPER.get().create(level);
        if (interloper == null) {
            return;
        }
        Vec3 look = player.getViewVector(1.0F);
        double x = player.getX() - look.x * 42.0D + (level.getRandom().nextDouble() - 0.5D) * 10.0D;
        double z = player.getZ() - look.z * 42.0D + (level.getRandom().nextDouble() - 0.5D) * 10.0D;
        double y = player.getY() + 6.0D;
        interloper.moveTo(x, y, z, player.getYRot(), 0.0F);
        interloper.bindPlayer(player.getUUID());
        interloper.setTarget(player);
        interloper.setHunting(true);
        level.addFreshEntity(interloper);

        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 100, 0.9F));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("ent_create interloper", 0xFFCFCFCF, false));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("NPC pre-cache failed: models/interloper.mdl", 0xFFFF7A00, false));
    }

    private static void schedule(ServerPlayer player, int delay, Runnable action) {
        ServerScheduler.schedule(delay, () -> {
            // the demo belongs to one player; if they left, the tape stops
            if (Hunters.isOnline(player) && player.isAlive()) {
                action.run();
            }
        });
    }

    private static void line(ServerPlayer player, int delay, String text, int color) {
        if (delay <= 0) {
            ModNetwork.toPlayer(player, new ConsoleLinePacket(text, color, false));
            return;
        }
        schedule(player, delay, () -> ModNetwork.toPlayer(player, new ConsoleLinePacket(text, color, false)));
    }

    private DemoSequence() {
    }
}
