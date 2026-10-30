package net.anomidae.interloper.client;

import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.MthHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * All client side screen corruption timers live here.
 */
public final class ClientEffects {
    public static int glitchTicks;
    public static int darkTicks;
    public static int flashTicks;
    public static int shakeTicks;
    public static int cubemapTicks;
    public static int captureTicks;
    public static int whisperTicks;

    public static float glitchIntensity = 1.0F;
    public static float darkIntensity = 1.0F;
    public static float proximity;

    public static void trigger(ScreenEffectPacket.Effect effect, int duration, float intensity) {
        duration = Math.max(1, duration);
        boolean flashing = InterloperConfig.FLASHING_EFFECTS.get();

        switch (effect) {
            case GLITCH -> {
                glitchTicks = Math.max(glitchTicks, duration);
                glitchIntensity = MthHelper.clamp(intensity, 0.1F, 1.0F);
                play(ModSounds.STATIC.get(), 0.9F, 0.7F);
            }
            case DARKNESS -> {
                darkTicks = Math.max(darkTicks, duration);
                darkIntensity = MthHelper.clamp(intensity, 0.2F, 1.0F);
            }
            case FLASH -> {
                if (flashing) {
                    flashTicks = Math.max(flashTicks, Math.min(duration, 40));
                    play(ModSounds.STINGER.get(), 0.8F, 1.2F);
                }
            }
            case SHAKE -> shakeTicks = Math.max(shakeTicks, duration);
            case CUBEMAP -> {
                if (flashing) {
                    cubemapTicks = Math.max(cubemapTicks, duration);
                    play(ModSounds.CUBEMAP.get(), 1.0F, 0.9F);
                }
            }
            case CAPTURE -> {
                captureTicks = Math.max(captureTicks, duration);
                play(ModSounds.CAPTURE.get(), 1.0F, 0.9F);
            }
            case WHISPER -> {
                whisperTicks = Math.max(whisperTicks, duration);
                play(ModSounds.WHISPER.get(), 0.9F, 0.8F);
            }
        }
    }

    private static void play(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            player.playSound(sound, volume, pitch);
        }
    }

    public static void tick() {
        if (glitchTicks > 0) glitchTicks--;
        if (darkTicks > 0) darkTicks--;
        if (flashTicks > 0) flashTicks--;
        if (shakeTicks > 0) shakeTicks--;
        if (cubemapTicks > 0) cubemapTicks--;
        if (captureTicks > 0) captureTicks--;
        if (whisperTicks > 0) whisperTicks--;

        if (glitchTicks == 0) glitchIntensity = 0.0F;
        if (darkTicks == 0) darkIntensity = 0.0F;
    }

    public static float shakeFactor(float partialTick) {
        if (shakeTicks <= 0 && captureTicks <= 0) {
            return 0.0F;
        }
        float base = shakeTicks > 0 ? 1.0F : 0.0F;
        float capture = captureTicks > 0 ? 0.8F : 0.0F;
        return Math.max(base, capture);
    }

    public static float fovDistortion() {
        float factor = 0.0F;
        if (captureTicks > 0) factor = Math.max(factor, 0.9F);
        if (cubemapTicks > 0) factor = Math.max(factor, 0.7F);
        if (glitchTicks > 0) factor = Math.max(factor, 0.35F * glitchIntensity);
        return factor;
    }

    public static void reset() {
        glitchTicks = 0;
        darkTicks = 0;
        flashTicks = 0;
        shakeTicks = 0;
        cubemapTicks = 0;
        captureTicks = 0;
        whisperTicks = 0;
        glitchIntensity = 0.0F;
        darkIntensity = 0.0F;
        proximity = 0.0F;
    }

    private ClientEffects() {
    }
}
