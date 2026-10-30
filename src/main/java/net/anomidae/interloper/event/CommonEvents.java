package net.anomidae.interloper.event;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.command.InterloperCommand;
import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.item.ConsoleCommands;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.world.HorrorDirector;
import net.anomidae.interloper.world.VoidDirector;
import net.anomidae.interloper.world.VoidFabricator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private static final String CONSOLE_FLAG = "interloper.console_given";

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        InterloperCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ServerScheduler.tick();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerScheduler.reset();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        HorrorDirector.onLevelTick(event);
        VoidFabricator.tick(level);
        VoidDirector.tick(level);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean firstTime = !player.getPersistentData().getBoolean(CONSOLE_FLAG);
        if (firstTime) {
            player.getPersistentData().putBoolean(CONSOLE_FLAG, true);
            ItemStack console = new ItemStack(ModItems.CONSOLE.get());
            if (!player.getInventory().contains(console)) {
                player.getInventory().add(console);
            }
            ModNetwork.toPlayer(player, new ConsoleLinePacket("Source Engine 2006, build 4044", 0xFFCFCFCF, false));
            ModNetwork.toPlayer(player, new ConsoleLinePacket("Console initialized.", 0xFFCFCFCF, false));
            ModNetwork.toPlayer(player, new ConsoleLinePacket("map: minecraft", 0xFFFFFFFF, false));
            ModNetwork.toPlayer(player, new ConsoleLinePacket("> play demo interloper_1   <- press ENTER",
                    0xFFFFFFFF, true));
        }

        if (!InterloperConfig.INTRO_MESSAGE.get()) {
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
        var source = event.getSource().getEntity();
        if (source instanceof net.anomidae.interloper.entity.SourceEntity
                || source instanceof net.anomidae.interloper.entity.InterloperEntity) {
            player.serverLevel().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(),
                    SoundSource.HOSTILE, 1.0F, 0.6F);
            player.sendSystemMessage(Component.literal("error: entity killed the player without a valid trace")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    private CommonEvents() {
    }
}
