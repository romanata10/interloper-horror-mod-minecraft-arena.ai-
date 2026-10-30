package net.anomidae.interloper.client;

import net.anomidae.interloper.client.screen.ConsoleScreen;
import net.minecraft.client.Minecraft;

/**
 * Client entry point so that the server never touches a Screen class.
 */
public final class ConsoleOpener {
    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ConsoleScreen) {
            return;
        }
        minecraft.setScreen(new ConsoleScreen());
    }

    private ConsoleOpener() {
    }
}
