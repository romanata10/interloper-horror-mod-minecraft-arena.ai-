package net.anomidae.interloper.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * A message that arrives in chat without any player having written it.
 */
public class ChatGhostPacket {
    private final Component message;

    public ChatGhostPacket(Component message) {
        this.message = message;
    }

    public Component message() {
        return message;
    }

    public static void encode(ChatGhostPacket packet, FriendlyByteBuf buf) {
        buf.writeComponent(packet.message);
    }

    public static ChatGhostPacket decode(FriendlyByteBuf buf) {
        return new ChatGhostPacket(buf.readComponent());
    }
}
