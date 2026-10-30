package net.anomidae.interloper.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side copy of the engine log. It never clears on its own.
 */
public final class ConsoleLog {
    public record Line(String text, int color) {
    }

    private static final List<Line> LINES = new ArrayList<>();
    private static final int MAX = 400;

    public static void add(String text, int color) {
        LINES.add(new Line(text, color));
        while (LINES.size() > MAX) {
            LINES.remove(0);
        }
    }

    public static List<Line> lines() {
        return LINES;
    }

    public static boolean isEmpty() {
        return LINES.isEmpty();
    }

    public static void clear() {
        LINES.clear();
    }

    private ConsoleLog() {
    }
}
