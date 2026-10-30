package net.anomidae.interloper.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A line of engine output. It goes to the console and, optionally, to chat.
 */
public class ConsoleLinePacket {
    private final String text;
    private final int color;
    private final boolean chat;

    public ConsoleLinePacket(String text, int color, boolean chat) {
        this.text = text;
        this.color = color;
        this.chat = chat;
    }

    public String text() {
        return text;
    }

    public int color() {
        return color;
    }

    public boolean chat() {
        return chat;
    }

    public static void encode(ConsoleLinePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.text, 512);
        buf.writeInt(packet.color);
        buf.writeBoolean(packet.chat);
    }

    public static ConsoleLinePacket decode(FriendlyByteBuf buf) {
        return new ConsoleLinePacket(buf.readUtf(512), buf.readInt(), buf.readBoolean());
    }
}
