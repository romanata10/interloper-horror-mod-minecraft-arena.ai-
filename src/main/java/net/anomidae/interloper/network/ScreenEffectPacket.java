package net.anomidae.interloper.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client bound instruction to corrupt the screen for a while.
 */
public class ScreenEffectPacket {
    public enum Effect {
        GLITCH,
        DARKNESS,
        FLASH,
        SHAKE,
        CUBEMAP,
        CAPTURE,
        WHISPER,
        VRAD_ERROR,
        CRASH_TEXT,
        CORRUPT
    }

    private final Effect effect;
    private final int duration;
    private final float intensity;

    public ScreenEffectPacket(Effect effect, int duration) {
        this(effect, duration, 1.0F);
    }

    public ScreenEffectPacket(Effect effect, int duration, float intensity) {
        this.effect = effect;
        this.duration = duration;
        this.intensity = intensity;
    }

    public Effect effect() {
        return effect;
    }

    public int duration() {
        return duration;
    }

    public float intensity() {
        return intensity;
    }

    public static void encode(ScreenEffectPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.effect);
        buf.writeVarInt(packet.duration);
        buf.writeFloat(packet.intensity);
    }

    public static ScreenEffectPacket decode(FriendlyByteBuf buf) {
        return new ScreenEffectPacket(buf.readEnum(Effect.class), buf.readVarInt(), buf.readFloat());
    }
}
