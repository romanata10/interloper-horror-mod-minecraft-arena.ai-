package net.anomidae.interloper.util;

import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Text scraped from an engine that has not existed for a very long time.
 */
public final class SourceText {
    public static final List<String> COMMANDS = List.of(
            "sv_cheats 1",
            "sv_gravity 0",
            "noclip",
            "impulse 101",
            "notarget",
            "god",
            "map d1_trainstation_01",
            "connect 127.0.0.1:27015",
            "host_timescale 0.25",
            "mat_crosshair",
            "r_drawothermodels 2",
            "developer 1",
            "ent_fire !self sethealth 0",
            "ai_disable",
            "ent_create prop_physics",
            "cl_showpos 1",
            "sv_portal_placement_never_fail 1",
            "play demo interloper_1",
            "stopdemo",
            "disconnect",
            "quit"
    );

    public static final List<String> CONSOLE_LINES = List.of(
            "Source Engine 2006, build 4044",
            "Console initialized.",
            "Attempting to load vgui2...",
            "Loading unsigned module filesystem_stdio",
            "No SteamRE2 gameinfo.txt found.",
            "exec interloper.cfg",
            "Unknown command: interloper",
            "Demo recording started: interloper_1.dem",
            "maps/interloper_void.bsp loaded",
            "Warning: material 'debug/debugempty' missing",
            "Warning: vtex 'sourcefloor.vtf' has no mip levels",
            "CGameClient::ProcessInput: no local player",
            "Sending client info...",
            "Server: 1 human, 0 bots",
            "Skybox loaded: void_01",
            "ERROR: couldn't find model 'models/interloper.mdl'",
            "using fallback model: models/error.mdl",
            "stringtable dictionary overflow",
            "sv_allow_wait_command 1",
            "The engine remembers the demo.",
            "Playback: 00:00:47 / 00:00:47",
            "Demo playback finished.",
            "Please do not look at the skybox.",
            "sv_cheats has been forced to 0.",
            "No such map: 'minecraft'",
            "Loading source void...",
            "ERROR: map has no entities",
            "Cbuf_Execute: 3 commands",
            "cl_interp 0.1",
            "The umbrella has no shadow."
    );

    public static final List<String> ERRORS = List.of(
            "ERROR: Can't load vtex: materials/vrad/error.vtf",
            "ERROR: Material \"vrad_error\" not found",
            "ERROR: leaf 0 is not in the map",
            "ERROR: TraceLine: no world",
            "ERROR: entity 0 (worldspawn) is corrupt",
            "ERROR: bad surface extents",
            "ERROR: MDL: bad bone index",
            "ERROR: lightmap page overflow",
            "ERROR: cannot resolve stringtable index",
            "ERROR: model has 0 vertices",
            "ERROR: collision mesh missing",
            "ERROR: source void: minecraft",
            "WARNING: NPC has no sequence",
            "ERROR: player origin out of bounds",
            "ERROR: failed to write demo file"
    );

    public static final List<String> LETTERS = List.of(
            "a", "b", "c", "d", "e", "f", "g", "h", "k", "m", "n", "p", "r", "s", "t", "v", "x", "z",
            "A", "B", "C", "D", "E", "F", "G", "K", "M", "N", "P", "R", "S", "T", "V", "X", "Z"
    );

    public static String randomCommand(RandomSource random) {
        return COMMANDS.get(random.nextInt(COMMANDS.size()));
    }

    public static String randomLine(RandomSource random) {
        return CONSOLE_LINES.get(random.nextInt(CONSOLE_LINES.size()));
    }

    public static String randomError(RandomSource random) {
        return ERRORS.get(random.nextInt(ERRORS.size()));
    }

    public static String randomLetters(RandomSource random) {
        int count = 1 + random.nextInt(3);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(LETTERS.get(random.nextInt(LETTERS.size())));
        }
        return builder.toString();
    }

    private SourceText() {
    }
}
