package net.anomidae.interloper.entity;

import net.anomidae.interloper.util.Bindings;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.MobGrounding;
import net.anomidae.interloper.util.TargetBound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Base class for everything that walks through the Source Void.
 * No collision, no gravity, no pathfinding - only intent, and a single target.
 */
public abstract class SourceEntity extends Monster implements TargetBound {
    private static final int LOST_TARGET_GRACE = 60;

    @Nullable
    private UUID targetUuid;
    private int lostTargetTicks;

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
    public @Nullable UUID boundPlayerId() {
        return this.targetUuid;
    }

    @Override
    public void bindPlayer(@Nullable UUID playerId) {
        this.targetUuid = playerId;
        this.lostTargetTicks = 0;
        if (playerId != null && !this.level().isClientSide) {
            Hunters.track(this);
        }
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

    /**
     * The player this entity hunts. Binds to the nearest player the first time,
     * then never switches - important on a server with more than one player.
     *
     * Returns null and eventually discards the entity if that player is gone.
     */
    @Nullable
    protected ServerPlayer resolveTarget(double range) {
        if (this.isRemoved()) {
            return null;
        }
        UUID id = this.targetUuid;
        if (id != null) {
            ServerPlayer bound = Bindings.online(this, id);
            if (bound != null && Bindings.validTarget(bound, this)) {
                this.lostTargetTicks = 0;
                return bound;
            }
            if (++this.lostTargetTicks > LOST_TARGET_GRACE) {
                this.abandonHunt();
            }
            return null;
        }

        ServerPlayer nearest = Bindings.nearest(this, range);
        if (nearest != null) {
            this.bindPlayer(nearest.getUUID());
            return nearest;
        }
        return null;
    }

    /**
     * Called when the target is dead, offline or in another dimension.
     */
    protected void abandonHunt() {
        Hunters.forget(this);
        this.discard();
    }

    protected void snapToGround(int maxDrop) {
        MobGrounding.snap(this, maxDrop);
    }

    protected boolean touching(ServerPlayer player) {
        return this.getBoundingBox().inflate(0.45D).intersects(player.getBoundingBox());
    }

    protected MinecraftServer server() {
        return this.level().getServer();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetUuid != null) {
            tag.putUUID("HuntTarget", this.targetUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("HuntTarget")) {
            this.bindPlayer(tag.getUUID("HuntTarget"));
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        Hunters.forget(this);
        super.remove(reason);
    }
}
