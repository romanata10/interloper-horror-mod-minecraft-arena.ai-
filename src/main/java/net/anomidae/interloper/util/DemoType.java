package net.anomidae.interloper.util;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

/**
 * The five known demo types documented by the investigator.
 * Each tape plays back a fragment of the recording.
 */
public enum DemoType {
    TYPE_1(1, "flail"),
    TYPE_2(2, "static"),
    TYPE_3(3, "aimless"),
    TYPE_4(4, "out_of_bounds"),
    TYPE_5(5, "dmnpc");

    public static final String TAG = "DemoType";
    private static final DemoType[] BY_ID = new DemoType[6];

    static {
        for (DemoType type : values()) {
            BY_ID[type.id] = type;
        }
    }

    private final int id;
    private final String key;

    DemoType(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int id() {
        return id;
    }

    public String nameKey() {
        return "demo." + Interloper.MODID + "." + key;
    }

    public String logKey(int line) {
        return "demo." + Interloper.MODID + "." + key + ".log" + line;
    }

    public net.minecraft.network.chat.MutableComponent displayName() {
        return Component.translatable("item." + Interloper.MODID + ".demo_tape.named", Component.translatable(nameKey()));
    }

    public net.minecraft.network.chat.MutableComponent description() {
        return Component.translatable("demo." + Interloper.MODID + "." + key + ".desc");
    }

    public void write(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG, id);
    }

    public static DemoType byId(int id) {
        if (id >= 0 && id < BY_ID.length && BY_ID[id] != null) {
            return BY_ID[id];
        }
        return TYPE_1;
    }

    public static DemoType read(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) {
            return TYPE_1;
        }
        return byId(tag.getInt(TAG));
    }

    public static DemoType random(RandomSource random) {
        return values()[random.nextInt(values().length)];
    }

    /**
     * Plays the recording back to the player. Called server side.
     */
    public void play(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        MinecraftServer server = player.server;
        RandomSource random = level.getRandom();

        switch (this) {
            case TYPE_1 -> {
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 120));
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 80, 0.6F));
                level.playSound(null, player.blockPosition(), ModSounds.STATIC.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
                send(player, "demo.interloper.type_1.log1", 20);
                send(player, "demo.interloper.type_1.log2", 60);
                send(player, "demo.interloper.type_1.log3", 110);
            }
            case TYPE_2 -> {
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 160, 1.0F));
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CUBEMAP, 60));
                level.playSound(null, player.blockPosition(), ModSounds.CUBEMAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
                send(player, "demo.interloper.type_2.log1", 25);
                send(player, "demo.interloper.type_2.log2", 90);
                send(player, "demo.interloper.type_2.log3", 150);
            }
            case TYPE_3 -> {
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.DARKNESS, 140, 0.5F));
                level.playSound(null, player.blockPosition(), ModSounds.WHISPER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                send(player, "demo.interloper.type_3.log1", 20);
                send(player, "demo.interloper.type_3.log2", 70);
                send(player, "demo.interloper.type_3.log3", 130);
            }
            case TYPE_4 -> {
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.DARKNESS, 200, 0.9F));
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 200));
                level.playSound(null, player.blockPosition(), ModSounds.CUBEMAP.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
                send(player, "demo.interloper.type_4.log1", 20);
                send(player, "demo.interloper.type_4.log2", 80);
                send(player, "demo.interloper.type_4.log3", 150);
                send(player, "demo.interloper.type_4.log4", 210);
                if (random.nextFloat() < 0.35F) {
                    server.tell(new TickTask(server.getTickCount() + 120, () -> {
                        if (player.isAlive()) {
                            level.playSound(null, player.blockPosition(), ModSounds.STINGER.get(), SoundSource.AMBIENT, 1.0F, 0.7F);
                        }
                    }));
                }
            }
            case TYPE_5 -> {
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CAPTURE, 220, 1.0F));
                ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CUBEMAP, 100));
                level.playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                send(player, "demo.interloper.type_5.log1", 20);
                send(player, "demo.interloper.type_5.log2", 60);
                send(player, "demo.interloper.type_5.log3", 120);
                send(player, "demo.interloper.type_5.log4", 180);
                server.tell(new TickTask(server.getTickCount() + 240, () -> {
                    if (player.isAlive() && random.nextFloat() < 0.5F) {
                        var spawned = ModEntities.INTERLOPER.get().spawn(level, player.blockPosition().offset(
                                random.nextInt(20) - 10, 0, random.nextInt(20) - 10), MobSpawnType.EVENT);
                        if (spawned != null) {
                            spawned.setTarget(player);
                        }
                    }
                }));
            }
        }
    }

    private static void send(ServerPlayer player, String key, int delayTicks) {
        MinecraftServer server = player.server;
        server.tell(new TickTask(server.getTickCount() + delayTicks,
                () -> ModNetwork.toPlayer(player, new ChatGhostPacket(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY)))));
    }
}
