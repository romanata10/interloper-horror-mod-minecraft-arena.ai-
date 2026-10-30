package net.anomidae.interloper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.DemoType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * /interlope - the command that should not exist.
 */
public final class InterloperCommand {
    private static final SimpleCommandExceptionType BAD_EFFECT =
            new SimpleCommandExceptionType(Component.translatable("command.interloper.bad_effect"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(build("interlope"));
        dispatcher.register(build("interloper"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
        return Commands.literal(name)
                .executes(InterloperCommand::executeLore)
                .then(Commands.literal("help").executes(InterloperCommand::executeHelp))
                .then(Commands.literal("hunt").requires(s -> s.hasPermission(2)).executes(InterloperCommand::executeHunt))
                .then(Commands.literal("skyghost").requires(s -> s.hasPermission(2)).executes(InterloperCommand::executeSkyghost))
                .then(Commands.literal("terminal").requires(s -> s.hasPermission(2)).executes(InterloperCommand::executeTerminal))
                .then(Commands.literal("cubemap").executes(ctx -> executeEffect(ctx, "cubemap", 100)))
                .then(Commands.literal("demo")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("type", IntegerArgumentType.integer(1, 5))
                                .executes(InterloperCommand::executeDemo)))
                .then(Commands.literal("effect")
                        .then(Commands.argument("effect", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (ScreenEffectPacket.Effect effect : ScreenEffectPacket.Effect.values()) {
                                        builder.suggest(effect.name().toLowerCase(Locale.ROOT));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(InterloperCommand::executeEffectArgument)))
                .then(Commands.literal("fragment")
                        .requires(s -> s.hasPermission(2))
                        .executes(InterloperCommand::executeFragment));
    }

    private static int executeLore(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        source.sendSuccess(() -> Component.literal("INTERLOPE").withStyle(ChatFormatting.DARK_GRAY), false);
        source.sendSuccess(() -> Component.literal("Unknown or incomplete command, see below for error")
                .withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("interlope<--[HERE]").withStyle(ChatFormatting.RED), false);

        if (player != null) {
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 60, 0.7F));
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.WHISPER, 80, 0.8F));
            player.serverLevel().playSound(null, player.blockPosition(), ModSounds.STATIC.get(),
                    SoundSource.AMBIENT, 1.0F, 0.5F);
        }
        return 1;
    }

    private static int executeHelp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.translatable("command.interloper.help.header")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        source.sendSuccess(() -> Component.translatable("command.interloper.help.lore")
                .withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/interlope demo <1-5>").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/interlope effect <glitch|darkness|flash|shake|cubemap|capture|whisper>")
                .withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/interlope hunt | skyghost | terminal | fragment")
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int executeHunt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        InterloperEntity interloper = ModEntities.INTERLOPER.get().create(level);
        if (interloper == null) {
            return 0;
        }
        Vec3 look = player.getViewVector(1.0F);
        double x = player.getX() - look.x * 22.0D;
        double z = player.getZ() - look.z * 22.0D;
        interloper.moveTo(x, player.getY(), z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        interloper.setTarget(player);
        interloper.setHunting(true);
        level.addFreshEntity(interloper);

        level.playSound(null, player.blockPosition(), ModSounds.STINGER.get(), SoundSource.AMBIENT, 1.0F, 0.6F);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 80, 0.9F));
        source.sendSuccess(() -> Component.translatable("command.interloper.hunt")
                .withStyle(ChatFormatting.DARK_RED), false);
        return 1;
    }

    private static int executeSkyghost(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        SkyghostEntity ghost = ModEntities.SKYGHOST.get().create(level);
        if (ghost == null) {
            return 0;
        }
        Vec3 look = player.getViewVector(1.0F);
        ghost.moveTo(player.getX() - look.x * 20.0D, player.getY() + 2.0D, player.getZ() - look.z * 20.0D,
                0.0F, 0.0F);
        level.addFreshEntity(ghost);
        source.sendSuccess(() -> Component.translatable("command.interloper.skyghost")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int executeTerminal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        var stack = new net.minecraft.world.item.ItemStack(net.anomidae.interloper.registry.ModBlocks.ENGINE_TERMINAL_ITEM.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return 1;
    }

    private static int executeFragment(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        var stack = new net.minecraft.world.item.ItemStack(ModItems.SKYBOX_FRAGMENT.get(), 4);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return 1;
    }

    private static int executeDemo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int type = IntegerArgumentType.getInteger(ctx, "type");
        var stack = ModItems.demoTape(DemoType.byId(type));
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return 1;
    }

    private static int executeEffectArgument(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "effect").toUpperCase(Locale.ROOT);
        try {
            ScreenEffectPacket.Effect.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw BAD_EFFECT.create();
        }
        return executeEffect(ctx, name, 120);
    }

    private static int executeEffect(CommandContext<CommandSourceStack> ctx, String name, int duration) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        ScreenEffectPacket.Effect effect;
        try {
            effect = ScreenEffectPacket.Effect.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return 0;
        }
        if (player != null) {
            ModNetwork.toPlayer(player, new ScreenEffectPacket(effect, duration, 1.0F));
        }
        return 1;
    }

    private InterloperCommand() {
    }
}
