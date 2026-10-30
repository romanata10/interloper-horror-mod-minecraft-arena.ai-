package net.anomidae.interloper.client;

import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

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

    public static void handleConsoleLine(ConsoleLinePacket packet) {
        ConsoleLog.add(packet.text(), packet.color());
        if (!packet.chat()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            ChatComponent chat = mc.gui.getChat();
            chat.addMessage(Component.literal(packet.text()).withStyle(style -> style.withColor(packet.color())));
        }
    }

    private ClientPacketHandler() {
    }
}
