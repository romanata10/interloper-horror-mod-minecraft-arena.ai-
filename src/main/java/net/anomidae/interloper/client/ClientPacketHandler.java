package net.anomidae.interloper.client;

import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {
    public static void handleScreenEffect(ScreenEffectPacket packet) {
        ClientEffects.trigger(packet.effect(), packet.duration(), packet.intensity());
    }

    public static void handleChat(ChatGhostPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(packet.message());
        }
    }

    private ClientPacketHandler() {
    }
}
