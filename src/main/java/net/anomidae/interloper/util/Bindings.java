package net.anomidae.interloper.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Player lookups that behave the same on singleplayer and on a busy server.
 */
public final class Bindings {
    @Nullable
    public static ServerPlayer online(Entity entity, @Nullable UUID id) {
        if (id == null) {
            return null;
        }
        MinecraftServer server = entity.level().getServer();
        return server == null ? null : server.getPlayerList().getPlayer(id);
    }

    /**
     * A target only counts if it can actually be hunted.
     */
    public static boolean validTarget(@Nullable ServerPlayer player, Entity hunter) {
        return player != null
                && player.isAlive()
                && !player.isSpectator()
                && !player.isCreative()
                && !player.isDeadOrDying()
                && player.level().dimension().equals(hunter.level().dimension());
    }

    @Nullable
    public static ServerPlayer nearest(Entity hunter, double range) {
        var player = hunter.level().getNearestPlayer(hunter, range);
        if (player instanceof ServerPlayer serverPlayer && validTarget(serverPlayer, hunter)) {
            return serverPlayer;
        }
        return null;
    }

    private Bindings() {
    }
}
