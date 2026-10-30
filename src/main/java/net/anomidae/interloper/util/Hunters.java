package net.anomidae.interloper.util;

import net.anomidae.interloper.config.InterloperConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Bookkeeping for entities that belong to a player.
 *
 * Everything spawned by this mod is bound to exactly one player, and every hunt
 * ends when that player dies, disconnects or is kicked. Without this, a leftover
 * Interloper would keep kicking the same player the moment they reconnect.
 */
public final class Hunters {
    private static final String CRASH_COOLDOWN_TAG = "interloper.crash_cooldown";
    private static final Set<Entity> TRACKED =
            Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    public static void track(Entity entity) {
        if (entity != null && !entity.level().isClientSide) {
            TRACKED.add(entity);
        }
    }

    public static void forget(Entity entity) {
        TRACKED.remove(entity);
    }

    /**
     * Removes every hunt that belongs to this player. Safe to call on death,
     * on logout and after a crash kick.
     */
    public static void discardBoundTo(@Nullable ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUUID();
        List<Entity> doomed = new ArrayList<>();
        synchronized (TRACKED) {
            Iterator<Entity> iterator = TRACKED.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (entity == null || entity.isRemoved()) {
                    iterator.remove();
                    continue;
                }
                if (entity instanceof TargetBound bound && bound.isBoundTo(id)) {
                    doomed.add(entity);
                    iterator.remove();
                }
            }
        }
        // discard outside the iteration: removing an entity notifies this class again
        for (Entity entity : doomed) {
            entity.discard();
        }
    }

    public static boolean isOnline(@Nullable ServerPlayer player) {
        if (player == null) {
            return false;
        }
        MinecraftServer server = player.getServer();
        return server != null && server.getPlayerList().getPlayer(player.getUUID()) == player;
    }

    /**
     * How long a player is left alone after being crashed out of the game.
     */
    public static int cooldownTicks() {
        try {
            return Math.max(200, InterloperConfig.CRASH_COOLDOWN.get() * 20);
        } catch (Exception ignored) {
            return 3600;
        }
    }

    public static void startCrashCooldown(@Nullable ServerPlayer player) {
        if (player == null) {
            return;
        }
        player.getPersistentData().putLong(CRASH_COOLDOWN_TAG, now(player) + cooldownTicks());
    }

    public static boolean isOnCrashCooldown(@Nullable ServerPlayer player) {
        if (player == null) {
            return false;
        }
        long until = player.getPersistentData().getLong(CRASH_COOLDOWN_TAG);
        return until > now(player);
    }

    public static void clearCrashCooldown(@Nullable ServerPlayer player) {
        if (player != null) {
            player.getPersistentData().remove(CRASH_COOLDOWN_TAG);
        }
    }

    private static long now(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server == null ? 0L : server.overworld().getGameTime();
    }

    /**
     * Keeps player flags (void state, cooldown) across death and respawn.
     */
    public static void copyPersistentData(ServerPlayer from, ServerPlayer to) {
        if (from == null || to == null) {
            return;
        }
        var source = from.getPersistentData();
        var target = to.getPersistentData();
        Set<String> keys = new HashSet<>();
        source.getAllKeys().forEach(keys::add);
        for (String key : keys) {
            if (!key.startsWith("interloper.")) {
                continue;
            }
            var value = source.get(key);
            if (value != null) {
                target.put(key, value.copy());
            }
        }
    }

    private Hunters() {
    }
}
