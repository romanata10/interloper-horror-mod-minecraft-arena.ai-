package net.anomidae.interloper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.anomidae.interloper.item.ConsoleCommands;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.world.DemoSequence;
import net.anomidae.interloper.world.SourceVoid;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * /interloper - the command that should not exist.
 */
public final class InterloperCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("interloper")
                .executes(InterloperCommand::help)
                .then(Commands.literal("help").executes(InterloperCommand::help))
                .then(Commands.literal("hunt").executes(InterloperCommand::hunt))
                .then(Commands.literal("void").executes(InterloperCommand::enterVoid))
                .then(Commands.literal("exit").executes(InterloperCommand::leaveVoid))
                .then(Commands.literal("console").executes(InterloperCommand::console))
                .then(Commands.literal("demo")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1, 5))
                                .executes(context -> demo(context, IntegerArgumentType.getInteger(context, "id")))))
                .then(Commands.literal("spawn")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> spawn(context, StringArgumentType.getString(context, "name")))));

        dispatcher.register(root);
        dispatcher.register(Commands.literal("interlope").redirect(root.build()));
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("/interloper hunt       - start a hunt"), false);
        source.sendSuccess(() -> Component.literal("/interloper demo <1-5> - play a demo"), false);
        source.sendSuccess(() -> Component.literal("/interloper void       - go to the source void"), false);
        source.sendSuccess(() -> Component.literal("/interloper exit       - leave the source void"), false);
        source.sendSuccess(() -> Component.literal("/interloper console    - receive the console"), false);
        source.sendSuccess(() -> Component.literal("/interloper spawn <entity>"), false);
        return 1;
    }

    private static int hunt(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        DemoSequence.spawnHunter(player, true);
        return 1;
    }

    private static int enterVoid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return SourceVoid.enter(player) ? 1 : 0;
    }

    private static int leaveVoid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return SourceVoid.exit(player) ? 1 : 0;
    }

    private static int console(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.getInventory().contains(new ItemStack(ModItems.CONSOLE.get()))) {
            player.getInventory().add(new ItemStack(ModItems.CONSOLE.get()));
        }
        ConsoleCommands.bootConsole(player);
        player.sendSystemMessage(Component.literal("console received").withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    private static int demo(CommandContext<CommandSourceStack> context, int id) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        DemoSequence.play(player, id);
        return 1;
    }

    private static int spawn(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ConsoleCommands.execute(player, "spawn " + name);
        return 1;
    }

    private InterloperCommand() {
    }
}
