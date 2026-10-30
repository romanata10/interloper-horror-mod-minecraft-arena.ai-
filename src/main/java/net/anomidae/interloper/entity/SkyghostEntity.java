package net.anomidae.interloper.entity;

import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * A player in an A-pose that is not a player. It watches, it beeps, it spawns
 * things, and sometimes it decides that you should stop playing.
 */
public class SkyghostEntity extends Monster {
    private static final EntityType<?>[] SPAWNABLE = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.PILLAGER, EntityType.DROWNED,
            EntityType.HUSK, EntityType.STRAY, EntityType.SLIME, EntityType.PHANTOM,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.VEX, EntityType.SILVERFISH
    };

    private int lifeTicks;
    private int beepTimer = 40;
    private int spawnTimer = 200;
    private int killTimer = 140;

    public SkyghostEntity(EntityType<? extends SkyghostEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        // It only watches.
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
        if (this.lifeTicks > 20 * 120) {
            this.discard();
            return;
        }

        var nearest = this.level().getNearestPlayer(this, 64.0D);
        if (!(nearest instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        this.getLookControl().setLookAt(player, 40.0F, 40.0F);
        double distance = this.distanceTo(player);
        if (distance < 3.2D) {
            this.discard();
            return;
        }

        boolean watched = InterloperEntity.isWatched(player, this);

        if (--this.beepTimer <= 0) {
            this.beepTimer = 90 + this.random.nextInt(120);
            float pitch = watched ? 1.9F : 1.2F;
            this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.HOSTILE, 0.8F, pitch);
            if (this.random.nextInt(5) == 0) {
                this.level().playSound(null, this.blockPosition(), ModSounds.STATIC.get(),
                        SoundSource.HOSTILE, 0.5F, 1.4F);
            }
        }

        // While observed it does not move. It only exists.
        if (watched) {
            return;
        }

        if (distance > 14.0D && this.tickCount % 10 == 0) {
            Vec3 look = player.getViewVector(1.0F);
            double hop = 11.0D + this.random.nextDouble() * 6.0D;
            double x = player.getX() - look.x * hop + (this.random.nextDouble() - 0.5D) * 6.0D;
            double z = player.getZ() - look.z * hop + (this.random.nextDouble() - 0.5D) * 6.0D;
            double y = player.getY() + 1.0D + this.random.nextDouble() * 2.5D;
            this.randomTeleport(x, y, z, false);
        } else if (distance > 6.0D) {
            Vec3 direction = player.position().subtract(this.position()).normalize().scale(0.06D);
            this.setPos(this.getX() + direction.x, this.getY(), this.getZ() + direction.z);
        }

        if (--this.spawnTimer <= 0) {
            this.spawnTimer = 200 + this.random.nextInt(300);
            this.spawnSomething();
        }

        if (distance < 7.0D && --this.killTimer <= 0) {
            this.killTimer = 200;
            if (this.random.nextInt(6) == 0 && player.isAlive()) {
                player.hurt(player.level().damageSources().genericKill(), 1000.0F);
                this.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.HOSTILE, 1.0F, 0.4F);
            }
        }
    }

    private void spawnSomething() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        EntityType<?> type = SPAWNABLE[this.random.nextInt(SPAWNABLE.length)];
        Entity entity = type.create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) {
            return;
        }
        var player = level.getNearestPlayer(this, 40.0D);
        if (player == null) {
            return;
        }
        double x = player.getX() + (this.random.nextDouble() - 0.5D) * 14.0D;
        double z = player.getZ() + (this.random.nextDouble() - 0.5D) * 14.0D;
        double y = player.getY();
        mob.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(mob);
        level.playSound(null, new BlockPos((int) x, (int) y, (int) z), ModSounds.STINGER.get(),
                SoundSource.HOSTILE, 0.6F, 1.6F);
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
        return true;
    }
}
