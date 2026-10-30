package net.anomidae.interloper.network;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.client.ClientPacketHandler;
import net.anomidae.interloper.item.ConsoleCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "2";
    private static int nextId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Interloper.id("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void init() {
        CHANNEL.messageBuilder(ScreenEffectPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScreenEffectPacket::encode)
                .decoder(ScreenEffectPacket::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleScreenEffect(msg)))
                .add();

        CHANNEL.messageBuilder(ChatGhostPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChatGhostPacket::encode)
                .decoder(ChatGhostPacket::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleChat(msg)))
                .add();

        CHANNEL.messageBuilder(ConsoleLinePacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ConsoleLinePacket::encode)
                .decoder(ConsoleLinePacket::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleConsoleLine(msg)))
                .add();

        CHANNEL.messageBuilder(ConsoleCommandPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ConsoleCommandPacket::encode)
                .decoder(ConsoleCommandPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ServerPlayer sender = ctx.get().getSender();
                    if (sender != null) {
                        ConsoleCommands.execute(sender, msg.command());
                    }
                })
                .add();
    }

    public static void toPlayer(ServerPlayer player, Object packet) {
        if (player == null) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    private ModNetwork() {
    }
}
