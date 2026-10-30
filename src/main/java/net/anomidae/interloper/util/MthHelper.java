package net.anomidae.interloper.util;

public final class MthHelper {
    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    private MthHelper() {
    }
}
