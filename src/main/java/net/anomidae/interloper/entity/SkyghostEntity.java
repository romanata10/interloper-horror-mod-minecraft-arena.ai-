package net.anomidae.interloper.entity;

import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * The thing that watches from the edge of the render distance.
 * It never moves while you are looking at it.
 */
public class SkyghostEntity extends Monster {
    private int lifeTicks;
    private int hopCooldown = 20;

    public SkyghostEntity(EntityType<? extends SkyghostEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        // It does not walk. It is simply there.
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isOnFire()) {
            this.clearFire();
        }

        if (this.level().isClientSide) {
            return;
        }

        this.lifeTicks++;
        if (this.lifeTicks > 20 * 90) {
            this.discard();
            return;
        }

        Player player = this.level().getNearestPlayer(this, 64.0D);
        if (player == null) {
            return;
        }

        this.getLookControl().setLookAt(player, 40.0F, 40.0F);

        double distance = this.distanceTo(player);
        if (distance < 6.0D) {
            this.leave();
            return;
        }

        if (--this.hopCooldown > 0) {
            return;
        }
        this.hopCooldown = 30 + this.random.nextInt(40);

        if (this.isWatchedBy(player)) {
            // frozen in place, watching back
            if (this.random.nextInt(4) == 0) {
                this.level().playSound(null, this.blockPosition(), ModSounds.WHISPER.get(),
                        SoundSource.AMBIENT, 0.6F, 0.7F);
            }
            return;
        }

        if (distance > 16.0D) {
            Vec3 look = player.getViewVector(1.0F);
            double hop = 14.0D + this.random.nextDouble() * 8.0D;
            double x = player.getX() - look.x * hop;
            double z = player.getZ() - look.z * hop;
            double y = player.getY() + 1.0D + this.random.nextDouble() * 2.5D;
            this.randomTeleport(x, y, z, false);
        }
    }

    private void leave() {
        this.level().playSound(null, this.blockPosition(), ModSounds.STATIC.get(), SoundSource.HOSTILE, 1.0F, 1.6F);
        this.discard();
    }

    private boolean isWatchedBy(Player player) {
        return InterloperEntity.isWatched(player, this);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            this.leave();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WHISPER.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STATIC.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STATIC.get();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkyghostLife", this.lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifeTicks = tag.getInt("SkyghostLife");
    }

    public static boolean checkSpawnRules(EntityType<SkyghostEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.COMMAND || reason == MobSpawnType.EVENT) {
            return true;
        }
        return level.getLevel().isNight() && random.nextInt(4) == 0;
    }
}
