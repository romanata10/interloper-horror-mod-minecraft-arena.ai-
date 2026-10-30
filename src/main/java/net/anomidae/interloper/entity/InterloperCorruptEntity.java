package net.anomidae.interloper.entity;

import net.anomidae.interloper.util.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The other one. Taller, faster, and its lower half and umbrella are made of
 * the texture that the engine uses when a texture is missing.
 */
public class InterloperCorruptEntity extends InterloperEntity {
    public InterloperCorruptEntity(EntityType<? extends InterloperCorruptEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    @Override
    protected double flightSpeed() {
        return 0.42D;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void onTouch(ServerPlayer player) {
        player.hurt(player.level().damageSources().genericKill(), 1000.0F);
        ServerScheduler.schedule(30, () -> {
            if (player.connection != null) {
                player.connection.disconnect(Component.literal("error:crash_by-error\ninterloper_corrupt"));
            }
        });
    }
}
