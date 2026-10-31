package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.ConeEntity;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.anomidae.interloper.entity.CrasherEntity;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.entity.VradEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.util.SourceText;
import net.anomidae.interloper.world.DemoSequence;
import net.anomidae.interloper.world.Dimensions;
import net.anomidae.interloper.world.SourceVoid;
import net.anomidae.interloper.world.WhiteDoor;
import net.anomidae.interloper.world.XboxDreams;
import net.anomidae.interloper.world.VoidDirector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * What the console accepts. Most commands do nothing, which is the point.
 */
public final class ConsoleCommands {
    public static void execute(ServerPlayer player, String raw) {
        String input = raw == null ? "" : raw.trim();
        if (input.isEmpty()) {
            return;
        }
        line(player, "] " + input, 0xFFFFFFFF, true);
        String lower = input.toLowerCase(Locale.ROOT);
        String[] parts = lower.split("\\s+");

        if (lower.startsWith("play demo") || lower.startsWith("demo") || lower.startsWith("playdemo")) {
            int id = 1;
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    id = Integer.parseInt(part);
                    break;
                }
            }
            DemoSequence.play(player, id);
            return;
        }

        switch (parts[0]) {
            case "help" -> {
                line(player, "Console commands:", 0xFFCFCFCF, false);
                line(player, "  play demo <1..5>  - play a demo file", 0xFF9A9A9A, false);
                line(player, "  stopdemo          - stop playback", 0xFF9A9A9A, false);
                line(player, "  status            - engine status", 0xFF9A9A9A, false);
                line(player, "  sv_cheats 1       - enable cheating", 0xFF9A9A9A, false);
                line(player, "  noclip            - toggle noclip", 0xFF9A9A9A, false);
                line(player, "  disconnect        - leave the game", 0xFF9A9A9A, false);
                line(player, "  source_void       - go to the source void", 0xFF9A9A9A, false);
                line(player, "  spawn <name>      - spawn an entity", 0xFF9A9A9A, false);
                line(player, "  xbox              - go to xbox 360 dreams", 0xFF9A9A9A, false);
                line(player, "  white_door        - go to white_door_bsp", 0xFF9A9A9A, false);
            }
            case "sv_cheats" -> fsky(player);
            case "status" -> {
                line(player, "hostname: source void", 0xFFFFFFFF, false);
                line(player, "map     : " + player.level().dimension().location().getPath(), 0xFFFFFFFF, false);
                line(player, "players : 1 human, 0 bots", 0xFFFFFFFF, false);
                line(player, "build   : 4044 (interloper)", 0xFFFFFFFF, false);
            }
            case "noclip", "god", "impulse", "notarget", "map", "connect", "developer", "mat_crosshair",
                 "impulse_101", "ai_disable", "ent_fire", "host_timescale", "cl_showpos", "playdemo" -> fsky(player);
            case "stopdemo" -> line(player, "No demo playing.", 0xFFCFCFCF, false);
            case "disconnect", "quit", "exit" -> {
                line(player, "Disconnecting...", 0xFFFFA500, true);
                player.connection.disconnect(net.minecraft.network.chat.Component.literal("error:crash_by-error"));
            }
            case "source_void", "void" -> {
                line(player, "Loading source_void.bsp ...", 0xFFCFCFCF, false);
                SourceVoid.enter(player);
            }
            case "xbox", "xbox_360_dreams", "dreams" -> XboxDreams.enter(player);
            case "white_door", "white_door_bsp" -> WhiteDoor.enter(player);
            case "spawn", "ent_create" -> {
                if (parts.length < 2) {
                    line(player, "usage: spawn <corrupted_npc|vrad|crasher|cone|dmnpc|skyghost|interloper|interloper_corrupt>",
                            0xFFCFCFCF, false);
                    return;
                }
                spawn(player, parts[1]);
            }
            default -> {
                // every Source command ends the same way on server FSKY
                fsky(player);
                if (player.getRandom().nextInt(3) == 0) {
                    line(player, SourceText.randomError(player.getRandom()), 0xFFFF7A00, false);
                }
            }
        }
    }

    private static void spawn(ServerPlayer player, String name) {
        ServerLevel level = player.serverLevel();
        EntityType<? extends Mob> type = switch (name) {
            case "corrupted_npc", "corrupted" -> ModEntities.CORRUPTED_NPC.get();
            case "vrad", "error" -> ModEntities.VRAD.get();
            case "crasher" -> ModEntities.CRASHER.get();
            case "cone" -> ModEntities.CONE.get();
            case "dmnpc", "enderman" -> ModEntities.DM_NPC.get();
            case "skyghost" -> ModEntities.SKYGHOST.get();
            case "broken", "broken_character" -> ModEntities.BROKEN_CHARACTER.get();
            case "interloper" -> ModEntities.INTERLOPER.get();
            case "interloper_corrupt", "corrupt" -> ModEntities.INTERLOPER_CORRUPT.get();
            default -> null;
        };
        if (type == null) {
            line(player, "No such entity: " + name, 0xFFFFA500, false);
            return;
        }
        Vec3 look = player.getViewVector(1.0F);
        BlockPos pos = BlockPos.containing(player.getX() + look.x * 4.0D,
                player.getY() + 1.0D, player.getZ() + look.z * 4.0D);
        Entity entity = type.create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot(), 0.0F);
        if (entity instanceof net.anomidae.interloper.util.TargetBound bound) {
            bound.bindPlayer(player.getUUID());
        }
        level.addFreshEntity(entity);
        if (entity instanceof InterloperEntity interloper && !(entity instanceof InterloperCorruptEntity)) {
            interloper.setHunting(true);
            interloper.setTarget(player);
        }
        line(player, "spawned " + name, 0xFFCFCFCF, false);
    }

    /**
     * The only answer this server ever gives to an admin command.
     */
    public static void fsky(ServerPlayer player) {
        line(player, "you dont have admin comands on server FSKY", 0xFFFFA500, true);
    }

    public static void line(ServerPlayer player, String text, int color, boolean chat) {
        ModNetwork.toPlayer(player, new ConsoleLinePacket(text, color, chat));
    }

    public static void bootConsole(ServerPlayer player) {
        line(player, "Source Engine 2006, build 4044", 0xFFCFCFCF, false);
        line(player, "Console initialized.", 0xFFCFCFCF, false);
        line(player, "Loading map: minecraft ...", 0xFFCFCFCF, false);
        line(player, "sv_cheats has been forced to 0.", 0xFFFFA500, false);
        line(player, "type 'play demo interloper_1' and press ENTER", 0xFFFFFFFF, false);
        line(player, "]", 0xFFFFFFFF, false);
    }

    private ConsoleCommands() {
    }
}
