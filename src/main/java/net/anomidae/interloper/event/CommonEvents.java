package net.anomidae.interloper.event;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.command.InterloperCommand;
import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.world.HorrorDirector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        InterloperCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        HorrorDirector.onLevelTick(event);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !InterloperConfig.INTRO_MESSAGE.get()) {
            return;
        }
        player.displayClientMessage(Component.translatable("message.interloper.intro")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        if (player.level().getRandom().nextInt(3) == 0) {
            player.level().playSound(null, player.blockPosition(), ModSounds.WHISPER.get(),
                    SoundSource.AMBIENT, 0.8F, 0.8F);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getSource().getEntity() instanceof InterloperEntity) {
            player.serverLevel().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(),
                    SoundSource.HOSTILE, 1.0F, 0.6F);
            var server = player.getServer();
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(
                        Component.translatable("message.interloper.caught", player.getDisplayName())
                                .withStyle(ChatFormatting.DARK_RED), false);
            }
        }
    }

    private CommonEvents() {
    }
}
