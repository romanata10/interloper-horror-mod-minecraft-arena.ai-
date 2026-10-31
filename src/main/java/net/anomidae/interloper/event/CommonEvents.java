package net.anomidae.interloper.event;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.command.InterloperCommand;
import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.item.ConsoleCommands;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.world.DemoSequence;
import net.anomidae.interloper.world.XboxFabricator;
import net.minecraft.server.MinecraftServer;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.util.TargetBound;
import net.anomidae.interloper.world.HorrorDirector;
import net.anomidae.interloper.world.VoidDirector;
import net.anomidae.interloper.world.VoidFabricator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private static final String CONSOLE_FLAG = "interloper.console_given";
    private static final String RETURN_CONSOLE = "interloper.return_console";

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
        VoidFabricator.reset();
        XboxFabricator.reset();
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

        // A hunt never survives a reconnect. Anything that was still chasing this
        // player from a previous session is deleted here, so the Interloper can not
        // camp the spawn point and crash the same player over and over.
        Hunters.discardBoundTo(player);
        ServerScheduler.schedule(20, () -> Hunters.discardBoundTo(player));

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
            player.sendSystemMessage(Component.translatable("message.interloper.console_given")
                    .withStyle(ChatFormatting.DARK_GRAY));
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
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Leaving the server (or being kicked by a crash) ends the hunt for that
        // player. Nothing of ours is left behind in the world.
        if (event.getEntity() instanceof ServerPlayer player) {
            Hunters.discardBoundTo(player);
        }
    }

    /**
     * The console is not a normal item: it comes back after death.
     */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.getPersistentData().getBoolean(RETURN_CONSOLE)) {
            return;
        }
        player.getPersistentData().remove(RETURN_CONSOLE);
        ItemStack console = new ItemStack(ModItems.CONSOLE.get());
        if (!player.getInventory().contains(console)) {
            player.getInventory().add(console);
        }
        // nothing is allowed to camp the spawn point
        Hunters.discardBoundTo(player);
    }

    /**
     * Chat commands: the overworld Interloper can only be called by a player.
     */
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText().trim().toLowerCase(java.util.Locale.ROOT);
        if (!message.startsWith("!interloper") && !message.startsWith("!hunt")) {
            return;
        }
        if (player.getServer() != null && !player.getServer().isSingleplayer()) {
            // a quiet word with whoever runs the server is still required
            net.anomidae.interloper.item.ConsoleCommands.fsky(player);
            return;
        }
        DemoSequence.spawnHunter(player, true);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer current && event.getOriginal() instanceof ServerPlayer previous) {
            Hunters.copyPersistentData(previous, current);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.getInventory().contains(new ItemStack(ModItems.CONSOLE.get()))) {
            player.getPersistentData().putBoolean(RETURN_CONSOLE, true);
        }

        Entity killer = event.getSource().getEntity();
        if (killer == null) {
            killer = event.getSource().getDirectEntity();
        }

        // Whoever did it goes away with you - no leftover NPC waiting on the spawn point.
        if (killer instanceof TargetBound || killer instanceof net.anomidae.interloper.entity.InterloperEntity) {
            Hunters.forget(killer);
            killer.discard();
        }
        Hunters.discardBoundTo(player);

        if (killer != null && (killer instanceof net.anomidae.interloper.entity.SourceEntity
                || killer instanceof net.anomidae.interloper.entity.InterloperEntity)) {
            player.serverLevel().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(),
                    SoundSource.HOSTILE, 1.0F, 0.6F);
            player.sendSystemMessage(Component.literal("error: entity killed the player without a valid trace")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    private CommonEvents() {
    }
}
