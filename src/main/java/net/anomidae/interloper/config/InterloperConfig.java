package net.anomidae.interloper.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config. Everything spooky can be dialled down here.
 */
public final class InterloperConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AMBIENCE_ENABLED;
    public static final ForgeConfigSpec.IntValue AMBIENCE_INTERVAL;
    public static final ForgeConfigSpec.BooleanValue FLASHING_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_CHAT;
    public static final ForgeConfigSpec.BooleanValue NATURAL_INTERLOPER;
    public static final ForgeConfigSpec.IntValue INTERLOPER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue NATURAL_SKYGHOST;
    public static final ForgeConfigSpec.BooleanValue SCANNER_GLITCH;
    public static final ForgeConfigSpec.BooleanValue INTRO_MESSAGE;
    public static final ForgeConfigSpec.IntValue CRASH_COOLDOWN;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Ambience / random horror events").push("ambience");
        AMBIENCE_ENABLED = b.comment("Master switch for random horror events.").define("enabled", true);
        AMBIENCE_INTERVAL = b.comment("Average ticks between random events per level (20 ticks = 1 second). Higher = rarer. Default 900 (~45s).")
                .defineInRange("interval", 900, 100, 72000);
        AMBIENT_CHAT = b.comment("Allow the mod to whisper into the chat.").define("chat_messages", true);
        b.pop();

        b.comment("Entities").push("entities");
        NATURAL_INTERLOPER = b.comment("Allow the Interloper to hunt players on its own at night.").define("natural_interloper", true);
        INTERLOPER_SPAWN_CHANCE = b.comment("1 in N chance per ambience roll to start a hunt (night only).").defineInRange("hunt_chance", 6, 1, 100);
        NATURAL_SKYGHOST = b.comment("Allow the Skyghost to watch from the distance.").define("natural_skyghost", true);
        CRASH_COOLDOWN = b.comment("Seconds a player is left alone after the Interloper crashed them out. " +
                        "During this time the Interloper still appears but only vanishes instead of kicking.")
                .defineInRange("crash_cooldown_seconds", 180, 0, 3600);
        b.pop();

        b.comment("Visuals").push("visuals");
        FLASHING_EFFECTS = b.comment("Flashing / cubemap sequence effects. Disable if you are photosensitive.").define("flashing", true);
        SCANNER_GLITCH = b.comment("The anomaly scanner corruppts your screen when something is close.").define("scanner_glitch", true);
        INTRO_MESSAGE = b.comment("Show the greeting message when a player joins.").define("intro_message", true);
        b.pop();

        SPEC = b.build();
    }

    private InterloperConfig() {
    }
}
