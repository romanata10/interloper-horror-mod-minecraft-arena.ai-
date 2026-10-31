package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The other one. Taller, faster, and its lower half and umbrella are made of
 * the texture that the engine uses when a texture is missing.
 *
 * It kills you, deletes itself, and then the client dies with it.
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

    /**
     * If it dies, it leaves the box it was using to look at the anomalies.
     */
    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting,
                                       boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                net.anomidae.interloper.registry.ModItems.SOURCE_BOX.get()));
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                net.anomidae.interloper.registry.ModItems.BROKEN_NPC_DEBRIS.get(),
                1 + this.random.nextInt(3)));
    }

    /**
     * The flashlight nuke is the only thing in the game that can end this one.
     * Called by the item, because it never goes through normal damage.
     */
    public void breakByNuke() {
        Hunters.forget(this);
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                net.anomidae.interloper.registry.ModItems.SOURCE_BOX.get()));
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                net.anomidae.interloper.registry.ModItems.BROKEN_NPC_DEBRIS.get(),
                1 + this.random.nextInt(3)));
        this.discard();
    }

    @Override
    protected void onTouch(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
            this.finish();
            return;
        }

        Hunters.startCrashCooldown(player);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CRASH_TEXT, 60, 1.0F));
        player.hurt(this.damageSources().mobAttack(this), 1000.0F);

        // the entity goes away with the player
        this.finish();
        Hunters.discardBoundTo(player);

        ServerScheduler.schedule(30, () -> {
            if (Hunters.isOnline(player) && player.connection != null) {
                player.connection.disconnect(Component.literal("error:crash_by-error\ninterloper_corrupt"));
            }
        });
    }
}
