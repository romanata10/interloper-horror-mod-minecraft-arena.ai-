package net.anomidae.interloper.entity;

import net.anomidae.interloper.util.MobGrounding;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Base class for everything that walks through the Source Void.
 * No collision, no gravity, no pathfinding - only intent.
 */
public abstract class SourceEntity extends Monster {
    protected SourceEntity(EntityType<? extends SourceEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        // Intent is handled manually, see serverTick().
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.serverTick();
    }

    protected abstract void serverTick();

    protected ServerPlayer nearestPlayer(double range) {
        var player = this.level().getNearestPlayer(this, range);
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    protected void snapToGround(int maxDrop) {
        MobGrounding.snap(this, maxDrop);
    }

    protected boolean touching(ServerPlayer player) {
        return this.getBoundingBox().inflate(0.45D).intersects(player.getBoundingBox());
    }
}
