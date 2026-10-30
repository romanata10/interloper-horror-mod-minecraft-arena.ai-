package net.anomidae.interloper.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tiny server side delay queue. Everything in this mod runs on the server tick,
 * so a list with due ticks is enough and it survives reloads of the client.
 */
public final class ServerScheduler {
    private static final List<Task> TASKS = new ArrayList<>();
    private static long ticks;

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(ticks + Math.max(1, delayTicks), action));
    }

    public static void tick() {
        ticks++;
        Iterator<Task> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.due > ticks) {
                continue;
            }
            iterator.remove();
            try {
                task.action.run();
            } catch (Exception ignored) {
                // a broken task must never take the server down
            }
        }
    }

    public static void reset() {
        TASKS.clear();
        ticks = 0;
    }

    private record Task(long due, Runnable action) {
    }

    private ServerScheduler() {
    }
}
