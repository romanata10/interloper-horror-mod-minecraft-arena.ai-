package net.anomidae.interloper.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * What the player typed into the console.
 */
public class ConsoleCommandPacket {
    private final String command;

    public ConsoleCommandPacket(String command) {
        this.command = command;
    }

    public String command() {
        return command;
    }

    public static void encode(ConsoleCommandPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.command, 512);
    }

    public static ConsoleCommandPacket decode(FriendlyByteBuf buf) {
        return new ConsoleCommandPacket(buf.readUtf(512));
    }
}
